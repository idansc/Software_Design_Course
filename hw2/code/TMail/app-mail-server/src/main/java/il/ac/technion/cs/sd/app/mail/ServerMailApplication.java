package il.ac.technion.cs.sd.app.mail;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
		
		
		// setting _persistentConfig to default (s.t. it would use the default
		// text file. 
		_persistentConfig = new PersistentConfig() {
			@Override
			public InputStream getPersistentMailInputStream() throws FileNotFoundException {
				File file = new File(getDafualtPesistentFilename());
				return new FileInputStream(file);
			}
			@Override
			public OutputStream getPersistentMailOverwriteOutputStream() throws FileNotFoundException {
				File file = new File(getDafualtPesistentFilename());
				return new FileOutputStream(file,false);
			}
		};
	}
	
	private String getDafualtPesistentFilename()
	{
		return getClass().getResource(_server.getserverAddress() + ".txt").toString();
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
	 */
	public void start() {
		
		try {
			_task = new ServerTaskMail(_persistentConfig);
			_task.loadPersistentData();
		} catch (IOException e) {
			throw new IOExceptionRuntime();
		}
		
		
		_server.startListenLoop(_task);
	}
	
	/**
	 * Stops the server. A stopped server can't accept mail, but doesn't delete any data. A stopped server does not use
	 * any system resources (e.g., messengers).
	 */
	public void stop() {
		try {
			_server.stopListenLoop();
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
		throw new UnsupportedOperationException("Not implemented");
	}
	
	
	public class InterruptedExceptionRuntime extends RuntimeException {private static final long serialVersionUID = 1L;}
	public class IOExceptionRuntime extends RuntimeException {private static final long serialVersionUID = 1L;}
	
	
	

}
