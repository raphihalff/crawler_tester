package edu.columbia.cs.dns_server;
import java.net.InetAddress;
/**
 * The basic framework for the hostname/IP datastructure.
 * @author Raphi
 *
 */
public abstract class HostStore {
	
    protected static byte[] MAGIC = "I am free!".getBytes();
    protected static String DOMAIN = null;
    protected static InetAddress[] valid_lb_IP = null;
    
    /**
     * Retrieves the unique instance of the host store
     * Allows one instance of the host list to be used by both the DNS request listener and the server-availability listener
     * @return the existing instance of Host store, or a new one if none exist
     */
    public static HostStore getInstance(){
        return null;
    }

    /**
     * Adds IP of free host to the store
     * @param IP
     */
    abstract void add_host(InetAddress IP);
    
    /**
     * Adds the IP-DomainName pair to the store
     * @param the_host_name
     * @param the_IP
     */
    abstract void add_host(String the_host_name, InetAddress the_IP);

    /**
     * Returns IP of available host
     * @return Free_Host_IP
     */
    abstract InetAddress get_IP();
    
    /**
     * Returns IP of specified host
     * @param the_host_name
     * @return host_IP
     */
    abstract InetAddress get_IP(String the_host_name);
    
    /**
     * Returns true if store is empty
     * @return is_empty
     */
    abstract boolean isEmpty();
    
    /**
     * Returns true if the host store functions by key-value pairs (ie. hostname-IP).
     * This returns false for the list case, true for the table case.
     * @return	is_keyed
     */
    abstract boolean isKeyed();
    
    /**
     * Set the magic value used by servers to announce their availability in the load-balancing mode.
     * @param magic
     */
    public void setMagic(String magic){
    	MAGIC = magic.getBytes();
    }
    /**
     * Get the magic value for this HostStore, used in load-balancing mode.
     * @return the magic value, or null if it was not set.
     */
    public byte[] getMagic(){
    	return MAGIC;
    }
    
    /**
     * Set the load_balancer domain name.
     * @param domain_name
     */
    public void setDomainName(String domain_name){
    	DOMAIN = domain_name;
    }
    /**
     * Get the load-balancer domain name.
     * @return the domain name.
     */
    public String getDomainName(){
    	return DOMAIN;
    }
    /**
     * Set the servers the load balancer is balancing--to insure we do not add an unknown server to the list of available servers.
     * @param IPs the load-balancer servers (meant for HostList use)
     */
    public void setLB_IP(InetAddress[] IPs) {
    	valid_lb_IP = IPs;
    }
    
    /**
     * Set the servers the load balancer is balancing--to insure we do not add an unknown server to the list of available servers.
     * @param IPs the load-balancer servers (meant for HostList use)
     */
    public InetAddress[] getLB_IP() {
    	return valid_lb_IP;
    } 
}
