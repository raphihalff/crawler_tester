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

public class TestCase extends Thread{
    /* The depth we want to crawl to (inclusive) */
    private final int DEPTH = 2;
    /* Number of unreachable levels, beyond our crawling depth; keep at one for now */
    private final int XTRA_DEPTH = 1;
    /* Server URLs to be given to crawler as seed urls and to be given to DNS;
     * MUST be same number of seed urls as seed ips */
    private final String[] SEED_URL = {"www.example.com", "www.ids.org", "www.av-hacks.net"};
    /* The last byte of the corresponding local IPs (i.e. 127.0.0.last_byte) */
    private final byte[] SEED_IP = {(byte)0b0001, (byte)0b0010, (byte)0b0011};
    /* The subdomains to be layered as children under seed urls, the '@' will be replaced by identifiers */
    private final String[] DEFAULT_SUBDOMAINS = {
        "/index@.html", 
        "/please_enter@", 
        "/read/this/page@",
        "/go/here@.html"};
    private final String ROBOTS = "User-agent: *\nDisallow: \n";
    
    private TestServer[] servers; 
    
    private Monitor monitor;

    public TestCase() {
        
        /* Set up and run DNS server */
        /*
         * Don't call this function since we used modified crawler that allows for custom dns resolver--
         * which is established in the TestDriver and CrawlerStarter.
         * runDNS();
         */
        
        /* Establish the servers */
        servers = new TestServer[SEED_URL.length];
        setupServers();
        startServers();

        /* @TODO make instance of Monitor */
    }

    /**
     * For the server name given this returns a list of all children nodes (unreachable included)
     * @param server_name       the server these nodes belong to
     */
    private HTMLServerNode[] generateHTMLNodes(String server_name) {
       
        int max_depth = DEPTH + XTRA_DEPTH;
        int children_per_parent = DEFAULT_SUBDOMAINS.length;
        int total_children = 0;
        for (int i = max_depth; i > 0; i--) {
            total_children += (int)java.lang.Math.pow((double)children_per_parent, (double)i); 
        }

        HTMLServerNode[] nodes = new HTMLServerNode[total_children];

        int cur_depth = 1;
        int child_index = 0; 
        int sib_index = 0; 
        int main_index = 0;

        while (cur_depth <= max_depth) {

            int gen_sze = (int)java.lang.Math.pow((double)children_per_parent, (double)cur_depth - 1);
            child_index = 0;
            while (child_index < gen_sze) {
                
                String[] paths = getPathnames(server_name, cur_depth, child_index, children_per_parent, max_depth);    
                sib_index = 0;
                while (sib_index < children_per_parent) {
                  
                    int fam_index = (child_index * children_per_parent) + sib_index;
                    nodes[main_index] = new HTMLServerNode(
                            server_name, 
                            paths[sib_index], 
                            "", "", 
                            getPathnames(server_name, cur_depth + 1, fam_index, children_per_parent, max_depth));

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
            children_paths[i] = new String(DEFAULT_SUBDOMAINS[i]).replaceFirst("@", replacement_tkn); 
        }
        
        return children_paths;
    }

    private void setupServers() {
        /* Create monitor */
        monitor = new Monitor(DEPTH, DEFAULT_SUBDOMAINS.length);
        /* Generate subdomain nodes for each server */
        for (int i = 0; i < SEED_URL.length; i++) {
            HTMLServerNode[] nodes = generateHTMLNodes(SEED_URL[i]);
            HTMLServerNode[] starting_nodes = new HTMLServerNode[DEFAULT_SUBDOMAINS.length];
            for (int j = 0; j < DEFAULT_SUBDOMAINS.length; j++){
                starting_nodes[j] = nodes[j];
            } 
            servers[i] = new TestServer(SEED_URL[i], SEED_IP[i], nodes, starting_nodes, ROBOTS);
            /* Add starters to monitor */
            monitor.addStarters(starting_nodes);
            /* and monitor to servers */
            servers[i].setMonitor(monitor);
        }
        /* add servers to monitor */
        monitor.setServers(servers);
    }

    private void startServers() {
        for (TestServer server : servers) {
            if (!server.start()){
                System.err.println("Problem starting TestServer");
            }
        }
    } 

    public void stopServers(){
        for (TestServer server : servers) {
            server.stop();
        }
    }

    private void runDNS() {
        /* Set up the DNS server's arguements */
        String[] dns_args = new String[SEED_URL.length + SEED_IP.length + 2];
        dns_args[0] = "-s"; //to set DNS Server without load balancer
        dns_args[1] = "53"; //port number 
        int pos, i;
        pos = 2;
        for (i = 0; i < SEED_URL.length; i++) {
            dns_args[pos++] = SEED_URL[i];
            dns_args[pos++] = "127.0.0." + (int)SEED_IP[i];
        }

        /* Setup the DNS Server in its own thread */
        Thread dns_server = new Thread(new Runnable() {
            public void run() {
                    DNSServer.main(dns_args);
            }
        });
        dns_server.start();
    }

    public InetAddress[] getSeedIPs() {
        InetAddress[] ips = new InetAddress[SEED_IP.length];
        for (int i = 0; i < SEED_IP.length; i++) {
            byte[] addr = new byte[]{127, 0, 0, SEED_IP[i]};
            try {
                ips[i] = InetAddress.getByAddress(addr);
            } catch (java.net.UnknownHostException uhe) {
                uhe.printStackTrace();
                System.exit(1);
            }
        }
        return ips;
    }

    public String[] getSeedURLs() {
        return SEED_URL;
    }

    public int getDepth() { 
        return DEPTH;
    }
} 


