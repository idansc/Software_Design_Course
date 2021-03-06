package il.ac.technion.cs.sd.app.mail;
import org.apache.commons.io.FileUtils;

import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import il.ac.technion.cs.sd.lib.clientserver.Server;

/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerMailApplication {
	private Server _server;
	private ServerTaskMail _task;
	private PersistentConfig _persistentConfig;
	
	/**
	 * Starts a new mail server. Servers with the same name retain all their information until
	 * {@link ServerMailApplication#clean()} is called.
	 * 
	 * @param name The name of the server by which it is known.
	 */
	public ServerMailApplication(String name) {
		_server = new Server(name); 
		_persistentConfig = new DefaultPersistentConfig();
	}
	
	void setServer(Server s)
	{
		_server = s;
	}
	
	// default PersistentConfig: default text file is used.
	private class DefaultPersistentConfig implements PersistentConfig {
		@Override
		public InputStream getPersistentMailInputStream() throws IOException {
			File file = getServerPesistentFilename();
			if (!file.exists())
			{
				clearAndInitPersistentDataFile();
			}
			assert(file.exists());
			return new FileInputStream(file);
		} 
		
		@Override
		public OutputStream getPersistentMailOverwriteOutputStream() throws FileNotFoundException {
			File file = getServerPesistentFilename();
			getPesistentDirOfAllServers().mkdirs();
			return new FileOutputStream(file,false);
		}
	}
	
	//TODO: maybe have all persistent content logic in a seperate module.
	private void clearAndInitPersistentDataFile() throws IOException {
		getPesistentDirOfAllServers().mkdirs();
		File file = getServerPesistentFilename();
		OutputStream stream = new FileOutputStream(file, false);
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, "UTF-8"));
		writer.beginArray();
		writer.endArray();
	    writer.close();
	}


	private static File getPesistentDirOfAllServers()
	{
		return new File("./TMP___ServersData");
	}
	
	private File getServerPesistentFilename()
	{
		String baseName = _server.getserverAddress() + ".txt";
		return new File(getPesistentDirOfAllServers(), baseName);
	}
	
	public void setPersistentConfig (PersistentConfig persistentConfig)
	{
		_persistentConfig = persistentConfig;
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return _server.getserverAddress();
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices under this server name are loaded. It is possible
	 * to start a new server instance in same, or another process. You may assume that two server instances with the
	 * same name won't be in parallel. Similarly, {@link ServerMailApplication#stop()} will be called before subsequent
	 * calls to {@link ServerMailApplication#start()}.
	 * @throws ListenLoopAlreadyBeingDone
	 */
	public void start() {
		
		try {
			_task = new ServerTaskMail(_persistentConfig);
			_task.loadPersistentData();
			_server.startListenLoop(_task); 
		} catch (IOException e) {
			throw new IOExceptionRuntime();
		} catch (InterruptedException e) {
			throw new InterruptedExceptionRuntime();
		}

		
	}
	
	/**
	 * Stops the server. A stopped server can't accept mail, but doesn't delete any data. A stopped server does not use
	 * any system resources (e.g., messengers).
	 * @throws NoCurrentListenLoop.
	 */
	public void stop() {
		try {
			_server.stopListenLoop();
			if (_task != null)
				_task.savePersistentData();
			
		} catch (IOException e) {
			throw new IOExceptionRuntime();
		}
		catch (InterruptedException e) {
			throw new InterruptedExceptionRuntime();
		}
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		try {
			clearAndInitPersistentDataFile();
		} catch (IOException e) {
			throw new IOExceptionRuntime();
		}
	}
	
	/* Removes current content of servers data directory (if currently exists)
	 * and makes sure the directory exists.
	 */
	public static void cleanAndInitPersistentDataDirOfAllServers()
	{
		File persistentDataDir = getPesistentDirOfAllServers();
		if (persistentDataDir.exists())
		{
			try {
				FileUtils.deleteDirectory(persistentDataDir);
			} catch (IOException e) {
				throw new IOExceptionRuntime();
			}
		}
		persistentDataDir.mkdirs();
	}
	
	
	public static class InterruptedExceptionRuntime extends RuntimeException {private static final long serialVersionUID = 1L;}
	public static class IOExceptionRuntime extends RuntimeException {private static final long serialVersionUID = 1L;}
	
	

}
