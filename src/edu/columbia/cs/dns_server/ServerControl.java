package edu.columbia.cs.dns_server;

/**
 * Begins two servers, one on port 53 for DNS requests, 
 * one on port 20001 for storage availability (send "I am free!").
 */
public class ServerControl extends Thread {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Thread t1 = new Thread(new Runnable() {
		    public void run() {
		        try {
                                        Thread.sleep(1000);
					LoadServer.main(new String[0]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
                                }
		    }
		});
		
		Thread t2 = new Thread(new Runnable() {
		    public void run() {
		        try {
					DNSServer.main(new String[0]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});

		t2.start();
		t1.start();
	}

}
