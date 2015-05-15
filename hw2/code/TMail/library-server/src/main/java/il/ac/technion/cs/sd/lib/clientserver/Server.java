package il.ac.technion.cs.sd.lib.clientserver;

import java.util.Optional;


import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;


/* Represents a server that performs a given task whenever data is recived from 
 * any client. */
public class Server {
	
	private String _serverAddress;
	private ServerTask _task;
	
	/* This is thread running the listening loop. 
	 * It's null iff no listen loop is currently done, or stopListenLoop was
	 * just called and the listen loop is going to stop. */ 
	private Thread listenThread = null; 
	
	private boolean stopListenRequested = false;
	
	
	public class NoCurrentListenLoop extends RuntimeException {private static final long serialVersionUID = 1L;} 
	public class ListenLoopAlreadyBeingDone extends RuntimeException {private static final long serialVersionUID = 1L;} 
	
	
	/* @param task The server task to be performed each time data is sent to the server. */
	public Server(String serverAddress, ServerTask task)
	{
		_serverAddress = serverAddress;
		_task = task;
	}
	
	
	/* Starts a "listen loop" in which the server repeatedly listens for data
	 * from clients, and for each data package sent - the given task is run. 
	 * This is done until stopListenLoop is called.
	 * This function is non-blocking (everything is done on another thread).
	 **/
	public void startListenLoop()
	{
		if (listenThread != null )
		{
			throw new ListenLoopAlreadyBeingDone();
		}
		stopListenRequested = false;
		
		listenThread = new Thread( () -> {
			try {
				Messenger messenger = createMessenger();
				while (!stopListenRequested)
				{
					Optional<byte[]> data = messenger.tryListen();
					if (data.isPresent())
					{
						_task.run(this,data.get());
					}
	
					// If we cared about not wasting CPU time:
					// Thread.sleep(10);
				}
				messenger.kill();
			} catch (MessengerException exc)
			{
				// hmmm, no indication is passed on, oh well :(
			}
				
		});
		
	
		
		listenThread = null;
	}
	
	
	public void stopListenLoop()
	{
		if (listenThread == null )
		{
			throw new NoCurrentListenLoop();
		}
		stopListenRequested = true;
		listenThread = null;
	}
	
	
	public void sendDataToClient(String clientAddress, byte[] data) throws MessengerException
	{
		Messenger m = createMessenger();
		m.send(clientAddress, data);
		m.kill();
	}
	
	
	private Messenger createMessenger() throws MessengerException {
		return (new MessengerFactory()).start(_serverAddress);
	}
}
