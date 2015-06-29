package il.ac.technion.cs.sd.lib;


import il.ac.technion.cs.sd.msg.MessengerException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClientLib {
	private class ClientMessngerWrapper extends MessengerWrapper{
		private ClientMessngerWrapper(String address,Consumer<String> dedicatedConsumer) {
			super(address,dedicatedConsumer);
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
	public void dedicatedSendFromConsumer(String to,String payload){
		messenger.dedicatedSendFromConsumer(to, payload);
	}
	/**
	 * send from a consumer without creating deadlock
	 * have a bigger overhead
	 * @param payload
	 */
	public void dedicatedSendFromConsumer(String payload){
		this.dedicatedSendFromConsumer(serverAddress, payload);
	}
	
	public void dedicatedBlockingSend(String to,String payload){
		messenger.dedicatedBlockingSend(to, payload,false);
	}
	
	public void dedicatedBlockingSendToServer(String payload){
		messenger.dedicatedBlockingSend(serverAddress, payload,false);
	}
	
	public String dedicatedSendRecieve(String to,String payload){
		dedicatedBlockingSend(to, payload);
		try {
			String retVal = null;
			while((retVal = messenger.getResponds().poll(500, TimeUnit.MILLISECONDS)) == null) {
				dedicatedBlockingSend(to, payload);
			}
			return retVal;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String dedicatedSendRecieve(String payload){
		return dedicatedSendRecieve(serverAddress, payload);
	}
	
	//TODO:OFER
	public void kill() {
		messenger.kill();
	}
}
