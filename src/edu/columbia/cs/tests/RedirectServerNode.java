package edu.columbia.cs.tests;

import java.net.HttpURLConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import java.io.IOException;

import java.util.Random;
/**
 * A HTML page node that contains only HTML links to its children.
 */
public class RedirectServerNode extends TestServerNode
{
	/** the content type for an HTML node */
	protected static final String CONTENT_TYPE_HTML = "text/html";
        
        private static final String LOCATION_KEY = "Location";
	
        /** the template for generating an HTML link */
	private static final String
	LINK_TEMPLATE = "<a href=%1$s>\n%1$s\n</a><br>";
	
	/** the template for the entire HTML body */
	private static final String
	HTML_TEMPLATE = "<html><head></head><body>\n%s\n</body></html>";
        
        /** whether this node is a redirect or an external link */
        private final boolean is_redirect;
        /** the url this redirects to */
        private final String location;
        
        /**
	 * Generate an HTML content.
	 * @param child_url	the URL reference to the child
	 * @return		an HTML-formatted link to the child
	 */
	private static String generateContent(String child_url)
	{
		return String.format(HTML_TEMPLATE, String.format(LINK_TEMPLATE, child_url));
	}

        /**
	 * Return true if the redirect address is same as "child" server
	 * @return	{@literal true} iff the child is in the same server,
	 *		and a child of this node
         * @override
	 */
	public boolean checkChild(TestServerNode child) {
            return true;/*   
            if (child.getServerName().equals(location)) {
                return true;
            } else {
                return false;
            }*/
	}

        /**
         * @return      one of four redirect statuses selected at random
         */
        private static int getRedirectStatus() {
            int[] statuses = {HttpURLConnection.HTTP_MOVED_PERM,
                HttpURLConnection.HTTP_MOVED_TEMP, 
                HttpURLConnection.HTTP_SEE_OTHER,
                HttpURLConnection.HTTP_MULT_CHOICE};
            Random rand = new Random();
            int select = rand.nextInt();
            select = (select < 0) ? (select * -1) % statuses.length : select % statuses.length;

            return statuses[select];
        }
        
        protected void sendContents(HttpExchange exchange, int status, String content_type, byte[] contents) {
		Headers headers = exchange.getResponseHeaders();
                
                try {
                System.out.println("DOES THIS OCCUR2");
                
                if (is_redirect) {
                    headers.add(LOCATION_KEY, location);
                }

                TestServer.sendContents(exchange, status, content_type,
					contents);
                } catch (IOException io) {
                    io.printStackTrace();
                }
        }
            
	/**
	 * @param server_name	the name of the server containing this node,
	 *			as required by the super class
	 * @param path		the absolute path to this node,
	 *			as required by the super class
	 * @param query		the query for this node's URL,
	 *			as required by the super class
	 * @param fragment	the fragment for this node's URL,
	 *			as required by the super class
	 * @param rdr_location	the url this node redirects to
         * @param is_redirect   true iff this is a redirect node 
         *                      and not just a link to new domain
	 * @param children	any previously-known child nodes,
	 *			as required by the super class
	 */
	public RedirectServerNode(String server_name, String path, String query,
			      String fragment, String rdr_location, boolean is_redirect,
			      TestServerNode ... children)
	{
		super(server_name, path, query, fragment, 
                        (is_redirect ? getRedirectStatus() : HttpURLConnection.HTTP_ACCEPTED),
                        CONTENT_TYPE_HTML, 
                        (is_redirect ? "".getBytes() : generateContent(rdr_location).getBytes()), 
                        children);
	        this.is_redirect = is_redirect;
                location = rdr_location;
        }

}
