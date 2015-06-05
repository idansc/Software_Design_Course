/**
 * 
 */
package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.lib.clientserver.Client;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author idansc
 *
 */
class MessageDataConsumer implements Consumer<MessageData> {
	final Consumer<InstantMessage> _mc;
	final Function<String, Boolean> _frh;
	final BiConsumer<String, Boolean> _frc;
	Client _client;
	MessageDataConsumer(Client client,Consumer<InstantMessage> messageConsumer,
			Function<String, Boolean> friendshipRequestHandler,
			BiConsumer<String, Boolean> friendshipReplyConsumer){
		_mc = messageConsumer;
		_frh = friendshipRequestHandler;
		_frc = friendshipReplyConsumer;
		_client = client;
	}
	@Override
	public void accept(MessageData messageData){
		switch (messageData._clientTaskType) {
		case CLIENT_RECEIVED_MESSAGE:
			_mc.accept(messageData._message);
			break;
		case CLIENT_RECEIVED_FRIEND_REQUEST:{
			MessageData friendRequestReply = new MessageData(ClientTaskType.CLIENT_RECEIVED_FRIEND_REQUEST,
					_frh.apply(messageData._whom));
			_client.send(friendRequestReply);
		}
		case CLIENT_RECEIVED_FRIEND_REPLY:
			_frc.accept(messageData._whom,messageData._friendRequestAnswer);
		default:
			break;
		}
	}
}
