package edu.columbia.cs.tests;

import java.util.TreeMap;
import java.util.LinkedList;

import java.net.URI;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * A single resource in a server.
 */
public class TestServerNode
{
	/**
	 * the name of the server, to check if a potential child is in the same server
	 */
	private final String server_name;
	/**
	 * the path immediately following the server name in the URL,
	 * ending at "?" or "#" if they exist
	 */
	public final String path;
	/** short ID, excluding path */
	public final String short_id;
	/** entire part of the URL after the server name */
	public final String id;
	/** the status code if this node has been correctly visited */
	private int status;
	/** the type of the content, if this node hase been correctly visited */
	private final String content_type;
	/** the content, if this node hase been correctly visited */
	private final byte[] contents;

	/**
	 * lookup table of linked-to children within this domain,
	 * indexed by the {@code id} field
	 */
	private TreeMap<String, TestServerNode> children;
	/** the server containing this node */
	private TestServer host;

	/**
	 * Append the short ID, excluding the path.
	 * @param builder	the builder to which to append the short ID.
	 * @param query		the query, which follows the path
	 * @param fragment	the fragment, which follows the query
	 */
	private static void
	buildShortID(StringBuilder builder, String query, String fragment)
	{
		if (query != null && query.length() > 0) {
			builder.append("?" + query);
		}
		if (fragment != null && fragment.length() > 0) {
			builder.append("#" + fragment);
		}
	}

	/**
	 * Generate the short ID, excluding the path.
	 * @param query		the query, which follows the path
	 * @param fragment	the fragment, which follows the query
	 * @return		the short ID, excluding the path
	 */
	public static String toShortID(String query, String fragment)
	{
		StringBuilder builder = new StringBuilder();

		buildShortID(builder, query, fragment);
		return builder.toString();
	}

	/**
	 * Generate the short ID, excluding the path.
	 * @param uri	the whole URL, containing the query and fragment
	 * @return	the short ID, excluding the path
	 */
	public static String toShortID(URI uri)
	{
		return toShortID(uri.getQuery(), uri.getFragment());
	}

	/**
	 * Format how the part of the URL after the server name would look like,
	 * given the relevant parameters of the URL.
	 * @param path		path of the URL, after the server name
	 * @param short_id	short ID, as it would appear after the path
	 * @return		a joining of the path, query and fragment
	 *			as it would appear in the URL
	 */
	public static String toID(String path, String short_id)
	{
		return path + short_id;
	}

	/**
	 * Format how the part of the URL after the server name would look like,
	 * given the relevant parameters of the URL.
	 * @param path		the path of the URL, after the server name
	 * @param query		the query of the URL, after the path
	 * @param fragment	the fragment of the URL, after the fragment
	 * @return		a joining of the path, query and fragment
	 *			as it would appear in the URL
	 */
	public static String toID(String path, String query, String fragment)
	{
		StringBuilder builder = new StringBuilder(path);

		toShortID(query, fragment);

		return builder.toString();
	}

	/**
	 * Format how the part of the URL after the server name would look like,
	 * given the relevant parameters of the URL.
	 * @param uri	the whole URL, containing the path, query and fragment
	 * @return	a joining of the path, query and fragment
	 *		as it would appear in the URL
	 */
	public static String toID(URI uri)
	{
		return toID(uri.getPath(), uri.getQuery(), uri.getFragment());
	}

	/**
	 * @param server_name	name of the server containing this node
	 * @param path		path of the URL of this node
	 * @param query		query of the URL of this node
	 * @param fragment	fragment of the URL of this node
	 * @param status	status to return upon a proper visit
	 * @param content_type	type of content to return upon a proper visit
	 * @param contents	type of content to return upon a proper visit
	 * @param children	already-known children of this node
	 */
	public TestServerNode(String server_name, String path, String query,
			      String fragment, int status,
			      String content_type, byte[] contents,
			      TestServerNode ... children)
	{
		this.server_name = server_name;
		this.path = path;
		short_id = toShortID(query, fragment);
		id = toID(path, short_id);
		this.status = status;
		this.content_type = content_type;
		this.contents = contents.clone();
		this.children = TestServer.createNodeMap(children);
	}

	/**
	 * Add a child if it has not already been added.
	 * @param child	the child to add
	 * @return	{@literal true} iff the child was not already added,
	 *		and is now added
	 */
	public boolean addChild(TestServerNode child)
	{
		if (checkChild(child)) {
			return false;
		}
		children.put(child.id, child);
		return true;
	}

	/**
	 * Set the server containing this node.
	 * @param host	the host containing this node
	 */
	protected void setHost(TestServer host)
	{
		this.host = host;
	}

	/**
	 * Check if another node is actually a child of this one.
	 * @return	{@literal true} iff the child is in the same server,
	 *		and a child of this node
	 */
	public boolean checkChild(TestServerNode child)
	{
		return server_name.equals(child.server_name) &&
		       children.containsKey(child.id);
	}

        public String getServerName() {
            return server_name;
        }       
	/**
	 * Handle a request whose path,
	 * request and fragment is handled by this node.
	 * @param exchange	the request to this node
	 */
	public void handle(HttpExchange exchange) throws IOException
	{
		/** check that the ID exactly the same */
		if (!id.equals(toID(exchange.getRequestURI()))) {
			host.handle(exchange);
			return;
		}

		/** check that the visit was at the correct times */
		if (!host.checkOrder(this, exchange)) {
			return;
		}

		/** success: send contents and record the visit as correct */
		sendContents(exchange, status, content_type,
					contents);
		host.logVisit(exchange, false);
	}

    protected void sendContents(HttpExchange exchange, int status, String content_type, byte[] contents) {
            try {    
                TestServer.sendContents(exchange, status, content_type,
					contents);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
}       
