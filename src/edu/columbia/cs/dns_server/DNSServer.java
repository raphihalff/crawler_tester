package edu.columbia.cs.dns_server;
import java.net.*;
import java.io.IOException;

/**
 * 
 * Server establishes one UDP socket (ideally on port 53) to listen to DNS requests.
 * Responds with a DNS response containing the IP of an available server, error packet otherwise.
 *
 * The default setting uses a list (HostList) as the HostStore, with one hostname ("www.IDSLAB.edu")
 * and five local IPs (127.0.0.2-6, port 53). Note that in non-Linux systems you may have to reconfigure some
 * things in order to allow multiple loop-back addresses. 
 * To run in default: java DNSServer   
 *
 * To pass your own unique hostname and one or more IPs (load-balancing mode, still with HostList) and a specific port run as:
 * java DNSServer port hostname IP...
 * Example: java DNSServer 53 www.example.com 127.0.0.1 127.0.0.2 127.0.0.3
 *
 * To run the server with HostTable and several hostnames and their corresponding IPs, run as:
 * java DNSServer -s port [hostname IP]...
 * Example: java DNSServer -s 53 www.example1.com 127.0.0.1 www.example2.org 127.0.0.2
 *
 * Note: The magic word used by servers in the load-balancing scenario is specified by LoadServer, not by DNSServer. 
 *
 * Using authbind:
 * To set up a socket on port 53 on linux systems use authbind.
 * First install authbind. Then insure there is an executable /etc/authbind/byport/53. Lastly run DNSServer
 * as directed above but preficed with authbind (ie. authbind java DNSServer).
 *
 * Sources:
 * 1) Java Practices  Reading and Writing Binary Files.Accessed February 19, 2016. 
 * http://www.javapractices.com/topic/TopicAction.do?Id=245.
 * 2) slackhack. How to Set a Custom DNS Server with Java System Properties.Slackhacker, July 21, 2010. 
 * http://slackhacker.com/2010/07/21/how-to-set-a-custom-dns-server-with-java-system-properties/.
 * 3) Accessed February 19, 2016. 
 * http://www.cs.technion.ac.il/Labs/Lccn/lab_tools/dns/dnsserver.java.
 * 
 * @author Raphi 
 * 
 */
class DNSServer
{
        private static String[] domain;
        private static InetAddress[] IP;
        private static int DNS_PORT; //idealy the priveleged port 53, (use authbind on linux)
        private final static String USAGE = "Usage: java DNSServer" + 
                                            "[port hostname IP...] | [-s port [hostname IP]...]" +
                                            "\nFor more info see the comment at the start of DNSServer.java";    
	/**
	 * Creates a UDP socket and listens for DNS queries, sending appropriate responses.
	 * @param  args  [port hostname IP...] [-s port [hostname IP]...]
	 */
	public static void main(String args[])
	{
                Decider decider;

                if (args.length == 0){
                    //default
                    DNS_PORT = 53;
                    domain = new String[1];
                    domain[0] = "www.IDSLAB.edu";
                    IP = new InetAddress[5];
                    for (int i = 0; i < IP.length; i++){
                        try {
                            IP[i] = InetAddress.getByName("127.0.0." + (i + 2));
                        } catch (UnknownHostException e){
                            System.err.println("Problem getting default local IPs");
                            System.exit(1);
                        }
                    } 
                    decider = new Decider(IP, domain[0]);

                } else if (args[0].equalsIgnoreCase("-s")){
                    //static table mode
                    if (args.length < 4 || (args.length % 2) > 0){
                        System.err.println(USAGE);
                        System.exit(1);
                    }

                    DNS_PORT = Integer.parseInt(args[1]);
                    domain = new String[(args.length - 2)/2];
                    IP = new InetAddress[(args.length - 2)/2];
                    int count = 0;
                    for (int i = 2; i < args.length - 1; i = i + 2){
                        domain[count] = args[i];
                        try {
                            IP[count] = InetAddress.getByName(args[i+1]);
                        } catch (UnknownHostException e){
                            System.err.println("Problem getting default local IPs");
                            System.exit(1);
                        }
                        count++;
                    }
                    decider = new Decider(IP, domain);
                } else {
                    //static list mode
                    if (args.length < 3){
                        System.err.println(USAGE);
                        System.exit(1);
                    }
                    DNS_PORT = Integer.parseInt(args[0]);
                    domain = new String[1];
                    domain[0] = args[1];
                    IP = new InetAddress[args.length - 2];
                    int count = 0;
                    for (int i = 2; i < args.length; i++){
                        try{
                            IP[count++] = InetAddress.getByName(args[i]);
                        } catch (UnknownHostException e) {
                            System.err.println("Problem getting default local IPs");
                            System.exit(1);
                        }
                    }
                    decider = new Decider(IP, domain[0]);
                }
		

		//Socket listening for DNS requests
                DatagramSocket dnsSocket = null;
                try {
                    dnsSocket = new DatagramSocket(DNS_PORT);
                    //System.out.println("DNS Listening on: " + dnsSocket.getLocalAddress().getHostAddress() + ":" + DNS_PORT);
                } catch (SocketException e) {
                    System.err.println("Failed to establish DNS socket.");
                    System.exit(1);
                }
		byte[] dnsQuery = new byte[1024];//the domain name query 
		
		//listening
		while(true)
		{
			DatagramPacket dnsQPacket = new DatagramPacket(dnsQuery, dnsQuery.length);
                        try {
                            dnsSocket.receive(dnsQPacket);
                            //the 0 arg signifies a DNS request, and not a load balance message
                            DatagramPacket dnsResponse = decider.decide(dnsQPacket, 0);
                            if (dnsResponse != null){//response was succesfully formulated
                                //System.out.println("Response sent");
                                dnsSocket.send(dnsResponse);
                            } else {
				System.err.println("Failed to formulate reponse. " + 
                                        "Check that the Decider is treating this as a DNS request " + 
                                        "and not a load-balance message.");
                            }
                        } catch (IOException e) {
                            System.err.println("Problem with DNS socket");
                            //e.printStackTrace();
                            System.exit(1);
                        }
		}
	}
} 
