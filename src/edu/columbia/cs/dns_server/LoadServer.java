package edu.columbia.cs.dns_server;
import java.net.*;
import java.io.IOException;

/**
 * 
 * Server establishes one UDP socket to listen to availability of storage servers. 
 * Responds by placing the available server's IP/domain-name on a linked-list. 
 *
 * The default settings are port 20001 and IP 127.0.0.1 and the magic phrase (in order to indentify our servers) "I am free!"
 * To run: java LoadServer
 *
 * To specify the port, IP, and the magic phrase run as:
 * java LoadServer port IP magic
 * Example: java LoadServer 2000 localhost anything remaining args are part of magic phrase
 *
 * Sources:
 * 1) java Practices  Reading and Writing Binary Files.Accessed February 19, 2016. 
 * http://www.javapractices.com/topic/TopicAction.do?Id=245.
 * 2) slackhack. How to Set a Custom DNS Server with Java System Properties. Slackhacker, July 21, 2010. 
 * http://slackhacker.com/2010/07/21/how-to-set-a-custom-dns-server-with-java-system-properties/.
 * 3) Accessed February 19, 2016. 
 * http://www.cs.technion.ac.il/Labs/Lccn/lab_tools/dns/dnsserver.java.
 * @author Raphi 
 * 
 */
class LoadServer
{
        private static String magic;
        private static int FREE_PORT;
        private static InetAddress IP;
        private final static String USAGE = "Usage: java LoadServer [port IP magic...]" + 
                                            "\nFor more info see the comment at the start of DNSServer.java";    
	/**
	 * Creates a UDP socket and listens for notifications from host servers and updates the host list.
	 * @param args [port magic...]
	 */
	public static void main(String args[]) 
	{
	        if (args.length == 0) {
                    //default
                    FREE_PORT = 20001;
                    magic = "I am free!";
                    try {
                        IP = InetAddress.getByName("127.0.0.1");
                    } catch (UnknownHostException e){
                        System.err.println("Problem getting default local IP");
                        System.exit(1);
                    }
                } else if (args.length >= 2){
                    FREE_PORT = Integer.parseInt(args[0]);   
                    try {
                        IP = InetAddress.getByName(args[1]);
                    } catch (UnknownHostException e){
                        System.err.println("Problem getting IP");
                        System.exit(1);
                    }
                    magic = args[2];
                    for (int i = 3; i < args.length; i++){
                        magic += " " + args[i];
                    }
                } else {
                    System.err.println(USAGE);
                    System.exit(1);
                } 
		
                Decider decider =  new Decider();
                decider.setMagic(magic);
                if (decider.getLB_IP()==null){
                    System.out.println("why is it null");
                }

		//Socket listening for both type of requests
                DatagramSocket udpSocket = null;
	        try {
                    udpSocket = new DatagramSocket(FREE_PORT, IP);
                    //System.out.println("Load Listening on: " + udpSocket.getLocalAddress().getLocalHostAddress() + ":" + FREE_PORT);
                } catch (SocketException e) {
                    System.err.println("Problem with Load-balancer socket");
                    System.exit(1);
                }

		byte[] freeQuery = new byte[1024];    //the free server's query 
		
		//listening
		while(true)
		{
			//System.out.println(InetAddress.getLocalHost().getHostAddress());
			
			DatagramPacket freeQPacket = new DatagramPacket(freeQuery, freeQuery.length);
                        try {
                            udpSocket.receive(freeQPacket);
                            DatagramPacket response = decider.decide(freeQPacket, 1);
                            if(response!=null) {
                                    udpSocket.send(response);
                            } else {
				System.err.println("Failed to formulate reponse. " + 
                                                   "Check that the servers are supplying the correct magic phrase, " +
                                                   "that the servers' IPs are registered with the DNS server " + 
                                                   "and that the Decider is not treating this server as s DNS server");     
                            }
                        } catch (IOException e) {
                            System.err.println("Problem with Load-balancer socket");
                            //e.printStackTrace();
                            System.exit(1);
                        }
		}
			
	}
} 
