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
		if(messageData._serverTaskType==null){
			return;
		}
		switch (messageData._serverTaskType) {
		case SEND_MESSAGE_TASK:
			_mc.accept(messageData._message);
			break;
		case REQUEST_FRIENDSHIP_TASK:{
			Boolean answer = _frh.apply(messageData._from);
			MessageData friendRequestReply = new MessageData(ServerTaskType.CLIENT_REPLY_FRIEND_REQUEST_TASK,answer,
					messageData._from
					);
			_client.send(friendRequestReply);
		}
		case CLIENT_REPLY_FRIEND_REQUEST_TASK:
			_frc.accept(messageData._from,messageData._friendRequestAnswer);
		default:
			break;
		}
	}
}
