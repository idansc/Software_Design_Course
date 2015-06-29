package il.ac.technion.cs.sd.lib;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PayLoad {
	public final String payload;
	public final String sender;
	public final boolean respond;
	private PayLoad() {
		payload = null;
		sender = null;
		respond=false;
	}
	
	public PayLoad(String payload,String sender,boolean respond) {
		this.payload = payload;
		this.sender = sender;
		this.respond=respond;
	}
	
	public String toJson(){
		return new Gson().toJson(this);
	}
	
	public static PayLoad fromString(String json){
		return new Gson().fromJson(json,PayLoad.class);
	}
}
