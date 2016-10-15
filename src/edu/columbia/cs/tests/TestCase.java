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

public class TestCase extends Thread{
    
    private final int DEPTH = 2;
    private final String[] SEED_URL = {"www.example.com", "www.ids.org", "www.av-hacks.net"};
    private final String[] SEED_IP = {"127.0.0.1", "127.0.0.2", "127.0.0.3"};

	
    public TestCase() {
        runDNS();
        /* @TODO make instance of Monitor */
        /* @TODO etsablish the servers */
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
            dns_args[pos++] = SEED_IP[i];
        }

        /* Setup the DNS Server in its own thread */
        Thread dns_server = new Thread(new Runnable() {
            public void run() {
                    DNSServer.main(dns_args);
            }
        });
        dns_server.start();
    }
} 


