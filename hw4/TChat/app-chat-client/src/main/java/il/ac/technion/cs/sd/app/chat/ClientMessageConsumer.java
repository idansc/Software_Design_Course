package il.ac.technion.cs.sd.app.chat;

import java.util.function.Consumer;

public class ClientMessageConsumer implements Consumer<ClientMessage> {

	Consumer<ChatMessage> _chatMessageConsumer;
	Consumer<RoomAnnouncement> _announcementConsumer;
	
	public ClientMessageConsumer(Consumer<ChatMessage> chatMessageConsumer,
			Consumer<RoomAnnouncement> announcementConsumer) {
		super();
		this._chatMessageConsumer = chatMessageConsumer;
		this._announcementConsumer = announcementConsumer;
	}

	@Override
	public void accept(ClientMessage messageData) {
		switch (messageData.type) {
		case MESSAGE:
			_chatMessageConsumer.accept(messageData.getMessage());
			break;
		case ANNOUNCEMENT:
			_announcementConsumer.accept(messageData.getAnnouncement());
			break;
		default:
			break;
		}
		
	}
	
	
}
