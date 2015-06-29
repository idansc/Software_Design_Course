package il.ac.technion.cs.sd.lib;


import il.ac.technion.cs.sd.msg.MessengerException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClientLib {
	private class ClientMessngerWrapper extends MessengerWrapper{
		private ClientMessngerWrapper(String address,Consumer<String> onRecieve) {
			super(address,onRecieve);
		}
		private BlockingQueue<String> getResponds(){
			return responds;
		}
	}
	
	private ClientMessngerWrapper messenger = null;
	private String serverAddress;
	private String myAddress;
	
	public ClientLib(String address,Consumer<String> onRecieve,String serverAddress){
		messenger = new  ClientMessngerWrapper(address, onRecieve);
		this.serverAddress=serverAddress;
		myAddress = address;
	}
	
	public ClientLib(String address,Consumer<String> onRecieve){
		messenger = new  ClientMessngerWrapper(address, onRecieve);
		myAddress = address;
	}
	
	public ClientLib(String address,String serverAddress){
		this.serverAddress=serverAddress;
		myAddress = address;
	}
	/**
	 * send from a consumer without creating deadlock
	 * have a bigger overhead
	 * @param to
	 * @param payload
	 */
	public void sendFromConsumer(String to,String payload){
		messenger.sendFromConsumer(to, payload);
	}
	/**
	 * send from a consumer without creating deadlock
	 * have a bigger overhead
	 * @param payload
	 */
	public void sendFromConsumer(String payload){
		this.sendFromConsumer(serverAddress, payload);
	}
	
	public void blockingSend(String to,String payload){
		messenger.blockingSend(to, payload,false);
	}
	
	public void blockingSendToServer(String payload){
		messenger.blockingSend(serverAddress, payload,false);
	}
	
	public String sendRecieve(String to,String payload){
		blockingSend(to, payload);
		try {
			String retVal = null;
			while((retVal = messenger.getResponds().poll(500, TimeUnit.MILLISECONDS)) == null) {
				blockingSend(to, payload);
			}
			return retVal;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String sendRecieve(String payload){
		return sendRecieve(serverAddress, payload);
	}
	
	public void kill() {
		messenger.kill();
	}
}
