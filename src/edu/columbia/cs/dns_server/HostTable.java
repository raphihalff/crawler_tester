package edu.columbia.cs.dns_server;
import java.net.*;
import java.util.Hashtable;
/**
 * A hashtable alternative to the linked list, keeping track of IPs
 */
class HostTable extends HostStore
{
    private Hashtable<String, InetAddress> the_hosts_table;
    private final static HostTable instance = new HostTable();

    private HostTable()
    {
        the_hosts_table = new Hashtable<String, InetAddress>();
    }

    /**
     * Retrieves the unique instance of the table.
     * Allows one instance of the host table to be used by both the DNS request listener and the server-availability listener
     * @return the existing instance of HostTable, or a new one if none exist
     */
    public static HostTable getInstance(){
        return instance;
    }
	
    /**
     * Adds the IP-DomainName pair to the table of hosts
     * @param the host name
     * @param IP
     */
    @Override
    public void add_host(String the_host_name, InetAddress the_IP)
    {
        the_hosts_table.put(the_host_name.toLowerCase(), the_IP);
    }
	
    /**
     * Not used or implemented
     */
    @Override
    @Deprecated
    public void add_host(InetAddress IP)
    {
    }

    @Override
    public InetAddress get_IP(String the_host_name)
    {
        return (InetAddress)the_hosts_table.get(the_host_name);
    }
	
    /**
     * Not used or implemented.
     */
    @Override
    @Deprecated
    public InetAddress get_IP() {
        return null;
    }
	
    /**
     * returns true if table is empty
     * @return is_empty
     */
    @Override
    public boolean isEmpty() {
        return the_hosts_table.isEmpty();
    }

    /**
     * Returns true, since this table stores hostname-IP pairs.
     * @return true
     */
    @Override
    boolean isKeyed() {
        return true;
    }
}
