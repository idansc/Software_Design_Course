package il.ac.technion.cs.sd.lib.clientserver;

import java.util.ArrayList;

// Represents a message one host is sending another.
public class MessageData
{

	// Address of the host sending this data packet.
	public String fromAddress;
	
	// Represent a user-defined packet type.
	public String messageType;
	
	// The data held by this packet.
	public ArrayList<String> data;
	
	
	public MessageData(String fromAddress, String packetType,
			ArrayList<String> data) {
		this.fromAddress = fromAddress;
		this.messageType = packetType;
		this.data = data;
	}
	
	
	/* A magic-string representing the messageType of a MessageData indicating
	 * the server finished it's task.
	 */
	public static final String serverTaskEndedPacketType 
		= "9PEdT1SJeR9waQajDF6k"; 
}

