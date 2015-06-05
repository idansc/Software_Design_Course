package il.ac.technion.cs.sd.lib.clientserver;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a client that can communicate (reliably) with a single server.
 * The messages sent and received consist of objects of any (non-generic) type.
 * 
 * This class is not thread-safe (meaning you must not access an object of this class from multiple 
 * threads simultaneously). 
 */
public class Client {

	private String _address;
	private String _serverAddress;
	
	/**
	 * 
	 * @param address The address of the new client.
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
	 * @param dataType The type of the object the server sends the client in each message as data.
	 * (i.e. the type of the parameter the consumer's callback function receives).
	 * Generic types are not supported.
	 * @throws InvalidMessage Invalid message received from the server 
	 * For example: the object sent as message data was not of type 'type'.
	 * @throws InvalidOperation When the listen loop is already running when calling this method.
	 */
	public <T> void startListenLoop(String serverAddress, Consumer<T> consumer, Class<T> dataType)
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
	 * @param data The object to be sent to the server (as message data).
	 * @param responseType The type of the object the server sends back as data.
	 * (i.e. the type of the returned value).
	 * Generic types are not supported.
	 * @return The response message data. 
	 * The response message is guaranteed to be the response to the message sent by this method
	 * (and not some other unrelated message the server sent the client).
	 * @throws InvalidMessage Invalid message was received back from the server. 
	 */
	public <T, S> S sendAndBlockUntilResponseArrives(T data, Class<S> responseType)
	{
		//TODO
		return null;
	}
	
	
	/**
	 * Just like {@link #sendAndBlockUntilResponseArrives(Object, Class)} but the returned response
	 * is a list of objects instead of a single object.
	 * @param data
	 * @param responseType
	 * @return
	 */
	public <T, S> List<S> sendAndBlockUntilResponseListArrives(T data, Class<S> responseType)
	{
		//TODO
		return null;
	}
	
}
