package il.ac.technion.cs.sd.lib.clientserver;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

//TODO: add documentation to the package.
//TODO: compile to html javadoc.
//TODO: consider documenting the fact that any type not suited for GSON - is not supported (not only
		// generic types (also types with generic fields.
/**
 * Represents a server that can communicate (reliably) with multiple clients, and save/load 
 * persistent data.
 * The messages sent and received consist of objects of any (non-generic) type.
 * 
 * This class is not thread-safe (meaning you must not access an object of this class from multiple 
 * threads simultaneously). 
 */
public class Server {

	private String _address;
	
	/*
	 * @param address - the address of the new server.
	 */
	public Server(String address)
	{
		_address = address;
	}


	/**
	 *  
	 * Starts the server listen loop. 
	 * While listening, the server process incoming messages from clients.
	 * Each incoming message invokes a callback function. 
	 * @param consumer The consumer who's callback will be invoked for each message received from a client.
	 * Any message sent back to the client via @{link {@link #sendResponse(String, Object)} 
	 * from the callback function is considered by the client as a response to the specific message 
	 * that invoked the callback.
	 * @param dataType The type of the object sent by the client in each message
	 * (i.e., the type of the object passed to the consumer's callback function).
	 * @throws InvalidMessage If the list loop is already running. 
	 */
	public <T> void startListenLoop(BiConsumer<T,String> consumer, Class<T> dataType) {
		
		//TODO
	}

	

	/**
	 * Stop the listen loop (messages sent from clients will no longer be consumed.
	 * @throws InvalidOperation When the listen loop was not running when calling this method.
	 */
	public void stop()
	{
		//TODO
	}
	
	

	/**
	 * Sends a message to a client. The message is NOT a response to a previous message from the client.
	 * @param clientAddress The address of the client.
	 * @param data The data to be sent to the client.
	 * Generic types of 'data' are not supported.
	 */
	public <T> void send(String clientAddress, T data)
	{
		//TODO
	}
	
	
	/**
	 * Just like {@link #send(String, Object)}, but sends a list of objects rather than a
	 * single object.
	 * The type of the elements in 'data' must be non-generic. 
	 */
	public <T> void send(String clientAddress, List<T> data)
	{
		//TODO
	}
	
	
	/**
	 * Sends a message to a client as a response to a message previously sent from it.
	 * You must call this method only from the consumer of the listen loop (i.e., from the 
	 * callback function invoked by the message to which the response is for).
	 * @param clientAddress The address of the client.
	 * @param data The data to be sent to the client.
	 * Generic types of 'data' are not supported.
	 */
	public <T> void sendResponse(String clientAddress, T data)
	{
		//TODO
	}
	
	/**
	 * Just like {@link #sendResponse(String, Object)}, but sends a list of objects rather than a
	 * single object.
	 * The type of the elements in 'data' must be non-generic. 
	 */
	public <T> void sendResponse(String clientAddress, List<T> data)
	{
		//TODO
	}
	
	
	/**
	 * Saves a list of objects to persistent memory (file).
	 * @param filename The filename of the file to save, without path.
	 * If the file already exists, the previous content is lost.
	 * @param objects The objects to be saved to the file (order is preserved).
	 * Generic object types are not supported.
	 */
	public <T> void saveObjectsToFile(String filename, List<T> objects)
	{
		//TODO
	}
	 
	/**
	 * Reads a list of objects from persistent memory (file).
	 * @param filename The filename of the file to read, without path.
	 * @param objects The objects to be saved to the file (order is preserved).
	 * Generic object types are not supported.
	 * @return List of read objects (order preserved), or empty value if no file with that name exists.
	 */
	public <T> Optional<List<T>> readObjectsFromFile(String filename, Class<T> type)
	{
		return null;
		//TODO
	}

}
