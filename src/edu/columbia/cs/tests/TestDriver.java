package edu.columbia.cs.tests;

import edu.columbia.cs.crawler_starter.CrawlerStarter;
import edu.columbia.cs.domain_crawler.DomainCrawler;
import java.net.InetAddress;
import org.apache.http.impl.conn.InMemoryDnsResolver;

public class TestDriver {

    private static int depth;
    private static String[] seed_urls;
    private static InetAddress[] seed_ips;
    private static String crawl_dir;
    private static final String protocol = "http://";

    /**
     * Runs the Test and the Crawler.
     * @param crawl_data        the directory for crawler data
     */
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("No crawler_data directory given to TestDriver");
            return;
        }
        crawl_dir = args[0];


        System.out.print("\nSetting up test....");
        /* Run the test which sets up DNS and Servers */
        TestCase test = new TestCase();
        
        depth = test.getDepth();
        /* get urls for dns resolving */
        seed_urls = test.getURLs(false);
        seed_ips = test.getIPs(false);

        /* Setup the InMemoryDNSResolver object */
        InMemoryDnsResolver dns = new InMemoryDnsResolver();
        for (int i = 0; i < seed_urls.length; i++) {
            dns.add(seed_urls[i], seed_ips[i]);
        }

        System.out.print("DONE\n");
        
        /* get seed urls for crawler */
        seed_urls = test.getURLs(true);
        seed_ips = test.getIPs(true);

        /* Compile args for crawler; 
         * NOTE: add protocol! */ 
        String[] crawler_args = new String[2 + seed_urls.length];
        crawler_args[0] = crawl_dir;
        crawler_args[1] = depth + "";
        for (int i = 2; i < crawler_args.length; i++) {
            crawler_args[i] = protocol + seed_urls[i-2];
        }
        /* start crawler */
        System.out.print("\nStarting crawler\n");
        try {
            CrawlerStarter crwl_strtr = new CrawlerStarter(dns);
            crwl_strtr.main(crawler_args); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("\ncrawler DONE\n");

        /* stop the servers */
        System.out.print("\nstopping servers\n");
        test.stopServers();
        test.sendVisits();

    }

}
