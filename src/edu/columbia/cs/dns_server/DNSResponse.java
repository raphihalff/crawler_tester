package edu.columbia.cs.dns_server;
import java.net.*;

/**
 * This class represents a DNS response.
 * It references a hostname store that extends HostStore.
 * Either HostTable or HostList may be used, they will be 
 * automatically differentiated.
 */
class DNSResponse extends DNSEntity
{
	private boolean ok_flag = false;

	/**
         * This constructor creates an DNS response to the given request 
	 * @param the_request 	the DNS query this is in response to	
	 * @param host_store      the HostStore structure that holds the DNS information          
	 */
	public DNSResponse(DNSRequest the_request, HostStore host_store)
	{
		// uses copy constructor of DNSEntity
		super(the_request);

		// Looks in the hosts table to find this domain name
                // If it is a table it will search by the hostname key
                if (host_store.isKeyed()){
                    domain_address = host_store.get_IP(the_request.domain_name.toString().toLowerCase());    
                } else {
                    domain_address = host_store.get_IP();
                }

		// We do not answer if we don't know the answer
		if (domain_address != null &&
				(DNS_type == TYPE_A || DNS_type == TYPE_ANY) &&
				DNS_class == CLASS_INTERNET)
		{
			set_response_flags(OK_RESPONSE_FLAGS);

			set_number_of_answers_rr((short)1); // There Is Only One Answer

			int answer_domain_name_byte_pos = the_packet.getLength();

			int type_byte_pos = copy_domain_name_to_answer_part(answer_domain_name_byte_pos);
			//tools.report("answer_domain_name_byte_pos: " + answer_domain_name_byte_pos +
			//		"   type byte pos : " + type_byte_pos);

			// the response type is type A = Normal
			set_response_type(type_byte_pos, TYPE_A);

			// The response class is class 1 = Internet
			set_response_class(type_byte_pos, CLASS_INTERNET);

			// the time to live value is 86400 sec
			set_response_TTL(type_byte_pos, RESPONSE_TTL);

			// the datalength is 4 bytes in order to contain the ip address
			set_response_data_length(type_byte_pos, DNS_RESPONSE_DATA_LENGTH);

			// copy the IP address in to the answer section
			set_response_data(type_byte_pos, domain_address);

			//tools.report("before. Length= " + the_packet.getLength());
			the_packet.setLength(type_byte_pos + 13 + 1);

			ok_flag = true;

			//tools.report("after. Length= " + the_packet.getLength());
		}
	}

	/**
         * This constructor returns an error response
	 * @param the_request 	the DNS request this error is in response to
	 * @param type		0 for server failure, 1 for no such domain, else not-implemented error
	 */
	public DNSResponse(DNSRequest the_request, int type){

		// uses copy constructor of DNSEntity
		super(the_request);

		if (type == 0){
			set_response_flags(ERROR_RESPONSE_FLAGS_SERV_FAIL);
		} else if (type==1) {
			set_response_flags(ERROR_RESPONSE_FLAGS_NO_DOMAIN);
		} else {
                    set_response_flags(ERROR_RESPONSE_FLAGS_NOT_IMP);
                }

		set_number_of_answers_rr((short)0); // There Is Only One Answer

		int answer_domain_name_byte_pos = the_packet.getLength();

		int type_byte_pos = copy_domain_name_to_answer_part(answer_domain_name_byte_pos);
		//tools.report("answer_domain_name_byte_pos: " + answer_domain_name_byte_pos +
		//		"   type byte pos : " + type_byte_pos);

		// the response type is type A = Normal
		set_response_type(type_byte_pos, TYPE_A);

		// The response class is class 1 = Internet
		set_response_class(type_byte_pos, CLASS_INTERNET);

		// the time to live value is 86400 sec
		set_response_TTL(type_byte_pos, RESPONSE_TTL);

		// the datalength is 4 bytes in order to contain the ip address
		set_response_data_length(type_byte_pos, DNS_RESPONSE_DATA_LENGTH);

		//tools.report("before. Length= " + the_packet.getLength());
		the_packet.setLength(type_byte_pos + 13 + 1);

		ok_flag = false;

		//tools.report("after. Length= " + the_packet.getLength());
	}

	public DatagramPacket get_response_packet()
	{
		DatagramPacket the_response_packet =
				new DatagramPacket(
						the_packet.getData(),
						the_packet.getLength(),
						the_packet.getAddress(), the_packet.getPort());

		return the_response_packet;
	}

	public boolean is_ok()
	{
		return ok_flag;
	}

	private byte get_LS_byte(short the_short)
	{
		return (byte) (the_short & 0xFF);
	}

	private byte get_MS_byte(short the_short)
	{
		return (byte) ((the_short >>> 8) & 0xFF);
	}

	private void set_ith_byte_in_buffer(int iByte, byte new_value)
	{
		the_packet.getData()[iByte] = new_value;
	}

	private void set_short_in_buffer(int iByte, short new_value)
	{
		// might be the other way around
		set_ith_byte_in_buffer(iByte, get_MS_byte(new_value));
		set_ith_byte_in_buffer(iByte + 1, get_LS_byte(new_value));
	}

	private void set_response_flags(short the_new_flag)
	{
		set_short_in_buffer(2, the_new_flag);
	}

	private void set_number_of_answers_rr(short number_of_answers)
	{
		set_short_in_buffer(6, number_of_answers);
	}

	private void set_response_type(int type_byte_pos, short response_type)
	{
		set_short_in_buffer(type_byte_pos, response_type);
	}

	private void set_response_class(int type_byte_pos, short response_class)
	{
		set_short_in_buffer(type_byte_pos + 2, response_class);
	}

	private void set_response_TTL(int type_byte_pos, int response_TTL)
	{
		set_short_in_buffer(type_byte_pos + 4, (short)((response_TTL >>> 16) & 0xFFFF));
		set_short_in_buffer(type_byte_pos + 6, (short)(response_TTL & 0xFFFF));
	}

	private void set_response_data_length(int type_byte_pos, short response_data_length)
	{
		set_short_in_buffer(type_byte_pos + 8, response_data_length);
	}

	private int copy_domain_name_to_answer_part(int answer_domain_name_byte_pos)
	{
		for (int iByte = BYTES_IN_HEADER; iByte < BYTES_IN_HEADER + domain_name_length_in_bytes; ++iByte)
			set_ith_byte_in_buffer(answer_domain_name_byte_pos + iByte - BYTES_IN_HEADER, the_packet.getData()[iByte]);

		return (answer_domain_name_byte_pos + domain_name_length_in_bytes + 1);
	}

	// copy the IP address in to the answer section
	void set_response_data(int type_byte_pos, InetAddress domain_address)
	{
		byte[] addr = new byte[4];
		addr = domain_address.getAddress();

		set_ith_byte_in_buffer(type_byte_pos + 10, addr[0]);
		set_ith_byte_in_buffer(type_byte_pos + 11, addr[1]);
		set_ith_byte_in_buffer(type_byte_pos + 12, addr[2]);
		set_ith_byte_in_buffer(type_byte_pos + 13, addr[3]);
	}
}
