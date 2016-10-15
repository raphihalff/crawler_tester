package edu.columbia.cs.dns_server;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * Tests whether DNSServer is listening at port 53 and if InetAddress.getByName() succeeds.
 */
public class DNSTest {
        
    private final static String DOMAIN = "www.IDSLAB.edu";
    /** 
    * Makes a standard dns request using java functions
    * @param args
    **/
    public static void main(String[] args) {
        //Supposedly to set this program as DNS namesever, 
        //but ineffective (and port 53 is priveledged)
        System.setProperty("sun.net.spi.nameservice.nameservers", "127.0.0.1");
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        int i = 0;
        try {
            Thread.sleep(2000);
        } catch (Exception e){
        }
        while (i<15){
            try {
                    i++;
                    InetAddress answer = InetAddress.getByName(DOMAIN);
                    System.out.println("ANSWER: " + answer.getHostAddress());
                    Thread.sleep(700);
            } catch (UnknownHostException e) {
                    System.out.println("ERROR: NO IP RETURNED");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
   
