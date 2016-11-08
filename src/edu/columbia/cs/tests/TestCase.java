/**
 * The TestCase class should contain:
 *      (1) the depth we want to crawler to crawl 
 *      (2) the seed URLs the crawler will begin with  
 *      (3) the server that will be running and
 *      (4) the monitor class that will be running and passed to each server
 *
 * The TestCase should establish the servers, initialize the DNS server and 
 * pass the the depth and seed URLs to the TestDriver 
 * (which will send them to a class extending the web crawler)
 * The test settings are statically defined.
 *
 * @author Raphi
 */

package edu.columbia.cs.tests;

import edu.columbia.cs.dns_server.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class TestCase extends Thread{
    /* The depth we want to crawl to (inclusive) */
    private final int DEPTH = 2;
    /* Number of unreachable levels, beyond our crawling depth; keep at one for now */
    private final int XTRA_DEPTH = 1;
    /* The servers to be crawled and given to DNS;
     * specify url, ip, port, reachability, and if it is a seed */
    private final Server[] servers = {
        new Server("www.example.com",   (byte)0b0001, -1, true, true),  /* reachable and seed */
        new Server("www.ids.org",       (byte)0b0010, -1, true, true),  /* reachable and seed */
        new Server("www.good-rdr.fr",   (byte)0b0100, -1, true, false), /* reachable and not seed */
        new Server("www.bad-rdr.fr",    (byte)0b0101, -1, false, false) /* not reachable and not seed */
    };
    /* the subdomains to be automatically generated for each node;
     * they may rely on the values of the above servers 
     * the '@' in the name variable will be replaced by identifiers */
    private final Node[] subdomains = {
        new Node("/index@.html",        -1, null),                      /* not redirect */
        new Node("/please_enter@",      -1, null),                      /* not redirect */ 
        new Node("/read/this/page@",    -1, null),                      /* not redirect */
        new Node("/go/here@.html",      -1, null),                      /* not redirect */
        new Node("/redirect@",           0, "http://www.good-rdr.fr"),  /* is redirect */
        new Node("/bad_link@",          1, "http://www.bad-rdr.fr")     /* is external link */
    };
    /* number of subdomains per node, aka children per parent */
    private final int cpp = subdomains.length;
    /* number of servers */
    private final int server_cnt = servers.length;
    /* the robots contents */
    private final String ROBOTS = "User-agent: *\nDisallow: \n";
    
    private TestServer[] server_objs; 
   
    private ArrayList<String> all_urls; 

    private Monitor monitor;

    public TestCase() {
        
        /* Set up and run DNS server */
        /*
         * Don't call this function since we used modified crawler that allows for custom dns resolver--
         * which is established in the TestDriver and CrawlerStarter.
         * runDNS();
         */
        
        /* Establish the servers */
        server_objs = new TestServer[server_cnt];
        all_urls = new ArrayList<String>();
        setupServers();
        startServers();

        /* @TODO make instance of Monitor */
    }

    /**
     * For the server name given this returns a list of all children nodes (unreachable included)
     * @param server_name       the server these nodes belong to
     */
    private TestServerNode[] generateNodes(String server_name) {
       
        int max_depth = DEPTH + XTRA_DEPTH;
        int total_children = 0;
        for (int i = max_depth; i > 0; i--) {
            total_children += (int)java.lang.Math.pow((double)cpp, (double)i); 
        }

        TestServerNode[] nodes = new TestServerNode[total_children];

        int cur_depth = 1;
        int child_index = 0; 
        int sib_index = 0; 
        int main_index = 0;

        while (cur_depth <= max_depth) {

            int gen_sze = (int)java.lang.Math.pow((double)cpp, (double)cur_depth - 1);
            child_index = 0;
            while (child_index < gen_sze) {
                
                String[] paths = getPathnames(server_name, cur_depth, child_index, cpp, max_depth);    
                sib_index = 0;
                while (sib_index < cpp) {
                  
                    int fam_index = (child_index * cpp) + sib_index;
                    if (subdomains[sib_index].is_redirect == 0) {       /* a real redirect */
                        nodes[main_index] = new RedirectServerNode(
                                server_name,
                                paths[sib_index],
                                "", "", 
                                subdomains[sib_index].location, 
                                true);
                    } else if (subdomains[sib_index].is_redirect == 1) { /* an external link */
                        nodes[main_index] = new RedirectServerNode(
                                server_name,
                                paths[sib_index],
                                "", "", 
                                subdomains[sib_index].location, 
                                false);
                    } else {                                             /* a regular node */
                        nodes[main_index] = new HTMLServerNode(
                                server_name, 
                                paths[sib_index], 
                                "", "", 
                                getPathnames(server_name, cur_depth + 1, fam_index, cpp, max_depth));
                    }
                    sib_index++;
                    main_index++;
            
                }
                child_index++;
            }
            cur_depth++;
        }
        return nodes;
    }

    /**
     * Return the pathnames of the sibling nodes at given depth and index.
     * @param server            the server name these sub_domains are nested under
     * @param cur_depth         the depth of the pathnames
     * @param family_index      the grouping identifier (all sibling nodes have same family index)
     * @param cpp               the number of children per parent
     * @param max_depth         the last level of sub_domains (with no children of their own)
     */
    private String[] getPathnames(String server, int cur_depth, int family_index, int cpp, int max_depth) { 
        if (cur_depth > max_depth){
            return new String[0];
        }
        
        String[] children_paths = new String[cpp];
        String replacement_tkn = "d" + cur_depth + "c" + family_index;

        for (int i = 0; i < cpp; i++) {
            children_paths[i] = new String(subdomains[i].name).replaceFirst("@", replacement_tkn); 
            /* add these to list of all urls */
            if (cur_depth < max_depth && !all_urls.contains(server + children_paths[i])) {
                all_urls.add(server + children_paths[i]);
            }
        }
       
       
        return children_paths;
    }

    private void setupServers() {
        /* Create monitor */
        monitor = new Monitor(DEPTH, cpp);
        /* Generate subdomain nodes for each server */
        for (int i = 0; i < server_cnt; i++) {
            TestServerNode[] nodes = generateNodes(servers[i].name);
            TestServerNode[] starting_nodes = new TestServerNode[cpp];
            for (int j = 0; j < cpp; j++){
                starting_nodes[j] = nodes[j];
            }
            
            /* If a specific port was specified */ 
            if (servers[i].port == -1) { // use default port = 80
                server_objs[i] = new TestServer(servers[i].name, servers[i].ip, nodes, starting_nodes, ROBOTS);
            } else { // use specified port and indicate it in seed url
                server_objs[i] = new TestServer(servers[i].name + ":" + servers[i].port, 
                        servers[i].ip, nodes, starting_nodes, ROBOTS);
                /* set port */
                server_objs[i].setPort(servers[i].port);
            }

            /* add servername to url list */
            if (!all_urls.contains(servers[i].name + "/")) {
                all_urls.add(servers[i].name + "/");
            }
            
            /* Add starters to monitor */
            monitor.addStarters(starting_nodes);
            /* and monitor to servers */
            server_objs[i].setMonitor(monitor);
        }
        /* add servers to monitor */
        monitor.setServers(server_objs);
        /* add list of urls to monitor */
        System.out.println(all_urls.size());
        monitor.setURLs(all_urls);
    }

    private void startServers() {
        for (TestServer server : server_objs) {
            if (!server.start()){
                System.err.println("Problem starting TestServer");
            }
        }
    } 

    protected void stopServers() {
        for (TestServer server : server_objs) {
            server.stop();
        }
    }

    protected void sendVisits() {
        for (TestServer server : server_objs) {
            monitor.checkVisits(server.getVisits());
        }
        monitor.checkShouldHaves();
    }

    private void runDNS() {
        /* Set up the DNS server's arguements */
        String[] dns_args = new String[(server_cnt * 2) + 2];
        dns_args[0] = "-s"; //to set DNS Server without load balancer
        dns_args[1] = "53"; //port number 
        int pos, i;
        pos = 2;
        for (i = 0; i < server_cnt; i++) {
            dns_args[pos++] = servers[i].name;
            dns_args[pos++] = "127.0.0." + (int)servers[i].ip;
        }

        /* Setup the DNS Server in its own thread */
        Thread dns_server = new Thread(new Runnable() {
            public void run() {
                    DNSServer.main(dns_args);
            }
        });
        dns_server.start();
    }

    /**
     * @param only_seeds        if true, returns only the seed urls, not all urls
     */
    public InetAddress[] getIPs(boolean only_seeds) {
        InetAddress[] ips = new InetAddress[server_cnt];
        int i = 0;
        for (Server server : servers) {
            if (server.is_seed || !only_seeds) {
               try {
                    byte[] addr = new byte[]{127, 0, 0, server.ip};
                    ips[i++] = InetAddress.getByAddress(addr);
                } catch (java.net.UnknownHostException uhe) {
                    uhe.printStackTrace();
                    System.exit(1);
                }
            }
        }
        
        /* shorten array if needed */
        InetAddress[] abbreviated;
        if (i != server_cnt) {
            abbreviated = new InetAddress[i];
            for (int j = 0; j < i; j++) {
                abbreviated[j] = ips[j];
            }
            return abbreviated;
        }

        return ips;
    }

    /**
     * @param only_seeds        if true, returns only the seed urls, not all urls
     */
    public String[] getURLs(boolean only_seeds) {
        String[] urls = new String[server_cnt];
        int i = 0;
        for (Server server : servers) {
           if (server.is_seed || !only_seeds) {
                urls[i++] = server.name;
           }
        } 
        
        /* shorten array if needed */
        String[] abbreviated;
        if (i != server_cnt) {
            abbreviated = new String[i];
            for (int j = 0; j < i; j++) {
                abbreviated[j] = urls[j];
            }
            return abbreviated;
        }

        return urls;
    }

    public int getDepth() { 
        return DEPTH;
    }

    /**
     * Represents a server entity
     */
    private class Server {
        
        private String name;
        private byte ip;
        private int port;
        private boolean should_visit;
        private boolean is_seed;

        /** 
         * @param name          the name of the server 
         * @param ip            the last byte of the local ip
         * @param port          the port number, -1 for default port 80 
         * @param should_visit  true iff the server should be visited
         * @param is_seed       true iff this server should be given to crawler 
         *                      as a seed url
         */
        public Server(String name, byte ip, int port, boolean should_visit, boolean is_seed) {
            this.name = name;
            this.ip = ip;
            this.port = port;
            this.should_visit = should_visit;
            this.is_seed = is_seed;
        }
    } 

    /**
     * Represents a sub_domain node
     */
    private class Node {

        private String name;
        private int is_redirect;
        private String location;

        /**
         * @param name          the name of the subdomain
         * @param is_redirect   -1 iff not a redirect node, 
         *                      0 iff the node is a redirect,
         *                      1 iff the node links to new domain, but not redirect 
         * @param location      the redirect location, or null
         */
        public Node(String name, int is_redirect, String location) {
            this.name = name;
            this.is_redirect = is_redirect;
            this.location = location;
        }
    }
} 


