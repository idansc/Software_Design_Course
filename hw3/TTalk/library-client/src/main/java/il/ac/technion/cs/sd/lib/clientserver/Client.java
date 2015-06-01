package il.ac.technion.cs.sd.lib.clientserver;

import java.util.function.BiConsumer;

public class Client {

	public Client(String address)
	{
		//TODO
	}
	
	// String - fromAddress. //TODO
	public <T> void start(BiConsumer<T, String> callback, Class<T> type)
	{
		 //TODO
	}
	
	public void stop()
	{
		//TODO
	}
	
	public <T> void send(String serverAddress, T data) {
		//TODO
	}
	
	public <RequestType, AnswerType> AnswerType sendAndBlock(
			String serverAddress, RequestType data, Class<AnswerType> answerType)
	{
		//TODO
		return null;
	}
	
}
