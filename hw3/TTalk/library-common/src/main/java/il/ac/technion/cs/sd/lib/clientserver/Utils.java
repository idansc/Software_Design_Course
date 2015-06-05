package il.ac.technion.cs.sd.lib.clientserver;

/**
 * Common utilities.
 */
public class Utils {
	
	//TODO
	public <T> T fromGsonStrToObject(String gsonStr, Class<T> type)
	{
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		Gson gson = new GsonBuilder().create();
		
	}
}
