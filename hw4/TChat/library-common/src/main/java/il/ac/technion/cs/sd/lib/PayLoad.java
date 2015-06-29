package il.ac.technion.cs.sd.lib;

import com.google.gson.Gson;

public class PayLoad {
	public String payload;
	public String sender;
	public boolean respond;
	Long messageId;
	Long responseTargetId; 
	
	public  PayLoad() {
		payload = null;
		sender = null;
		respond=false;
	}
	
	public PayLoad(String payload,String sender,boolean respond) {
		this.payload = payload;
		this.sender = sender;
		this.respond=respond;
	}
	
	PayLoad(long messageId, Long respnseTargetId, String payload, String sender) {
		this.messageId = messageId;
		this.responseTargetId = respnseTargetId;
		this.payload = payload;
		this.sender = sender;
	}
	
	public String toJson(){
		return new Gson().toJson(this);
	}
	
	public static PayLoad fromString(String json){
		return new Gson().fromJson(json,PayLoad.class);
	}
}
