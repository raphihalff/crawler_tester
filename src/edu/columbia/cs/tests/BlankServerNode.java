package edu.columbia.cs.tests;

import java.net.HttpURLConnection;

/**
 * A HTML page node that contains only HTML links to its children.
 */
public class BlankServerNode extends TestServerNode
{
	/** the content type for an HTML node */
	protected static final String CONTENT_TYPE_HTML = "text/html";

	/** the template for the entire HTML body */
	private static final String
	HTML_TEMPLATE = "<html><head></head><body>\n%s\n</body></html>";

	/** the template for the broken HTML body */
	private static final String
	BROKEN_HTML_TEMPLATE = "<html><head></head><body>\n%s\n";

        protected static String generateContent(String body, boolean is_broken)
	{
                
		return (is_broken) ? String.format(BROKEN_HTML_TEMPLATE, body) : String.format(HTML_TEMPLATE, body);
	}
	
        /**
	 * Check if another node is actually a child of this one.
	 * @return	{@literal true} iff the child is in the same server,
	 *		and a child of this node
         * @override
	 */
	public boolean checkChild(TestServerNode child) {
		return true;
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
	 * @param body 	        the custom html body or null for empty,
	 *			as required by the super class
         * @param is_broken     true if html syntax should be incorrect        
	 * @param children	any previously-known child nodes,
	 *			as required by the super class
	 */
	public BlankServerNode(String server_name, String path, String query,
			      String fragment, String body, boolean is_broken,
			      TestServerNode ... children)
	{
		super(server_name, path, query, fragment, HttpURLConnection.HTTP_ACCEPTED,
		      CONTENT_TYPE_HTML, (body == null) ? null : generateContent(body, is_broken).getBytes(), children);
	}
}
