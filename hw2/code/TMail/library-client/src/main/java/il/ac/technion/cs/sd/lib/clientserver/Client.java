package il.ac.technion.cs.sd.lib.clientserver;


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
	 * @return The single MessageData data sent back from the server (or null if
	 * no data was returned). The server is expected to send back a MessageData
	 * in JSON format.
	 **/
	public MessageData sendToServerAndGetAnswer(
			String serverAddress, MessageData data) 
		throws MessengerException
	{
		Messenger m = createMessenger();
		data.setFromAddress(_clientAddress);
		m.send(serverAddress, data.serialize());
		
		MessageData $ = null;
		while (true)
		{
			MessageData md = MessageData.deserialize(m.listen());
			if (md.getMessageType().equals(MessageData.TASK_ENDED_MESSAGE_TYPE))
			{
				return $;
			}
			
			if ($ != null)
			{
				throw new RuntimeException("Multiple messages sent back from server!");
			}
			$ = md;
		}
	}
	
	
	private Messenger createMessenger() throws MessengerException {
		return (new MessengerFactory()).start(_clientAddress);
	}
}