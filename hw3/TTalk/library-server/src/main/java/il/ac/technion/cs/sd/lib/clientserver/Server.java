package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.MessengerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

//TODO: add documentation to the package.
//TODO: compile to html javadoc.
//TODO: consider giving in javadoc a tip on sending a type for generic types.
//TODO: add to documentation each time there's a "Type" - the generic pattern.
//TODO: recommend against nested classes because of gson.
//TODO: rename "start listen loop" to "start", same for "end"

/**
 * Represents a server that can communicate (reliably) with multiple clients, and save/load 
 * persistent data.
 * The messages sent and received consist of objects of any type.
 * 
 * This class is not thread-safe (meaning you must not access an object of this class from multiple 
 * threads simultaneously). 
 */
public class Server {

	private ReliableHost _reliableHost;
	
	
	public String getAddress() {
		return _reliableHost.getAddress();
	}


	/**
	 * @param address - the address of the new server. 
	 */
	public Server(String address)
	{
		_reliableHost = new ReliableHost(address);
	}


	/**
	 *  
	 * Starts the server listen loop. 
	 * While listening, the server process incoming messages from clients.
	 * Each incoming message invokes a callback function. 
	 * @param consumer The consumer who's callback will be invoked for each message received from a client.
	 * The first argument it takes is the data sent, and the second argument is the sender's address.
	 * Any message sent back to the client via @{link {@link #sendResponse(String, Object)} 
	 * from the callback function is considered by the client as a response to the specific message 
	 * that invoked the callback.
	 * While the consumer's callback is running - the listen loop is frozen, so the code in the 
	 * callback shouldn't wait for a new message to arrive. 
	 * @param dataType The type of the object sent by the client in each message
	 * (i.e., the type of the object passed to the consumer's callback function).
	 * If the type is generic, for example, a list of Integers, you should pass as 'dataType' 
	 * something created with the following pattern:
	 * {@code new TypeToken<List<Integer>>(){}.getType())}
	 * @throws InvalidMessage If the listen loop is already running. 
	 */
	public <T> void startListenLoop(BiConsumer<T,String> consumer, Type dataType) { //TODO
		
		try {
			_reliableHost.start((fromAddress, data) -> {
				consumer.accept(Utils.fromGsonStrToObject(data, dataType), fromAddress);
			});
		} catch (MessengerException e) {
			throw new InvalidOperation();
		}
		
	}

	

	/**
	 * Stop the listen loop (messages sent from clients will no longer be consumed).
	 *  {@link #saveObjectToFile(String, Object, boolean)} and {@link #readObjectFromFile(String, Type, boolean)} 
	 *  will re-open streams, so you'll start reading/writing from the begining of the files).
	 * @throws InvalidOperation When the listen loop was not running when calling this method.
	 * 
	 */
	public void stop()
	{
		_reliableHost.stop();
	}
	

	/**
	 * Sends a message to a client.
	 * @param clientAddress The address of the client.
	 * @param data The data to be sent to the client.
	 * @param isResponse true iff 'data' is a response to a message previously sent by the client.
	 * When true, you must call this method only from the consumer of the listen loop (i.e., from the 
	 * callback function invoked by the message to which the response is for).
	 * @throws InvalidOperation Bad clientAddress address etc..
	 */
	public <T> void send(String clientAddress, T data, boolean isResponse)
	{
		try {
			_reliableHost.send(clientAddress, Utils.fromObjectToGsonStr(data), isResponse);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		}
	}
	
	
	/**
	 * Clears all persistent data saved by this server.
	 */
	public void clearPersistentData()
	{
		File persistentDataDir = getServerPersistentDir();
		if (!persistentDataDir.exists())
		{
			return;
		}
		try {
			FileUtils.deleteDirectory(getServerPersistentDir());
		} catch (IOException e) {
			throw new RuntimeException("deleting directory failed!");
		}
	}
	
	/**
	 * Saves a an object to persistent memory (file).
	 * @param filename The filename, without path, of the file to save 'data' into.
	 * This file will hold a single object ('data'). Previous content, if the file already exists,
	 * will be lost.
	 * @param data The object to be saved to the file.  
	 */
	public <T> void saveObjectToFile(String filename, T data)
	{
		File file = getFileByName(filename, true);
		
		try (JsonWriter persistentDataWriter = createJsonWriter(file)) 
		{
			Utils.writeObjectToJsonWriter(data,persistentDataWriter);
		} catch (IOException e) {			
			throw new RuntimeException("Failed to close stream");
		}
	}
	 
	
	/**
	 * Reads an object from persistent memory (file).
	 * @param filename The filename of the file to read, without path. This file cotains a single object.
	 * @return The object read, or empty if the file doesn't exist.
	 * @throws BadFileContent If an unexpected file content was read.
	 */
	public <T> Optional<T> readObjectFromFile(String filename, Type type)
	{
		File file = getFileByName(filename, false);
		
		if (!file.exists())
		{
			return Optional.empty();
		}
		
		
		Optional<T> $;
		
		try (JsonReader persistentDataReader = createJsonReader(file))
		{
			$ =  Optional.of(Utils.readObjectFromGsonReader(persistentDataReader, type));
		}
		catch (RuntimeException e)
		{
			throw new BadFileContent();
		} catch (IOException e1) {
			throw new RuntimeException("Failed to close stream");
		}
		
		return $;
	}
	

	

	/**
	 * Precondition: the directory f is in must already exist (f itself might not exist).
	 */
	private JsonWriter createJsonWriter(File f)
	{
		try {
			OutputStream stream = new FileOutputStream(f);
			return  new JsonWriter(new OutputStreamWriter(stream, Utils.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("FileNotFoundException");
		}
	}
	
	
	/**
	 * Precondition: f is an existing file.
	 * @throws RuntimeException - file not found.
	 */
	private JsonReader createJsonReader(File f)
	{
		if(!f.exists())
		{
			throw new RuntimeException("File does not exist");
		}
		try {
			InputStream stream = new FileInputStream(f);
			return new JsonReader(new InputStreamReader(stream, Utils.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("FileNotFoundException");
		}
	}

	/**
	 * Returns a File object representing the file.
	 * @param filename - the filename, without path.
	 * @param createItsDirIfNecessary If true, and the directory of the file does not exist - it is 
	 * created (along with all necessary parents).
	 */
	private File getFileByName(String filename, boolean createItsDirIfNecessary)
	{
		File serverDir = getServerPersistentDir();
		if (createItsDirIfNecessary)
		{
			serverDir.mkdirs();
		}
		return new File(serverDir, filename);
	}


	// returns the directory holding the persistent files of the server.
	private File getServerPersistentDir() {
		return new File(getPesistentDirOfAllServers(), getServerPersistentDirName());
	}
	
	
	// returns the unique name (wihtout path) of the directory holding the persistent files of the server. 
	private String getServerPersistentDirName()
	{
		return Integer.toString(getAddress().hashCode());
	}
	
	private static File getPesistentDirOfAllServers()
	{
		return new File("./TMP___ServersData");
	}
}
