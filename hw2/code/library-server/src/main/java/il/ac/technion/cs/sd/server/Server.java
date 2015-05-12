package il.ac.technion.cs.sd.server;

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
