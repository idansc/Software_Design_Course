package il.ac.technion.cs.sd.lib.clientserver;

import java.io.ByteArrayOutputStream;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;


/* Represents a client that can send data to a server and receive answer. */
public class Client {

	private String _clientAddress;
	
	public Client (String clientAddress)
	{
		clientAddress = _clientAddress;
	}
	
	
	/* sends data to a server, and blocks until the server's task finishes 
	 * running.
	 * @return The data sent back from the server (until the server's task 
	 * finishes running). If multiple data packets are sent from the server -
	 * the returned buffer is the concatenation of all data packets received.
	 **/
	public byte[] sendToServerAndGetAnswer(String serverAddress, MessageData data) 
		throws MessengerException
	{
		Messenger m = createMessenger();
		m.send(serverAddress, data.serialize());
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		while (true)
		{
			MessageData md = MessageData.deserialize(m.listen());
			if (md.messageType.equals(MessageData.TASK_ENDED_PACKET_TYPE))
			{
				return outputStream.toByteArray();
			}
			
		}
		
		$ = ;
		TODO // do a listen loop, until ServerTaskEndedMessage is recived.
		m.kill();
		
		
	}
	
		
	
	private Messenger createMessenger() throws MessengerException {
		return (new MessengerFactory()).start(_clientAddress);
	}
}
