package edu.columbia.cs.tests;

import java.util.LinkedList;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Monitor to verify which nodes were properly or improperly visited (or not visited) by crawler
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
    protected TestObjects.Server[] servers;
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
     * Set the urls that must be visted by crawler
     * @param urls      the list of all urls that should be visited
     */
    public void setURLs(ArrayList<String> urls) {
        for (String url : urls) {
            boolean added = false;
            for (TestObjects.Server server : servers) {
                if (url.indexOf(server.name) != -1 && 
                        server.should_visit && 
                        url.indexOf("d" + (DEPTH + 1) + "c") == -1) {
   
                    /* If the server is not a seed, its depth is one level deeper than indicated */ 
                    if (!server.is_seed && url.indexOf("d" + DEPTH + "c") != -1) {
                        break;
                    }
                    to_visit_nodes.add(url);
                    added = true;
                    break;
                }
            }

            if (!added) {
                    prohib_nodes.add(url);
            }  
        }
    }
    
    /**
     * Set the servers for this test
     * @param starter_nodes     the starting nodes for this server
     */
    public void addStarters(TestObjects.Server[] servers) {
        this.servers = servers;   
        int seed_cnt = 0;     
        for (TestObjects.Server server : this.servers) {
            if (server.is_seed && server.should_visit) {
//                to_visit_nodes.add(server.name);
                seed_cnt++;
            } else if (server.should_visit) {
//                to_visit_nodes.add(server.name);
            } else {
                prohib_nodes.add(server.name);
            }
        }        
        nodes_this_depth = seed_cnt * CPP;
    }

    /**
     * Checks if node was visited correctly and logs result
     * @param new_node  the node to be checked
     * @return true iff node is correctly visited
     */
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

    protected void print(String text) {
        try {    
            FileWriter f = new FileWriter("results", true);
            f.write("\n" + text + "\n", 0, text.length() + 2);
            f.close(); 
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    /**
     * Ouputs visitation results for visited nodes.
     * @return true, iff there was no error
     */
    protected boolean checkVisits() { 
        boolean correct = true;
        try {
            FileWriter f = new FileWriter("results", true);
            for (TestVisitRecord visit : log) {
                if (visit.hasError()) {
                    System.out.println("Error--Should NOT have been visited: " + visit.getURL());
                    String tmp = "Error--Should NOT have been visited: " + visit.getURL() + "\n";
                    f.write(tmp, 0, tmp.length());
                    correct = false;
                } else {
                    System.out.println("Error--Ambiguous, this WAS visited: " + visit.getURL());
                    String tmp = "Error--Ambiguous, this WAS visited: " + visit.getURL() + "\n";
                    f.write(tmp, 0, tmp.length());
                    correct = false;
                } 
            }
            f.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return correct;
    }

    /**
     * Outputs visitation results for node that should have been visited, but were not
     * @return true, iff there were no errors (everything was visited)
     */
    protected boolean checkShouldHaves() {
        try {
            FileWriter f = new FileWriter("results", true);
            for (String node : to_visit_nodes) { 
                System.out.println("Error--Should have been visited: " + node);
                String tmp = "Error--Should have been visited: " + node + "\n";
                f.write(tmp, 0, tmp.length());
            }
            f.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return to_visit_nodes.isEmpty();
    } 
}
