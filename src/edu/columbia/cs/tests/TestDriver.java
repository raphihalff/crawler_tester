package edu.columbia.cs.tests;

import edu.columbia.cs.crawler_starter.CrawlerStarter;

public class TestDriver {

    private static int depth;
    private static String[] seed_urls;
    private static String crawl_dir;

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
        System.out.print("DONE\n");

        depth = test.getDepth();
        seed_urls = test.getSeedURLs();
        
        /* Compile args for crawler */
        String[] crawler_args = new String[2 + seed_urls.length];
        crawler_args[0] = crawl_dir;
        crawler_args[1] = depth + "";
        for (int i = 2; i < crawler_args.length; i++) {
            crawler_args[i] = seed_urls[i-2];
        }
        /* start crawler */
        System.out.print("\nStarting crawler\n");
        try {
            CrawlerStarter.main(crawler_args); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("\ncrawler DONE\n");

        /* stop the servers */
        System.out.print("\nstopping servers\n");
        test.stopServers();

    }

}
