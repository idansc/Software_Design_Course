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
	
	/* sends data to a server, without retrieving answer from server. */
	public void sendToServerWithoutAnswer(String serverAddress, byte[] data) 
		throws MessengerException
	{
		sendToServer(serverAddress, data, false);
	}
	
	/* sends data to a server, and blocks until an answer from the server is
	 * received.
	 * @return The data sent back from the server.
	 **/
	public byte[] sendToServerAndGetAnswer(String serverAddress, byte[] data) 
		throws MessengerException
	{
		return sendToServer(serverAddress, data, true);
	}
	
	
	/* like sendToServerAndGetAnswer, but returns answer from server (blocks) 
	 * iff getAnswer is true, otherwise - null is returned.
	 */
	public byte[] sendToServer(String serverAddress, byte[] data, boolean getAnswer) throws MessengerException
	{
		Messenger m = createMessenger();
		m.send(serverAddress, data);
		byte[] $ = null;
		if (getAnswer)
			$ = m.listen();
		m.kill();
		return $;
	}
	
	
	private Messenger createMessenger() throws MessengerException {
		return (new MessengerFactory()).start(_clientAddress);
	}
}
