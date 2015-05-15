package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.Messenger;

public class Server {
	
	private ServerTask _task;
	
	/* @param task The server task to be performed each time data is sent to the server. */
	public Server(ServerTask task)
	{
		_task = task;
	}
	
	
	public void listen()
	{
		Messenger messanger = new Messenger();
		
		String json = messanger.listen();
		
		Task task = _taskFactory.create(json);

		task.run();
		
	}
}
