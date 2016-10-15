package edu.columbia.cs.dns_server;
import java.net.*;
/**
 * This class represents a DNS request.
 */
class DNSRequest extends DNSEntity
{
    // constructor that get the DNS request packet
    public DNSRequest(DatagramPacket the_request_packet)
    {
        this.the_packet = new DatagramPacket(the_request_packet.getData(),
            the_request_packet.getLength(), the_request_packet.getAddress(),
            the_request_packet.getPort());

        // sets domain name as a side effect
        domain_name_length_in_bytes =
            parse_domain_name(the_request_packet.getData());

        DNS_type    = parse_type(the_request_packet.getData(),
                        domain_name_length_in_bytes);
        DNS_class   = parse_class(the_request_packet.getData(), domain_name_length_in_bytes);

        /*
        tools.report("DNSRequest constructor: domain_name = " + domain_name +
            " domain_name_length_in_bytes: " + domain_name_length_in_bytes +
            " type: " + DNS_type + " class: " + DNS_class);
        */
    }


    // Create DNS Response
    public DNSResponse get_response(HostStore host_store)
    {
        // We create a new DNSEntity instance of DNSResponse and resturn it.
        // the DNSResponse constructor is responsible to set this instance
        // to be the right response to this request
        DNSResponse the_response = new DNSResponse(this, host_store);
        return the_response;
    }
    
    /**
     * 
     * @param error_type	0 for server failure, 1 for no such domain
     * @return the_response     the DNS formatted error     
     */
    public DNSResponse get_error(int error_type){
    	DNSResponse the_response = new DNSResponse(this, error_type);
        return the_response;
    }

				
    // This function parses the requested domain name out of the request buffer
    // the domain (member data) is updated as a side effect of this function
    private short parse_domain_name(byte[] the_request_buffer)
    {
        domain_name = new StringBuffer();
        short curr_word_length_byte = BYTES_IN_HEADER;

        // TEST
//      tools.report("First");
//      for (int jByte = curr_word_length_byte; jByte <= 255; ++jByte)
//      {
//          if (the_request_buffer[jByte] < 50)
//			{
//                  tools.report(jByte + " " + (byte)the_request_buffer[jByte]); 
//			}
//          else 
//			{
//                 tools.report(jByte + " " + (char)the_request_buffer[jByte]);
//			}
//      }
//      tools.report("Second");
        // END TEST

        while (the_request_buffer[curr_word_length_byte] > 0 && curr_word_length_byte < 32)
        {
			String curr_word = new String(the_request_buffer, curr_word_length_byte + 1, the_request_buffer[curr_word_length_byte]);

//          for (int jByte = 1; jByte <= the_request_buffer[curr_word_length_byte]; ++jByte) 
//          {
//              domain_name.append((char)the_request_buffer[curr_word_length_byte + jByte]);
//              tools.report(jByte + " " + (char)the_request_buffer[curr_word_length_byte + jByte]);
//          }

            curr_word_length_byte += (the_request_buffer[curr_word_length_byte] + 1);

            domain_name.append(curr_word.toLowerCase());
            
            if (the_request_buffer[curr_word_length_byte] > 0 && curr_word_length_byte < 32)
            {
                domain_name.append(".");
            }
        }


        return (short)(curr_word_length_byte - BYTES_IN_HEADER);
    }

    private short parse_type(byte[] the_request_buffer, short the_domain_name_length)
    {
        int the_type = 0;

        the_type =  the_request_buffer[BYTES_IN_HEADER + the_domain_name_length + 2] & 0xFF;
        the_type |= ((the_request_buffer[BYTES_IN_HEADER + the_domain_name_length + 1] << 8) &
                        0xFF00);
        return (short)the_type;
    }

    private short parse_class(byte[] the_request_buffer, short the_domain_name_length)
    {
        int the_class = 0;

        the_class =  the_request_buffer[BYTES_IN_HEADER + the_domain_name_length + 4] & 0xFF;
        the_class |= ((the_request_buffer[BYTES_IN_HEADER + the_domain_name_length + 3] << 8) &
                        0xFF00);
        return (short)the_class;
    }

}
