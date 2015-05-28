package il.ac.technion.cs.sd.lib.clientserver;
import java.util.List;
import java.util.function.BiConsumer;

public class Server {

	Server(String address)
	{
		//TODO
	}
	
	// String - fromAddress.  //TODO
	<T> void start(BiConsumer<T,String> consumer, Class<T> type) {
		//TODO
	}
	
	void stop()
	{
		//TODO
	}
	
	
	<T> void saveObjectsToFile(String filename, List<T> objects)
	{
		//TODO
	}
	
	<T> List<T> readObjectsFromFile(String filename, Class<T> type)
	{
		return null;
		//TODO
	}
	
}
