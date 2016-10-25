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
        for (int i = starter_nodes.length - 1; i >= 0; i--) {
           starter_stack.push(starter_nodes[i]);
        } 
        //for (TestServerNode node : starter_nodes) {
        //    starter_stack.push(node);
        //}        
    }

    public boolean verify(TestServerNode new_node) {
        if (crawler_depth == 1) {
            System.out.println("BEFORE-starter:");
            for (int i = 0; i < starter_stack.size(); i++) {
                System.out.println(starter_stack.get(i).id);
            }
            if (starter_stack.size() == 0) {
                crawler_depth++;
            } else if (starter_stack.peek().checkChild(new_node)) {
                visit_stack.push(starter_stack.pop());
                return true; 
            } else {
                return false;
            }
        }
        
        System.out.println("BEFORE:");
        for (int i = 0; i < visit_stack.size(); i++) {
            System.out.println(visit_stack.get(i).id);
        }
        
        while (visit_stack.size() > 0 && !visit_stack.peek().checkChild(new_node)){
            visit_stack.pop();
        }
        System.out.println("AFTER:");
        for (int i = 0; i < visit_stack.size(); i++) {
            System.out.println(visit_stack.get(i).id);
        }

        if (visit_stack.size() == 0) {
            return false;
        }

        visit_stack.push(new_node);
        return true;
    }

}
