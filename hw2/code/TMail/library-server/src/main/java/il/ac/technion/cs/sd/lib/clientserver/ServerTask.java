package il.ac.technion.cs.sd.lib.clientserver;

/* Represents a task a server performs repeatedly - each time new data is sent 
 * from any client.*/
@FunctionalInterface
public interface ServerTask {
	
	/* @param serverAddress The address of server processing this task.
	 * @param data The data that the client sent to the server. 
	 * @return the answer to be sent back to the client. */
	MessageData run(String serverAddress, MessageData data);
}
