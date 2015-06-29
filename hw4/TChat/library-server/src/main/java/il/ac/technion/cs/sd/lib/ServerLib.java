package il.ac.technion.cs.sd.lib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

public class ServerLib{
	
	private MessengerWrapper messenger = null;
	private final BlockingQueue<String> tasks = new LinkedBlockingQueue<>();
	private String address;
	private BiConsumer<String,ServerLib> requestHandler;
	private volatile boolean started = false;
	private Thread start = new Thread(this::listenToRequests);
	
	/**
	 * 
	 * @param address
	 * @param requestHandler handle a request (string format of payload) will be run for every request 
	 * once the start method is activated
	 */
	public ServerLib(String address,BiConsumer<String,ServerLib> requestHandler){
		this.requestHandler = requestHandler;
		this.address = address;
	}
	/**
	 * change the request handler
	 * @throws alreadyRunning
	 * @param requestHandler
	 */
	public void setRequestHandler(BiConsumer<String,ServerLib> requestHandler){
		if (started)
			throw new alreadyRunning();
		this.requestHandler = requestHandler;
	}
	
	private void listenToRequests(){
		started =true;
		while (started){
			String task = null;
			try {
				task = tasks.take();
			} catch (InterruptedException e) {
				return;
			}
			requestHandler.accept(task, this);
		}
	}
	/**
	 * release all resources 
	 */
	public void kill(){
		started = false;
		start.interrupt();
		messenger.kill();
	}
	/**
	 * start waiting for requests 
	 * run requestHandler on each request (string argument)
	 */
	public void start(){
		if (started)
			throw new alreadyRunning();
		messenger = new  MessengerWrapper(address, (x) -> tasks.add(x));
		started = true;
		start = new Thread(this::listenToRequests);
		start.start();
	}
	
	/**
	 * payload go to the client's consumer
	 * can and should be used on request handler
	 * keep sending data until the target get it
	 * @param to
	 * @param payload
	 */
	public void blockingSend(String to,String payload){
		messenger.blockingSend(to,payload,false);
	}
	
	/**
	 * payload don't go to client's consumer assume consumer is 
	 * should be used just as a respond to user request where the user will sit and wait for it
	 * @param to user
	 * @param payload the respond
	 */
	public void blockingRespond(String to,String payload){
		messenger.blockingSend(to,payload,true);
	}
	
	
	public static class TargetIsntLogedIn extends RuntimeException{}
	public static class alreadyRunning extends RuntimeException{}
}
