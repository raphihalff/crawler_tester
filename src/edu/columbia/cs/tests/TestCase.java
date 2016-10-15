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
public class TestCase {
    
    private final int DEPTH = 2;
    private final String[] SEEDS = {"www.example.com", "www.ids.org", "www.av-hacks.net"};

    public TestCase() {
        /* @TODO make instance of Monitor */
        /* @TODO etsablish the servers */

        /* Setup the DNS Server */

    }
} 


