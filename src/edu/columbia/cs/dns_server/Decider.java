package edu.columbia.cs.dns_server;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Responds to DNS queries and server-availability messages.
 * Sends send DNS response if queried domain is expected and if and domains are marked as available.
 * Adds server to list of available server if the magic key is correct and the IP is expected.
 * @author Raphi
 *
 */
public class Decider{
	
	private HostStore hosts; 
        private static String DOMAIN;
        public static final String[] COLORS = {"\u001B[31m",
                                            "\u001B[32m",
                                            "\u001B[33m",
                                            "\u001B[34m",
                                            "\u001B[35m",
                                            "\u001B[36m"};
            //Reset code
        public static final String RESET = "\u001B[0m";
	
	/**
         * This contructor initalizes a HostList, for load-balancing scenarios.
	 * Initialize the hoststable here, ie. the domains and IP we service. 
         * @param the_IPs               the IPs corresponding to the servers
         * @param the_domain_name       the domain name our servers go by
	 */
	public Decider(InetAddress[] IP, String unique_domain){
            hosts = HostList.getInstance();
            hosts.setLB_IP(IP); 
            hosts.setDomainName(unique_domain);
	}

        /**
         * This constructor initializes a HostTable, for crawler testing.
         * It will add the passed IP-hostname combos (indexes correspond) to the "DNS library".
         * @param the_IPs
         * @param the_domain_names
         */	
	public Decider(InetAddress[] IP, String[] domains){
            hosts = HostTable.getInstance();
            for (int i = 0; i < IP.length; i++){
                hosts.add_host(domains[i], IP[i]);
            }
        }

        /**
         * This constructor uses the existing HostList without adding new entries.
         */
         public Decider() {
             hosts = HostList.getInstance();
         }

         public InetAddress[] getLB_IP(){
             return hosts.getLB_IP();
         }
	/**
	 * Checks the type of request, then either updates linked-list of available hosts, or returns response packet
	 * A message of availability from a registered host must have the UDP data field filled with FREE_KEY.
	 * A DNS query for an IP must be of type A 
         * (type ANY also works, this is often the type specified after a failed A request)
	 * 
	 * @param packet		the datagram packet
	 * @param type 			0 for DNS, 1 for UDP (server free)
	 *  
	 * @return response_packet or null when invalid type, lb_server msg comes from unrecognized IP, lb_server 
         * gives invalid magic phrase.
	 */
	public DatagramPacket decide(DatagramPacket packet, int type){
	       
           if (type == 0) { 
               //DNS Request
               return decide_dns(packet);
           } else if (type == 1) {
               //Load-Balance Msg
               return decide_lb(packet);
           } else {
               //ERROR, type doesn't exist
               System.out.println("Decider: invalid input type, must be either"+
                                  " 0 (for DNS) or 1 (for standard UDP); given " + type);
               return null;
           } 
        }

        /**
         * Create the reponse packet for a DNS request.
         * @param the_dns_query
         * @return the_dns_response
         */
        private DatagramPacket decide_dns(DatagramPacket packet){
            DatagramPacket responsePacket;
            DNSResponse response;
            
            //Have packet parsed and analyzed
            DNSRequest query = new DNSRequest(packet);
            
            if (query.DNS_type==DNSEntity.TYPE_A || query.DNS_type==DNSEntity.TYPE_ANY){//DNS_type==1==TYPE_A
                System.out.println("It is a valid DNS request\n");
                if (hosts.isKeyed()){           //dns-nameserver style
                    return checkTable(query);
                } else if (!hosts.isKeyed()){   //load balancer style
                    return checkList(query);
                }
            } else {                            //Request type is not handled;
                System.out.println("Not implemented");
                response  = query.get_error(2);
                responsePacket = response.get_response_packet();
                return responsePacket;
            }

            //should not be reached 
            return null;
        }

        /**
         * Check and form a dns response for dns style requests.
         * @param query
         * @return the_answer
         */
        private DatagramPacket checkTable(DNSRequest query){
            DatagramPacket responsePacket;
            DNSResponse response;
            if(hosts.get_IP(query.domain_name.toString().toLowerCase())!=null){
                //if link list is populated and domain exists, send an IP
                System.out.println("Domain requested is good");
                response = query.get_response(hosts);	
                responsePacket = response.get_response_packet();
                return responsePacket;
            } else {
                //return error packet: domain not serviced
                System.out.println("Domain not serviced");
                response = query.get_error(1);	
                responsePacket = response.get_response_packet();
                return responsePacket;
            }
        }

        /**
         * Check and form a dns response for load-balancer style requests.
         * @param the_request
         * @return the_answer
         */
        private DatagramPacket checkList(DNSRequest query){
            DatagramPacket responsePacket;
            DNSResponse response;
            if(query.domain_name.toString().equalsIgnoreCase(hosts.getDomainName())){
                //System.out.println("Domain requested is good");
                //if link list is populated and domain exists, send an IP
                if(!hosts.isEmpty()){
                    //System.out.println("List is Not Empty");
                    response = query.get_response(hosts);	
                    responsePacket = response.get_response_packet();
                    return responsePacket;
                }else{
                    System.out.println("List is Empty");
                    //return error packet: no available servers
                    response = query.get_error(0);	
                    responsePacket = response.get_response_packet();
                    return responsePacket;
                }
            } else {
                //System.out.println("Domain not serviced");
                //return error packet: domain not serviced
                response = query.get_error(1);	
                responsePacket = response.get_response_packet();
                return responsePacket;
            }
        }

        /**
         * Create the response packet for a load-balance message.
         * @param the_lb_packet 
         * @return the_response_packet
         */
        private DatagramPacket decide_lb(DatagramPacket packet){
            boolean our_server = true;
            byte[] packet_key = packet.getData();
	    byte[] FREE_KEY = hosts.getMagic();
            InetAddress[] IP = hosts.getLB_IP();

            //Check magic/key
            for (int i = 0; i < FREE_KEY.length; i++){
                if(packet_key[i]!=FREE_KEY[i]){
                    System.out.println("Server supplied bad key");
                    return null;
                }
            }

            //Now check if the IP is expected
            boolean good_IP = false;
            String this_IP = packet.getAddress().getHostAddress();
            for (int i = 0; i < IP.length; i++){
                //System.out.println("ServerIP: " + IP[i].getHostAddress() + "\nPacketIP: " + this_IP); 
                if(IP[i].getHostAddress().equalsIgnoreCase(this_IP)){
                    good_IP = true;
                    int color = Integer.parseInt(this_IP.substring(this_IP.length()-1)) -2;
                    System.out.println("FREE!" + COLORS[color] + this_IP + RESET);
                    break;
                }
            }

            if (good_IP) {
                //add IP to linked list
                hosts.add_host(packet.getAddress());
                //arbitrary return value
                byte[] msg = "DONE!".getBytes();
                return new DatagramPacket(msg, msg.length, packet.getAddress(), packet.getPort());
            }else {
                System.out.println("Server IP not registered");
                return null;
            }
        }

        /**
         * Set the magic value for the load-balancer servers.
         * @param the_magic_phrase
         */ 
        public void setMagic(String magic){
            hosts.setMagic(magic);
        }        
}

