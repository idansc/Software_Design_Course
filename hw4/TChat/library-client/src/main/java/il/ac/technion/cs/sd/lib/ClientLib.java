package il.ac.technion.cs.sd.lib;


import il.ac.technion.cs.sd.msg.MessengerException;

import java.lang.reflect.Type;
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
	
	public ClientLib(String address)
	{
		this(address, null, null);
	}
	
	public ClientLib(String address,Consumer<String> dedicatedConsumer,String serverAddress){
		messenger = new  ClientMessngerWrapper(address, dedicatedConsumer);
		this.serverAddress=serverAddress;
	}
	
	public ClientLib(String address,Consumer<String> onRecieve){
		messenger = new  ClientMessngerWrapper(address, onRecieve);
	}
	
	public ClientLib(String address,String serverAddress){
		this.serverAddress=serverAddress;
	}
	
	
	public <T> void start(String serverAddress, Consumer<T> consumer, Type dataType)
	{
		String originalServerAddress = this.serverAddress;
		this.serverAddress = serverAddress;
		
		try {
			messenger.start((fromAddress,data) -> {
				consumer.accept(MessengerWrapper.fromGsonStrToObject(data, dataType));
			});
		} catch (MessengerException e) {
			this.serverAddress = originalServerAddress;
			throw new RuntimeException("Communication Failure");
		}
		
	}
	
	public <T> void blockingSend(T data) {
		try {
			String payload = MessengerWrapper.fromObjectToGsonStr(data);
			messenger.send(this.serverAddress, payload, false);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		} 
	}
	
	
	public <T, S> S sendRecieve(T data, Type responseType)
	{
		try {
			String str = messenger.sendAndBlockUntilResponseArrives(
					this.serverAddress, MessengerWrapper.fromObjectToGsonStr(data));
			
			return MessengerWrapper.fromGsonStrToObject(str, responseType);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		}
		
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
	
	public void kill() {
		messenger.stop();
	}
	
	@Override
	public String toString() {

		return messenger.getAddress();
	}
	
	public String getAddress() {

		return messenger.getAddress();
	}
}
