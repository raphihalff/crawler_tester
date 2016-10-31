package edu.columbia.cs.tests;

import java.util.LinkedList;
/**
 * -Keep track of crawler's current depth and enforce it
 * -Enforce BFS crawling order
 * -Ensure that prohibited nodes remain unvisited
 * -Ensure after crawl, that all good nodes were visited.
 */
public class Monitor {
    /* The max depth that should be crawled (inclusive) */
    protected final int DEPTH;
    /* The current depth of the crawler */
    private static int crawler_depth;
    /* The number of expected nodes */
    private static int nodes_this_depth;
    /* The number of nodes already seen and on queue */
    private static int on_queue;
    /* counter to keep track of nodes */
    private static int nodes_seen;
    /* Number of children per parent, for our tree structrue */
    protected final int CPP; 
    /* The starting nodes record keeper */
    protected LinkedList<TestServerNode> starter_stack; 
    /* The visit recored keeper */
    protected LinkedList<TestServerNode> visit_stack; 
    /* the servers for post check*/
    private TestServer[] servers;

    /**
     * @param depth             the max depth that should be crawled (inclusive)
     * @param cpp               the number of children nodes per parent
     */
    public Monitor(int depth, int cpp){
        DEPTH = depth;
        CPP = cpp;
        this.servers = servers;

        crawler_depth = 1;
        visit_stack = new LinkedList<TestServerNode>();
        starter_stack = new LinkedList<TestServerNode>();
    }

    /**
     * @param servers           the servers running for this test
     */
    public void setServers(TestServer[] servers) {
        this.servers = servers;
    }
    
    /**
     * @param starter_nodes     the starting nodes for this server
     */
    public void addStarters(TestServerNode[] starter_nodes) {
        for (TestServerNode node : starter_nodes) {
            starter_stack.addLast(node);
        }        
        nodes_this_depth = starter_stack.size() * CPP;
    }

    public boolean verify(TestServerNode new_node) {
        /* ensures depth 1 is crawled before depth 2, but in no particular order */
        if (crawler_depth == 1) { 
            
            if (starter_stack.size() == 0) {
                crawler_depth++;
                nodes_this_depth *= CPP;
                on_queue = visit_stack.size() - 1;
                nodes_seen = 0;
            } else {
                int time_out = 0;
                boolean correct = false;
                while (time_out < starter_stack.size() && 
                        !(correct = starter_stack.peekFirst().id.equals(new_node.id))) {
                     starter_stack.addLast(starter_stack.removeFirst());
                     time_out++;
                }

                if (correct) {
                    visit_stack.addLast(starter_stack.removeFirst());
                    System.out.println("TRUE (DEPTH 1)");
                    return true;
                }

                System.out.println("FALSE 1");
                System.out.println("onstack: " + starter_stack.peekFirst().id + ", newnode: " + new_node.id);
                return false;
            }
        }
        
        if (nodes_seen == nodes_this_depth) {
            for (int i = 0; i <= on_queue; i++) {
               visit_stack.removeFirst();
            }
            on_queue = nodes_seen;
            nodes_seen = 0;
            nodes_this_depth *= CPP;
            crawler_depth++;
        }
        
        System.out.println("THE STACK:");
        for (int i = 0; i < visit_stack.size(); i++) {
            System.out.println(visit_stack.get(i).id);
        }

        int exhausted = 0;
        while (visit_stack.size() > 0 && !visit_stack.peekFirst().checkChild(new_node) && exhausted++ <= on_queue){
            visit_stack.add(on_queue, visit_stack.removeFirst());
        }

        if (visit_stack.size() == 0 || crawler_depth > DEPTH) {
            System.out.println("FALSE 2");
            return false;
        }

        visit_stack.addLast(new_node);
        nodes_seen++;
        System.out.println("TRUE");
        return true;
    }

}
