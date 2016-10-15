package edu.columbia.cs.dns_server;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 * A list that keeps track of available host IPs.
 * Their domain names are irrelevant and not recorded.
 * For an alternate host-store that does manage the names see HostTable.
 * 
 * @author Raphi
 *
 */
class HostList extends HostStore{
	
    private LinkedList<InetAddress> list;
    private final static HostList instance = new HostList();

    /**
     * Initializes empty list. 
     */
    private HostList() {
        list = new LinkedList<InetAddress>();
    }
	
    /**
     * Retrieves the unique instance of the list.
     * Allows one instance of the host list to be used by both the DNS request listener and the server-availability listener
     * @return the existing instance of HostList, or a new one if none exist
     */
    public static HostList getInstance(){
        //if(instance==null){
        //instance = new HostList();
        //}
        return instance;
    }

    /**
     * Adds IP of free host to the list
     * @param IP
     */
    @Override
    public void add_host(InetAddress IP)
    {
    	//System.out.println(IP.toString());
        list.add(IP);
    }

    /**
     * Not used or implemented
     */
    @Deprecated
    @Override
    public void add_host(String the_host_name, InetAddress the_IP) {
    }
	
    /**
     * Returns IP of available host
     * @return Free_Host_IP
     */
    public InetAddress get_IP()
    {
        return list.removeFirst();
    }
    
    /**
     * Returns IP of available host, ignoring the_host_name passed
     * @param the_host_name, ignored
     */
    @Override
    @Deprecated
    public InetAddress get_IP(String the_host_name) {
    	 return (InetAddress)list.removeFirst();
    }
    
    /**
     * returns true if list is empty
     * @return is_empty
     */
    @Override
    public boolean isEmpty(){
    	return list.isEmpty();
    }

    /**
     * Returns false since this list stores only IPs
     * @return false
     */
    @Override
    public boolean isKeyed() {
            return false;
    }
    
}
