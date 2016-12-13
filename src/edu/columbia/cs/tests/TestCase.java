/**
 * The TestCase runs an individual test given servers and nodes (TestObjects) from the TestDriver,
 * the DNS resolver is set up in TestDriver and CrawlerStarter.
 * @author Raphi
 */

package edu.columbia.cs.tests;

import edu.columbia.cs.dns_server.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class TestCase extends Thread{
    /* The depth we want to crawl to (inclusive) */
    private final int DEPTH;
    /* Number of unreachable levels, beyond our crawling depth; keep at one for now */
    private final int XTRA_DEPTH;
    /* The servers to be crawled and given to DNS;
     * specify url, ip, port, reachability, and if it is a seed */
    private final TestObjects.Server[] servers;
    /* the subdomains to be automatically generated for each node;
     * they may rely on the values of the above servers 
     * the '@' in the name variable will be replaced by identifiers */
    private final TestObjects.Node[] subdomains;
    /* number of subdomains per node, aka children per parent */
    private final int cpp;
    /* number of servers */
    private final int server_cnt;
    /* the robots contents */
    private final String ROBOTS;
    
    private TestServer[] server_objs; 
   
    private ArrayList<String> all_urls; 

    private Monitor monitor;
    /**
     * @param t the TestObject containing the servers and nodes to be tested
     */
    public TestCase(TestObjects.TestObject t) {

        /* Get the tests variables */
        DEPTH = t.depth;
        XTRA_DEPTH = t.xtra_depth;
        servers = t.servers;
        subdomains = t.subdomains;
        ROBOTS = t.robots;
        cpp = subdomains.length;
        server_cnt = servers.length;
        
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
                    } else if(subdomains[sib_index].is_redirect == 2) { /* an empty html node */
                        nodes[main_index] = new BlankServerNode(
                                server_name,
                                paths[sib_index],
                                "", "",
                                null, false);
                    } else if(subdomains[sib_index].is_redirect == 3) { /* a non-broken non-empty html node */
                        nodes[main_index] = new BlankServerNode(
                                server_name,
                                paths[sib_index],
                                "", "",
                                subdomains[sib_index].location, false);
                    } else if(subdomains[sib_index].is_redirect == 4) { /* a broken non-empty html node */
                        nodes[main_index] = new BlankServerNode(
                                server_name,
                                paths[sib_index],
                                "", "",
                                subdomains[sib_index].location, true);
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
            /* add these to list of all urls avoid nodes generated but that don't exist*/
            if (!all_urls.contains(server + children_paths[i]) && subdomains[family_index % cpp].is_redirect == -1) {
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

            
            /* and monitor to servers */
            server_objs[i].setMonitor(monitor);
        }
        /* Add starters to monitor */
        monitor.addStarters(servers);
        /* add list of urls to monitor */
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

    protected void sendVisits(String test_title) {
        monitor.print(test_title);
        for (TestServer server : server_objs) {
            monitor.checkVisits();
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
     * @return the corresponding IP addresses ({@code InetAddress}) 
     */
    public InetAddress[] getIPs(boolean only_seeds) {
        InetAddress[] ips = new InetAddress[server_cnt];
        int i = 0;
        for (TestObjects.Server server : servers) {
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
     * @return the seed urls or all urls
     */
    public String[] getURLs(boolean only_seeds) {
        String[] urls = new String[server_cnt];
        int i = 0;
        for (TestObjects.Server server : servers) {
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


} 


