package il.ac.technion.cs.sd.lib.clientserver;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

//TODO: add documentation to the package.
//TODO: compile to html javadoc.

/**
 * Represents a server that can communicate with multiple clients.
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
	public <T> void start(BiConsumer<T,String> consumer, Class<T> dataType) {
		
		//TODO
	}
	
	public void stop()
	{
		//TODO
	}
	
	

	/**
	 * Sends a message to a client. The message is not considered a response to a specific message
	 * from the client.
	 * @param clientAddress The address of the client.
	 * @param data The data to be sent to the client.
	 * Generic types of 'data' are not supported.
	 */
	public <T> void send(String clientAddress, T data)
	{
		
	}
	
	
	/**
	 * Just like {@link #send(String, Object)} except that the message sent will be considered a 
	 * response to a specific message sent by a client.
	 * You must call this method only from the consumer of the of the listen loop (i.e., from the 
	 * callback function invoked by the message to which the response is for).
	 */
	public <T> void sendResponse(String clientAddress, T data)
	{
		
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

	
	public class InvalidMessage extends RuntimeException {private static final long serialVersionUID = 1L;}
}
