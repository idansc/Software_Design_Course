package il.ac.technion.cs.sd.lib;

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ServerLib{
	
	private MessengerWrapper messenger;
	private final BlockingQueue<String> tasks = new LinkedBlockingQueue<>();
	private String address;
	private BiConsumer<String,ServerLib> requestHandler;
	private volatile boolean started = false;
	private Thread start = new Thread(this::dedicatedListenToRequests);
	
	/**
	 * 
	 * @param address
	 * @param requestHandler handle a request (string format of payload) will be run for every request 
	 * once the start method is activated
	 */
	public ServerLib(String address,BiConsumer<String,ServerLib> requestHandler){
		this.requestHandler = requestHandler;
		this.address = address;
		messenger = new MessengerWrapper(address);
	}
	public ServerLib(String address)
	{
		this(address,null);
	}
	/**
	 * change the request handler
	 * @throws alreadyRunning
	 * @param requestHandler
	 */
	public void setDedicatedRequestHandler(BiConsumer<String,ServerLib> requestHandler){
		if (started)
			throw new alreadyRunning();
		this.requestHandler = requestHandler;
	}
	
	
	
public <T> void start(BiConsumer<T,String> consumer, Type dataType) {
		
		try {
			messenger.start((fromAddress, data) -> {
				consumer.accept(MessengerWrapper.fromGsonStrToObject(data, dataType), fromAddress);
			});
		} catch (MessengerException e) {
			System.out.println(e.getMessage());
			throw new InvalidOperation();
		}
		
	}

	public void stop()
	{
		messenger.stop();
	}
	

	public <T> void send(String clientAddress, T data, boolean isResponse)
	{
		try {
			messenger.send(clientAddress, MessengerWrapper.fromObjectToGsonStr(data), isResponse);
		} catch (MessengerException e) {
			throw new CommunicationFailure();
		}
	}
	
	
	public void clearPersistentData()
	{
		File persistentDataDir = getServerPersistentDir();
		if (!persistentDataDir.exists())
		{
			return;
		}
		try {
			if (!persistentDataDir.exists())
				return;
			FileUtils.cleanDirectory(getServerPersistentDir());
			FileUtils.deleteDirectory(getServerPersistentDir());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}


	public <T> void saveObjectToFile(String filename, T data)
	{
		File file = getFileByName(filename, true);
		
		try (JsonWriter persistentDataWriter = createJsonWriter(file)) 
		{
			MessengerWrapper.writeObjectToJsonWriter(data,persistentDataWriter);
		} catch (IOException e) {			
			throw new RuntimeException("Failed to close stream");
		}
	}
	 
	
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
			$ =  Optional.of(MessengerWrapper.readObjectFromGsonReader(persistentDataReader, type));
		}
		catch (RuntimeException e)
		{
			throw new BadFileContent();
		} catch (IOException e1) {
			throw new RuntimeException("Failed to close stream");
		}
		
		return $;
	}
	
	private JsonWriter createJsonWriter(File f)
	{
		try {
			OutputStream stream = new FileOutputStream(f);
			return  new JsonWriter(new OutputStreamWriter(stream, MessengerWrapper.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("FileNotFoundException");
		}
	}
	
	private JsonReader createJsonReader(File f)
	{
		if(!f.exists())
		{
			throw new RuntimeException("File does not exist");
		}
		try {
			InputStream stream = new FileInputStream(f);
			return new JsonReader(new InputStreamReader(stream, MessengerWrapper.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("FileNotFoundException");
		}
	}

	private File getFileByName(String filename, boolean createItsDirIfNecessary)
	{
		File serverDir = getServerPersistentDir();
		if (createItsDirIfNecessary)
		{
			serverDir.mkdirs();
		}
		return new File(serverDir, filename);
	}

	private File getServerPersistentDir() {
		return new File(getPesistentDirOfAllServers(), getServerPersistentDirName());
	}
	
	private String getServerPersistentDirName()
	{
		return Integer.toString(getAddress().hashCode());
	}
	
	private static File getPesistentDirOfAllServers()
	{
		return new File("./TMP___ServersData");
	}
	
	@SuppressWarnings("serial")
	public class BadFileContent extends RuntimeException {}
	
	
	
	private void dedicatedListenToRequests(){
		started =true;
		while (started){
			String task = null;
			try {
				task = tasks.take();
			} catch (InterruptedException e) {
				return;
			}
			requestHandler.accept(task, this);
		}
	}


	public void dedicatedStart(){
		if (started)
			throw new alreadyRunning();
		messenger = new  MessengerWrapper(address, (x) -> tasks.add(x));
		started = true;
		start = new Thread(this::dedicatedListenToRequests);
		start.start();
	}
	
	/**
	 * payload go to the client's consumer
	 * can and should be used on request handler
	 * keep sending data until the target get it
	 * @param to
	 * @param payload
	 */
	public void dedicatedBlockingSend(String to,String payload){
		messenger.dedicatedBlockingSend(to,payload,false);
	}
	
	/**
	 * payload don't go to client's consumer assume consumer is 
	 * should be used just as a respond to user request where the user will sit and wait for it
	 * @param to user
	 * @param payload the respond
	 */
	public void dedicatedBlockingRespond(String to,String payload){
		messenger.dedicatedBlockingSend(to,payload,true);
	}
	
	
	public String getAddress() {
		return address;
	}
	
	public static class TargetIsntLogedIn extends RuntimeException{}
	public static class alreadyRunning extends RuntimeException{}
}
