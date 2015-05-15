package il.ac.technion.cs.sd.lib.clientserver;

import java.util.Optional;


import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;


/* Represents a server that performs a given task whenever data is received from 
 * any client. */
public class Server {
	
	private String _serverAddress;
	
	/* This is thread running the listening loop. 
	 * It's null iff no listen loop is currently done, or stopListenLoop was
	 * just called and the listen loop is going to stop. */ 
	private Thread listenThread = null; 
	
	private boolean stopListenRequested = false;
	
	
	public class NoCurrentListenLoop extends RuntimeException {private static final long serialVersionUID = 1L;} 
	public class ListenLoopAlreadyBeingDone extends RuntimeException {private static final long serialVersionUID = 1L;} 
	
	
	public Server(String serverAddress, ServerTask task)
	{
		_serverAddress = serverAddress;
	}
	
	/* Starts a "listen loop" in which the server repeatedly listens for data
	 * from clients, and for each data package sent - the given task is run. 
	 * This is done until stopListenLoop is called.
	 * This function is non-blocking (everything is done on another thread).
	 * @param task The server task to be performed each time data is sent to the
	 * server. Once the task run is complete, a JSON MessageData will be 
	 * sent back to the client with the messageType of 
	 * MessageData.serverTaskEndedPacketType.
	 **/
	public void startListenLoop(ServerTask task)
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
						MessageData md = MessageData.deserialize(data.get());
								
						task.run(messenger,md);

						MessageData taskEndedMessage = new MessageData(
								_serverAddress,
								MessageData.TASK_ENDED_MESSAGE_TYPE,
								null);
						
						messenger.send(md.getFromAddress(), 
								taskEndedMessage.serialize() );
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
	
	/* Stops the "listen loop" started with startListenLoop */
	public void stopListenLoop()
	{
		if (listenThread == null )
		{
			throw new NoCurrentListenLoop();
		}
		stopListenRequested = true;
		listenThread = null;
	}
	
	
	private Messenger createMessenger() throws MessengerException {
		return (new MessengerFactory()).start(_serverAddress);
	}
}
