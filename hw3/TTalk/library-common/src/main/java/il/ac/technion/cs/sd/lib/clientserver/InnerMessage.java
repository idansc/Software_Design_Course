package il.ac.technion.cs.sd.lib.clientserver;

public class InnerMessage {
	@SuppressWarnings("unused")
	InnerMessage() {}
	InnerMessage(long messageId, Long respnseTargetId, String data, String fromAddress) {
		this.messageId = messageId;
		this.responseTargetId = respnseTargetId;
		this.data = data;
		this.fromAddress = fromAddress;
	}

	Long messageId;
	
	/* The id of the message that this message is the response to, 
	 * or null if this message is not a response. */
	Long responseTargetId; 
	
	String data;
	
	// The address of the sender.
	String fromAddress;
	
	
	@Override
	public String toString()
	{
		return "[from:" + showable(fromAddress) + ",messageId=" + messageId + 
		"," + "responseTargetId=" + responseTargetId + "]"; 
	}
	
	//TODO:DELETE
	public String TMP__toString()
	{
		return "[from:" + showable(fromAddress) + ",messageId=" + messageId + 
		"," + "responseTargetId=" + responseTargetId + "]"; 
	}
	
	private String showable(String str)
	{
		if (str.length() <= 15)
			return str;
		return str.substring(0, 15);
	}
}
