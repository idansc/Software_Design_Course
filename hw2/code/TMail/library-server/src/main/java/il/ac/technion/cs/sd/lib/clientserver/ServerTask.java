package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.Messenger;

/* Represents a task a server performs repeatedly - each time new data is sent 
 * from any client.*/
@FunctionalInterface
public interface ServerTask {
	
	/* @param serverMessenger The Messenger from which we should send data 
	 * back to the client (if relevant).
	 * @param data The data that the client sent to the server. */
	void run(Messenger serverMessenger, DataPacket data);
}
