package edu.columbia.cs.tests;

import java.util.LinkedList;
import java.util.ArrayList;
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
    
    /* The nodes of current depth */
    protected LinkedList<TestServerNode> curr_nodes; 
    /* The nodes belonging to next depth */
    protected LinkedList<TestServerNode> next_nodes; 
    /* the servers */
    protected TestCase.Server[] servers;
    /* the nodes that should not be reached */
    protected ArrayList<String> prohib_nodes;
    /* all the urls that should be visited */
    private static ArrayList<String> to_visit_nodes;
    /* visit log */
    private static ArrayList<TestVisitRecord> log;

    /**
     * @param depth             the max depth that should be crawled (inclusive)
     * @param cpp               the number of children nodes per parent
     */
    public Monitor(int depth, int cpp){
        DEPTH = depth;
        CPP = cpp;

        crawler_depth = 1;
        next_nodes = new LinkedList<TestServerNode>();
        curr_nodes = new LinkedList<TestServerNode>();
        prohib_nodes = new ArrayList<String>();
        to_visit_nodes = new ArrayList<String>();
        log = new ArrayList<TestVisitRecord>();
    }

    /**
     * @param urls      the list of all urls that should be visited
     */
    public void setURLs(ArrayList<String> urls) {
        for (String url : urls) {
            for (TestCase.Server server : servers) {
                if (url.indexOf(server.name) != -1 && 
                        server.should_visit && 
                        url.indexOf("d" + (DEPTH+1) + "c") == -1) {
                    
                    to_visit_nodes.add(url);
                    break;
                } else if ((url.indexOf(server.name) != -1 && !server.should_visit) 
                        || url.indexOf("d" + (DEPTH+1) + "c") == -1) {
                    
                    prohib_nodes.add(url);
                    break;
                }
            }  
        }
    }
    
    /**
     * @param starter_nodes     the starting nodes for this server
     */
    public void addStarters(TestCase.Server[] servers) {
        this.servers = servers;   
        int seed_cnt = 0;     
        for (TestCase.Server server : this.servers) {
            if (server.is_seed && server.should_visit) {
                to_visit_nodes.add(server.name);
                seed_cnt++;
            } else if (server.should_visit) {
                to_visit_nodes.add(server.name);
            } else {
                System.out.println("Prohibited server: "  + server.name);
                prohib_nodes.add(server.name);
            }
        }        
        nodes_this_depth = seed_cnt * CPP;
    }

    public boolean verify(TestServerNode new_node) {
        String url = new_node.getServerName() + new_node.id;
        if (to_visit_nodes.remove(url)) {
            return true;
        } else if (prohib_nodes.indexOf(url) != -1) {
           log.add(new TestVisitRecord(null, new_node.getServerName() + new_node.id, true));
           return false;
        } 
       
        log.add(new TestVisitRecord(null, new_node.getServerName() + new_node.id, false));

        return true;
    }

    protected boolean checkVisits() { 
        boolean correct = true;
        for (TestVisitRecord visit : log) {
            if (visit.hasError()) {
                System.out.println("Error--Should NOT have been visited: " + visit.getURL());
                correct = false;
            } else {
                System.out.println("Error--Ambiguous, this WAS visited: " + visit.getURL());
                correct = false;
            } 
        }
        return correct;
    }

    protected boolean checkShouldHaves() {
        for (String node : to_visit_nodes) { 
            System.out.println("Error--Should have been visited: " + node);
        }
        return to_visit_nodes.isEmpty();
    } 
}
