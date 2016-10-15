package edu.columbia.cs.dns_server;
import java.net.*;
/**
 * The abstract class that holds the common DNS request and response values
 * and general constructor.
 */ 
abstract class DNSEntity
{
    protected static final short    DNS_RESPONSE_DATA_LENGTH    	= (short)0x0004;
    protected static final short    OK_RESPONSE_FLAGS           	= (short)0x8500;
    protected static final short    ERROR_RESPONSE_FLAGS_NO_DOMAIN      = (short)0x8503;
    protected static final short    ERROR_RESPONSE_FLAGS_SERV_FAIL      = (short)0x8502;
    protected static final short    ERROR_RESPONSE_FLAGS_NOT_IMP        = (short)0x8504;
    protected static final short    CLASS_INTERNET              	= (short)0x0001;
    protected static final short    TYPE_A                     	 	= (short)0x0001;
    protected static final short    TYPE_ANY                    	= (short)0x00ff;
    protected static final short    TYPE_TXT    		        = (short)0x0010;
    protected static final int      RESPONSE_TTL               	 	= 0x00000000;
    protected static final short    BYTES_IN_HEADER             	= 12;

    // The requested domain name
    protected StringBuffer          domain_name;
    // The requested domain address
    protected InetAddress           domain_address;

    protected short                 domain_name_length_in_bytes;
    // The datagram packet (that got from DNS client or sent back to it)
    protected DatagramPacket        the_packet;
    protected short                 DNS_type;
    protected short                 DNS_class;

    // constructor
    public DNSEntity() {};

    // copy constructor
    public DNSEntity(DNSEntity the_other)
    {
        // These Are not due to change -> reference is enough
        domain_name                 = the_other.domain_name;
        domain_address              = the_other.domain_address;
        domain_name_length_in_bytes = the_other.domain_name_length_in_bytes;
        DNS_type                    = the_other.DNS_type;
        DNS_class                   = the_other.DNS_class;

        // This is due to changes -> we need to copy it
        the_packet = new DatagramPacket(the_other.the_packet.getData(),
            the_other.the_packet.getLength(), the_other.the_packet.getAddress(),
            the_other.the_packet.getPort());
    }
}
