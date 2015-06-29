package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.MessengerException;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class ClientLib {

	private String serverAddress;
	private MessengerWrapper messenger;
	
	public ClientLib(String address)
	{
		messenger = new MessengerWrapper(address);
	}
	
	public String getAddress()
	{
		return messenger.getAddress();
	}
	
	public <T> void start(String serverAddress, Consumer<T> consumer, Type dataType)
	{
		String originalServerAddress = this.serverAddress;
		this.serverAddress = serverAddress;
		
		try {
			messenger.start((fromAddress,data) -> {
				consumer.accept(Utils.fromGsonStrToObject(data, dataType));
			});
		} catch (MessengerException e) {
			this.serverAddress = originalServerAddress;
			throw new CommunicationFailure();
		}
		
	}
	
	public void kill()
	{
		messenger.stop();
	}

	public <T> void blockingSend(T data) {
		try {
			String payload = Utils.fromObjectToGsonStr(data);
			messenger.send(this.serverAddress, payload, false);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		} 
	}
	
	
	public <T, S> S sendRecieve(T data, Type responseType)
	{
		try {
			String str = messenger.sendAndBlockUntilResponseArrives(
					this.serverAddress, Utils.fromObjectToGsonStr(data));
			
			return Utils.fromGsonStrToObject(str, responseType);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		}
		
	}
	
	@Override
	public String toString() {

		return messenger.getAddress();
	}
	
}
