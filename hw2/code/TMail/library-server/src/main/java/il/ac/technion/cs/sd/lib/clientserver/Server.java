package il.ac.technion.cs.sd.lib.clientserver;

public class Server {
	
	private TaskFactory _taskFactory;
		
	public Server(TaskFactory taskFactory)
	{
		_taskFactory = taskFactory;
	}
	
	
	void Listen()
	{
		Mesanger messanger();
		String json = messanger.listen();
		
		Task task = _taskFactory.create(json);

		task.run();
		
	}
}
