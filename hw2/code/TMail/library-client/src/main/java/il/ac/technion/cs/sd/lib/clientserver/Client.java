package il.ac.technion.cs.sd.lib.clientserver;


import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;


/* Represents a client that can send data to a server and receive answer. */
public class Client {

	private String _clientAddress;
	
	public Client (String clientAddress)
	{
		_clientAddress = clientAddress;
	}
	
	
	
	/* sends data to a server, and blocks until an answer is returned.
	 * @return The single MessageData data sent back from the server.
	 * The server is expected to send back a MessageData
	 * in JSON format.
	 * @throws MultipleAnswersReceived - if multiple answers are received.
	 * @throws ConnectionError
	 **/
	public MessageData sendToServerAndGetAnswer(
			String serverAddress, MessageData data)  
	{
		try {
			Messenger m;
			m = createMessenger();
			
			data.setFromAddress(_clientAddress);
			m.send(serverAddress, data.serialize());
			
	
			MessageData md = MessageData.deserialize(m.listen());
			m.kill();
			return md;
		} catch (MessengerException e)
		{
			throw new ConnectionError();
		}
	}
	
	
	private Messenger createMessenger() throws MessengerException {
		return (new MessengerFactory()).start(_clientAddress);
	}

	
	public class ConnectionError extends RuntimeException {private static final long serialVersionUID = 1L;}

}
