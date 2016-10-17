package edu.columbia.cs.tests;

import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.LinkedList;

import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.URI;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.util.Calendar;

import java.net.UnknownHostException;
import java.io.IOException;

/**
 * A test server that decides whether a visit is correct or not,
 * and responds based on stored nodes corresponding to paths.
 */
public class TestServer implements HttpHandler
{
	/** the host name of the server */
	public final String server_name;
	/** the loop-back address of this server */
	public final InetSocketAddress local_address;
	/** lookup table for all possible nodes by their {@code id} field */
	private final TreeMap<String, TestServerNode> nodes;
	/** per-path lookup table of HttpHandler objects for nodes */
	private final TreeMap<String, TestHandler> handlers;
	/**
	 * lookup table for all nodes
	 * that can be visited immediately after visiting, /robots.txt,
	 * indexed by their {@code id} field
	 */
	private final TreeMap<String, TestServerNode> starters;
	/** the contents of /robots.txt */
	private final byte[] robots_bytes;

	/** stack containing current chain of visits */
	private LinkedList<TestServerNode> visit_stack;
	/** the server handling the requests */
	private HttpServer server;
	/**
	 * {@code HttpContext} instances that have been added to {@code server},
	 * and need to be removed in {@code stop()}
	 */
	private HashSet<HttpContext> contexts;
	/** Has any mistake been detected? */
	private boolean found_mistake;
	/** the log of visits */
	private LinkedList<TestVisitRecord> visits;
	/** Has /robots.txt been visited since server start? */
	private boolean visited_robots;

	/** the port used for all test servers (their IP addresses will vary) */
	private static final int WEB_PORT = 8080;

	/**
	 * Generate a loopback address on the common port.
	 * @param local_id	bottom byte of IP address
	 * @return		address on common port and loopback address
	 *			{@code 127.0.0.local_id}
	 */
	protected static InetSocketAddress getLocalAddress(byte local_id)
	{
		byte[] addr = new byte[]{127, 0, 0, local_id};
		try {
			InetAddress host_addr = InetAddress.getByAddress(addr);
			return new InetSocketAddress(host_addr, WEB_PORT);
		} catch (UnknownHostException uhe) {
			System.err.println(addr +
					   " is not of the right length " +
					   "for a network address: " + uhe);
			return null;
		}
	}

	/**
	 * Create a lookup table for nodes indexed by their {@code id} fields.
	 * @param	nodes	a flat list of nodes
	 * @return	a map, with {@code TestServerNode} objects as values,
	 *		and their {@code id} fields as keys
	 */
	protected static TreeMap<String, TestServerNode>
	createNodeMap(TestServerNode[] nodes)
	{
		TreeMap<String, TestServerNode>
		node_map = new TreeMap<String, TestServerNode>();

		for (TestServerNode node : nodes) {
			node_map.put(node.id, node);
		}

		return node_map;
	}

	/**
	 * per-path handler of requests for TestServerNode objects
	 */
	private class TestHandler implements HttpHandler
	{
		/** handled path */
		public final String path;
		/**
		 * nodes to which to forward the request,
		 * based on the {@code short_id} field
		 */
		public final TreeMap<String, TestServerNode> nodes;

		/**
		 * @param path	handled_path
		 */
		public TestHandler(String path)
		{
			this.path = path;
			nodes = new TreeMap<String, TestServerNode>();
		}

		/**
		 * Add a node to handle, if it falls in this path,
		 * and no node with the same {@code short_id}
		 * has already been added.
		 * @param node	the node to add
		 * @return	{@literal true} iff the node was added
		 */
		public boolean addNode(TestServerNode node)
		{
			if (path.equals(node.path)) {
				TestServerNode
				old_node = nodes.get(node.short_id);

				if (old_node == null) {
					nodes.put(node.short_id, node);
					return true;
				}
			}
			return false;
		}

		@Override
		/**
		 * Check the request, and forward it to the appropriate node.
		 * @param exchange	the request to handle
		 * @throws IOException	if handling the request failed
		 */
		public void handle(HttpExchange exchange) throws IOException
		{
			URI uri = exchange.getRequestURI();

			/** check that the path matches */
			if (path.equals(uri.getPath())) {
				/** check that there is a handling node */
				String short_id = TestServerNode.toShortID(uri);
				TestServerNode node = nodes.get(short_id);

				if (short_id != null) {
					node.handle(exchange);
					return;
				}
			}

			TestServer.this.handle(exchange);
		}

		/**
		 * Set the host server for all contained nodes.
		 * @param host	the value to pass into {@code setHost()},
		 *		for all contained nodes
		 */
		public void setHost(TestServer host)
		{
			for (TestServerNode node : nodes.values()) {
				node.setHost(host);
			}
		}
	}

	/**
	 * Create a lookup table for handlers of nodes indexed by their paths.
	 * @param	nodes	a flat list of nodes to add to the handlers
	 * @return	a map, with {@code TestHandler} objects as values,
	 *		and their {@code path} fields as keys
	 */
	private TreeMap<String, TestHandler>
	createHandlers(TestServerNode[] nodes)
	{
		TreeMap<String, TestHandler>
		handlers = new TreeMap<String, TestHandler>();

		for (TestServerNode node : nodes) {
			String path = node.path;
			TestHandler handler = handlers.get(path);

			if (handler == null) {
				handler = new TestHandler(path);
				handlers.put(path, handler);
			}

			if (!handler.addNode(node)) {
				System.err
				      .printf("Warning: %s already added.\n",
					      node.short_id);
			}
		}

		return handlers;
	}

	/**
	 * @param server_name	the host name of the server
	 * @param local_id	the bottom byte of the local IP address.
	 *			ie. The server will be listening on
	 *			{@code 127.0.0.local_id:8080}
	 * @param nodes		all the nodes accessible in this server
	 * @param starters	all the nodes from which the crawler can start
	 * @param robots_txt	the contents of /robots.txt
	 */
	public TestServer(String server_name, byte local_id,
			  TestServerNode[] nodes, TestServerNode[] starters,
			  String robots_txt)
	{
		this.server_name = server_name;
		this.local_address = getLocalAddress(local_id);
		this.nodes = createNodeMap(nodes);
		this.handlers = createHandlers(nodes);
		this.starters = createNodeMap(starters);
		this.robots_bytes = robots_txt.getBytes();

		visit_stack = new LinkedList<TestServerNode>();
		server = null;
		visited_robots = false;
	}

	/** the name of the HTTP header describing the data content type */
	private static final String CONTENT_TYPE_KEY = "Content-Type";

	/**
	 * Send data contents in the response.
	 * @param exchange	request to which to respond
	 * @param status	status code in the response
	 * @param content_type	{@code Content-Type} header value for the data
	 * @param contents	the actual contents
	 * @throws IOException	if writing the data failed
	 */
	protected static void
	sendContents(HttpExchange exchange, int status, String content_type,
		     byte[] contents) throws IOException
	{
		boolean have_contents = contents != null;
		int length = have_contents ? contents.length : 0;
		Headers headers;
		OutputStream response_body;

		headers = exchange.getResponseHeaders();

		if (have_contents) {
			headers.add(CONTENT_TYPE_KEY, content_type);
		}
		exchange.sendResponseHeaders(status, length);

		response_body = exchange.getResponseBody();
		if (have_contents) {
			response_body.write(contents);
		}
		response_body.close();
	}

	/** path to the robots file, which should be visited first */
	private static final String ROBOTS_PATH = "/robots.txt";
	/** content type for the robots file */
	private static final String ROBOTS_TYPE = "text/plain";

	@Override
	/**
	 * Handle for all paths without nodes,
	 * but only allow robots.txt
	 * if it hasn't been visited since the server started
	 * @throws IOException	if responding to the request failed
	 */
	public void handle(HttpExchange exchange) throws IOException
	{
		boolean correct;

		correct = !visited_robots &&
			  ROBOTS_PATH.equals(exchange.getRequestURI()
						     .getPath());
		if (correct) {
			visited_robots = true;
			sendContents(exchange, HttpURLConnection.HTTP_OK,
				     ROBOTS_TYPE, robots_bytes);
		}

		logVisit(exchange, !correct);
	}

	/**
	 * Remove all the handlers.
	 */
	private void cleanContexts()
	{
		for (HttpContext context : contexts) {
			server.removeContext(context);
		}
	}

	/**
	 * Stop and clean up the server.
	 */
	public void stop()
	{
		if (server == null) {
			return;
		}

		cleanContexts();

		server.stop(0);
	}

	/** the catch-all path handled by this server */
	private static final String DEFAULT_PATH = "/";

	/**
	 * Try to start the server and the handlers for each path.
	 * @return	{@code true} iff the server started successfully.
	 */
	public boolean start()
	{
		boolean need_default = true;
		int n_required_handlers = nodes.size();

		try {
			server = HttpServer.create();
		} catch (IOException ioe) {
			System.err.println("Unable to create server: " + ioe);
			return false;
		}

		contexts = new HashSet<HttpContext>();

		for (TestHandler handler : handlers.values()) {
			String path = handler.path;

			try {
				HttpContext
				context = server.createContext(path, handler);

				if (need_default && DEFAULT_PATH.equals(path)) {
					need_default = false;
				}
				handler.setHost(this);
				contexts.add(context);
			} catch (IllegalArgumentException iae) {
				System.err.printf("Unable to " +
						  "add handler for %s: %s\n",
						  path, iae);
			}
		}

		if (contexts.size() == n_required_handlers) {
			n_required_handlers++;
			try {
				HttpContext
				context = server.createContext(DEFAULT_PATH,
							       this);
				contexts.add(context);
				try {
					server.bind(local_address, 1);
					found_mistake = false;
					visits =
					new LinkedList<TestVisitRecord>();
					server.start();
					visited_robots = false;
					return true;
				} catch (IOException ioe) {
					System.err.println("Unable to " +
							   "bind server: " +
							   ioe);
				}
			} catch (IllegalArgumentException iae) {
				System.err.printf("Unable to " +
						  "add default handler: %s\n",
						  iae);
			}
		}

		cleanContexts();

		return false;
	}

	/*
	 * Check if a node was visited in the correct
	 * (depth-first from {@code starters}) order.
	 * If not, log the error and return {@literal false},
	 * telling the caller to not proceed.
	 * @param node		the node that was just visited
	 * @param exchange	the request handled by the node
	 * @return
	 */
	public boolean checkOrder(TestServerNode node, HttpExchange exchange)
	throws IOException
	{
		while (visit_stack.size() > 0 &&
		       !visit_stack.peek().checkChild(node)) {
			visit_stack.pop();
		}

		if (visit_stack.size() == 0) {
			boolean correct, have_error;

			correct = visited_robots &&
				  starters.containsKey(node.id);

			have_error = !correct;
			if (have_error) {
				logVisit(exchange, have_error);
				return false;
			}
		}

		visit_stack.push(node);
		return true;
	}

	/**
	 * Log a visit, giving an HTTP not-found status, if it was erroneous.
	 * @param exchange	the request of the visit
	 * @param have_error	Was the visit erroneous?
	 * @throws IOException	if writing an error response failed.
	 */
	protected void logVisit(HttpExchange exchange, boolean have_error)
	throws IOException
	{
		if (have_error) {
			sendContents(exchange, HttpURLConnection.HTTP_NOT_FOUND,
				     null, null);
			System.err.println("Invalid URL, " +
					   exchange.getRequestURI());
			found_mistake = true;
		}
		visits.add(new TestVisitRecord(exchange.getRequestURI(),
					       have_error));
	}

	/**
	 * Return the log of visits.
	 * @return	list of visits, in array form
	 */
	public TestVisitRecord[] getVisits()
	{
		TestVisitRecord[]
		visits_array = new TestVisitRecord[visits.size()];
		int visit_i = 0;

		for (TestVisitRecord visit : visits) {
			visits_array[visit_i++] = visit;
		}

		return visits_array;
	}

	/**
	 * Check if any of the recorded visits were ever erroneous.
	 * @return	{@code true} iff there exists a visit that was erroneous
	 */
	public boolean getFoundMistake()
	{
		return found_mistake;
	}
}
