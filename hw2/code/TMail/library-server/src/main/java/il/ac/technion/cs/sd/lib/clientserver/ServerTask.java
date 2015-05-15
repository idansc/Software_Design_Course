package il.ac.technion.cs.sd.lib.clientserver;

public interface ServerTask {
	
	/* @param sender The address of the host sending the request.
	 * @param data The data sent to the server on which the task should act upon. */
	void run(String sender, byte[] data);
}
