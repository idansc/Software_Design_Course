package il.ac.technion.cs.sd.lib.clientserver;

/* Represents a task a server performs repeatedly - each time new data is sent 
 * from any client.*/
@FunctionalInterface
public interface ServerTask {
	
	/* @param server The server processing this task (it'll be the same server 
	 * for all repeated calls to 'run' made with this object).
	 * @param data The data that the client sent to the server. */
	void run(Server server, byte[] data);
}
