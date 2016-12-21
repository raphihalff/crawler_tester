package edu.columbia.cs.tests;

/**
 * The test objects to be sent by TestDriver and run by TestCase,
 * consist of Servers and Nodes. 
 */
public class TestObjects {

    private TestObject[] tests;
    
    protected class TestObject {
        protected String title;
        /* The depth we want to crawl to (inclusive) */
        protected int depth;
        /* Number of unreachable levels, beyond our crawling depth; keep at one for now */
        protected int xtra_depth;
        /* The servers to be crawled and given to DNS;
         * specify url, ip, port, reachability, and if it is a seed */
        protected Server[] servers;
        /* the subdomains to be automatically generated for each node;
         * they may rely on the values of the above servers 
         * the '@' in the name variable will be replaced by identifiers */
        protected Node[] subdomains; 
        /* the robots contents */
        protected String robots;

        /**
         * @param title         the title of the test
         * @param depth         the max depth for the test
         * @param xtra-depth    number of additional levels of unreachable nodes to be made
         * @param servers       the set of servers in this test
         * @param subdomains    the set of nodes in this test
         * @param robots        the robots for this test
         */
        public TestObject(String title, int depth, int xtra_depth, Server[] servers, 
                Node[] subdomains, String robots) {
            this.title = title;
            this.depth = depth;
            this.xtra_depth = xtra_depth;
            this.servers = servers;
            this.subdomains = subdomains;
            this.robots = robots;
        }
        
    }
    
    /**
     * Represents a server entity
     */
    protected class Server {
        
        protected String name;
        protected byte ip;
        protected int port;
        protected boolean should_visit;
        protected boolean is_seed;

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
    protected class Node {

        protected String name;
        protected int is_redirect;
        protected String location;

        /**
         * @param name          the name of the subdomain
         * @param is_redirect   -1 iff not a redirect node, 
         *                      0 iff the node is a redirect,
         *                      1 iff the node links to new domain, but not redirect 
         *                      2 iff the node is empty html
         *                      3 iff the node is non-broken non-empty html
         *                      4 iff the node is broken non-empty html
         * @param location      the redirect location, or null
         */
        public Node(String name, int is_redirect, String location) {
            this.name = name;
            this.is_redirect = is_redirect;
            this.location = location;
        }
    }
    
    /**
     * @return the array of tests
     */
    protected TestObject[] getTests() {
        return tests;
    }

    /**
     * Statically define TestObjects
     */
    public TestObjects() {

        Server[] single_servers = {
            new Server("www.ids.org",       (byte)0b0010, -1, true, true),  /* reachable and seed */
            new Server("www.example.com",   (byte)0b0001, -1, true, false),  /* reachable and not seed */
            new Server("www.good-rdr.fr",   (byte)0b0100, -1, true, false), /* reachable and not seed */
            new Server("www.bad-rdr.fr",    (byte)0b0101, -1, false, false) /* not reachable and not seed */
        };
        Server[] single_straight_servers = {
            new Server("www.ids.org",       (byte)0b0010, -1, true, true)  /* reachable and seed */
        };
        Server[] single_sub_servers = {
            new Server("ids.org",           (byte)0b0001, -1, true, true),  /* reachable and seed */
            new Server("www.ids.org",       (byte)0b0010, -1, true, false),  /* reachable and not seed */
            new Server("cs.ids.org",        (byte)0b0011, -1, false, false),  /* reachable and not seed */
            new Server("cs.click.ids.org",  (byte)0b0100, -1, true, false),  /* reachable and not seed */
            new Server("ids.cs.org",        (byte)0b0101, -1, false, false)  /* not reachable and not seed */
        };
        Server[] straight_servers = {
            new Server("www.example.com",   (byte)0b0001, -1, true, true),  /* reachable and seed */
            new Server("www.ids.org",       (byte)0b0010, -1, true, true),  /* reachable and seed */
            new Server("www.oh_hello.fr",   (byte)0b0100, -1, true, true), /* reachable and seed */
        };
        Server[] mixed_servers = {
            new Server("www.example.com",   (byte)0b0001, -1, true, true),  /* reachable and seed */
            new Server("www.ids.org",       (byte)0b0010, -1, true, true),  /* reachable and seed */
            new Server("www.good-rdr.fr",   (byte)0b0100, -1, true, false), /* reachable and not seed */
            new Server("www.bad-rdr.fr",    (byte)0b0101, -1, false, false) /* not reachable and not seed */
        };
        Node[] single_rdr_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/read/this/page@",    -1, null),                      /* not redirect */
            new Node("/go/here@.html",      -1, null),                      /* not redirect */
            new Node("/rdr@",                0, "http://www.example.com"),  /* is redirect */
            new Node("/redirect@",           0, "http://www.good-rdr.fr"),  /* is redirect */
            new Node("/bad_link@",          1, "http://www.bad-rdr.fr")     /* is external link */
        };
        Node[] rdr_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/read/this/page@",    -1, null),                      /* not redirect */
            new Node("/go/here@.html",      -1, null),                      /* not redirect */
            new Node("/redirect@",           0, "http://www.good-rdr.fr"),  /* is redirect */
            new Node("/bad_link@",          1, "http://www.bad-rdr.fr")     /* is external link */
        };
        Node[] subdomain_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/rdr_sub@",            0, "http://www.ids.org"),      /* is redirect */
            new Node("/sub@",                1, "http://cs.click.ids.org"), /* is external link */
            new Node("/sub_@_np",            1, "cs.ids.org"),              /* is external link */
            new Node("/bad_sub@",            1, "http://ids.cs.org")        /* is external link */
        };
        Node[] single_bad_rdr_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/read/this/page@",    -1, null),                      /* not redirect */
            new Node("/redirect@",           0, "www.good-rdr.fr"),         /* is redirect without protocol*/
            new Node("/rdr@",                0, "http://www.example.com"),  /* is redirect */
            new Node("/empty_rdr@",          0, ""),                        /* is empty redirect */
            new Node("/bad_link@",          1, "")                          /* is empty external link */
        }; 
        Node[] bad_rdr_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/read/this/page@",    -1, null),                      /* not redirect */
            new Node("/redirect@",           0, "www.good-rdr.fr"),         /* is redirect without protocol*/
            new Node("/empty_rdr@",          0, ""),                        /* is empty redirect */
            new Node("/bad_link@",          1, "")                          /* is empty external link */
        }; 
        Node[] empty_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/read/this/page@",    -1, null),                      /* not redirect */
            new Node("/empty@.html",        2, null),                       /* is empty hmtl */
            new Node("/NBNE@",              3, ""),                         /* is non-empty non-broken html */
            new Node("/BNE@.html",          4, "hello")                     /* is non-empty broken html */
        };
        Node[] single_rbtsVrdr_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/rdr@",                0, "http://www.good-rdr.fr"),  /* is redirect */
            new Node("/gd_rdr@",             0, "http://www.example.com"),  /* is redirect */
            new Node("/redirect@",           0, "http://www.bad-rdr.fr"),   /* is redirect */
        };
        Node[] rbtsVrdr_nodes = {
            new Node("/index@.html",        -1, null),                      /* not redirect */
            new Node("/please_enter@",      -1, null),                      /* not redirect */ 
            new Node("/rdr@",                0, "http://www.good-rdr.fr"),  /* is redirect */
            new Node("/redirect@",           0, "http://www.bad-rdr.fr"),   /* is redirect */
        };
        String all_robots = "User-agent: *\nDisallow: \n";
        String rdr_robots = "User-agent: *\nDisallow: /redirect* \n";
        String rdr2_robots = "User-agent: *\nDisallow: www.bad-rdr.fr \n";

        /* For domain crawler */
        
        tests = new TestObject[]{new TestObject("REGULAR REDIRECT", 2, 1, single_servers, single_rdr_nodes, all_robots),
            new TestObject("BADLY FORMED REDIRECT", 2, 1, single_servers, single_bad_rdr_nodes, all_robots),
            new TestObject("EMPTY HTML PAGES", 2, 1, single_straight_servers, empty_nodes, all_robots),
            new TestObject("ROBOTS VS REDIRECT", 3, 1, single_servers, rbtsVrdr_nodes, rdr_robots),
            new TestObject("ROBOTS VS REDIRECT NUMBER 2", 3, 1, single_servers, single_rbtsVrdr_nodes, rdr2_robots)
        };
        /* For regular crawler */
        /*
        tests = new TestObject[]{new TestObject("REGULAR REDIRECT", 2, 1, mixed_servers, rdr_nodes, all_robots),
            new TestObject("BADLY FORMED REDIRECT", 2, 1, mixed_servers, bad_rdr_nodes, all_robots),
            new TestObject("EMPTY HTML PAGES", 2, 1, straight_servers, empty_nodes, all_robots),
            new TestObject("ROBOTS VS REDIRECT", 3, 1, mixed_servers, rbtsVrdr_nodes, rdr_robots),
            new TestObject("ROBOTS VS REDIRECT NUMBER 2", 3, 1, mixed_servers, rbtsVrdr_nodes, rdr2_robots),
            new TestObject("SUBDOMAINS", 2, 1, single_sub_servers, subdomain_nodes, all_robots)
        };*/
    }
}
