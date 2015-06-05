package il.ac.technion.cs.sd.lib.clientserver;

import java.util.function.Consumer;

public class Client {

	private String _address;
	private String _serverAddress;
	
	/**
	 * 
	 * @param address The address of the client.
	 */
	public Client(String address)
	{
		_address = address;
	}
	
	
	/**
	 * Start the listen loop of the client during which messages sent from the server are consumed.
	 * Each message received from the server invokes the consumer's callback function.
	 * @param serverAddress The server's address.
	 * @param consumer The consumer who's callback will be invoked for each message received from the server. 
	 * @param type The type of the object the server sends the client in each message as data.
	 * (i.e. the parameter type of the consumer's callback function).
	 * Generic types are not supported.
	 * @throws InvalidMessage Invalid message received from the server 
	 * For example: the object sent as message data was not of type 'type'.
	 * @throws InvalidOperation When the listen loop is already running when calling this method.
	 */
	public <T> void startListenLoop(String serverAddress, Consumer<T> consumer, Class<T> type)
	{
		_serverAddress = serverAddress;
		 //TODO
	}
	
	/**
	 * Stop the listen loop of the client (messages sent from server will no longer be consumed.
	 * @throws InvalidOperation When the listen loop was not running when calling this method.
	 */
	public void stopListenLoop()
	{
		//TODO
	}

	/**
	 * Sends a message to the server.
	 * @param data The object to be sent to the server (as message data).
	 * Parametric types of data are not supported.
	 */
	public <T> void send(T data) {
		//TODO
	}
	
	/**
	 * Sends a message to the server, and blocks until a response message is received.
	 * @return The response message data. 
	 * The response message is guaranteed to be the response to the message sent by this method
	 * (and not some other unrelated message the server sent the client). 
	 *  
	 * @param data The object to be sent to the server (as message data).
	 */
	public <T, S> S sendAndBlock(T data, Class<S> answerType)
	{
		//TODO
		return null;
	}

	
	public class InvalidMessage extends RuntimeException {private static final long serialVersionUID = 1L;}
	public class InvalidOperation extends RuntimeException {private static final long serialVersionUID = 1L;}
}
