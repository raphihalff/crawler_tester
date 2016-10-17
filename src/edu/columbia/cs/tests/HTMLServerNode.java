package edu.columbia.cs.tests;

import java.net.HttpURLConnection;

/**
 * A HTML page node that contains only HTML links to its children.
 */
public class HTMLServerNode extends TestServerNode
{
	/** the content type for an HTML node */
	private static final String CONTENT_TYPE_HTML = "text/html";

	/** the template for generating an HTML link */
	private static final String
	LINK_TEMPLATE = "<a href=%1$s>\n%1$s\n</a><br>";

	/**
	 * Generate an HTTML link to a child.
	 * @param child_url	the URL reference to the child
	 * @return		an HTML-formatted link to the child
	 */
	private static String generateLink(String child_url)
	{
		return String.format(LINK_TEMPLATE, child_url);
	}

	/** the template for the entire HTML body */
	private static final String
	HTML_TEMPLATE = "<html><head></head><body>\n%s\n</body></html>";

	private static String generateContent(String[] child_urls)
	{
		StringBuilder links_builder = new StringBuilder();

		for (String child_url : child_urls) {
			links_builder.append(generateLink(child_url));
		}

		return String.format(HTML_TEMPLATE, links_builder.toString());
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
	 * @param child_urls	the pre-determined list of URLs to the children,
	 *			as required by the super class
	 * @param children	any previously-known child nodes,
	 *			as required by the super class
	 */
	public HTMLServerNode(String server_name, String path, String query,
			      String fragment, String[] child_urls,
			      TestServerNode ... children)
	{
		super(server_name, path, query, fragment, HttpURLConnection.HTTP_ACCEPTED,
		      CONTENT_TYPE_HTML, generateContent(child_urls).getBytes(), children);
	}
}
