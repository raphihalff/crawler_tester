package edu.columbia.cs.tests;

import java.util.Calendar;
import java.net.URI;

/**
 * A visit record in the log.
 */
public class TestVisitRecord
{
	/** time the visit was recorded */
	private final Calendar visit_time;
	/** the visited URL */
	private final URI visitee;
        /** the host of the URI */
        private final String host;
	/** Was the visit wrong? */
	private final boolean have_error;

	/**
	 * @param visitee	the visited URL
         * @param host          the host on which the node was found
	 * @param have_error	Was the visit wrong?
	 */
	public
	TestVisitRecord(URI visitee, String host, boolean have_error)
	{
		this.visit_time = Calendar.getInstance();
		this.visitee = visitee;
                this.host = host;
		this.have_error = have_error;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		return String.format("%s: Visited %s; %s", visit_time.getTime(),
				     host + visitee, have_error ? "invalid" : "valid");
	}

        public String getURL() {
            return host + visitee;
        }

        public boolean hasError() {
            return have_error;
        }
}
