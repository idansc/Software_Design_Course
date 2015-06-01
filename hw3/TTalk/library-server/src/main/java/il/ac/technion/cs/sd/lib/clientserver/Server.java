package il.ac.technion.cs.sd.lib.clientserver;
import java.util.List;
import java.util.function.BiConsumer;

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
	 * Starts the server listen loop. While listening, the server process incoming requests  
	 * @param consumer - the callback function incoming requests from the clients.
	 */
	public <T> void start(BiConsumer<T,String> callback, Class<T> type) {
		
		//TODO
	}
	
	public void stop()
	{
		//TODO
	}
	
	/**
	 * 
	 * @param filename
	 * @param objects
	 */
	public <T> void saveObjectsToFile(String filename, List<T> objects)
	{
		//TODO
	}
	
	public <T> List<T> readObjectsFromFile(String filename, Class<T> type)
	{
		return null;
		//TODO
	}
	
}
