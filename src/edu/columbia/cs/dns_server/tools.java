package edu.columbia.cs.dns_server;
/**
 * Prints debug info from DNS classes
 */
class tools {
    public final static boolean TOOLS_DEBUG=true;
    public final static boolean CONNECTION_DEBUG=true;

    public static void report(String str) {
        System.out.println(str);
    }
    public static void report(String str,Exception e) {
        System.out.println(str + " : " + e.getMessage());
    }
    public static void debug_flow(String str) {
        if (TOOLS_DEBUG) report("DEBUG : " + str);
    }
    public static void debug_connection(String str) {
        if (CONNECTION_DEBUG) report("DEBUG : " + str);
    }
}
