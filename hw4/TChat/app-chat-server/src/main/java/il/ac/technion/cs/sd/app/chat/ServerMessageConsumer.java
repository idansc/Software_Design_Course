package il.ac.technion.cs.sd.app.chat;

import java.util.function.Consumer;

class ServerMessageConsumer implements Consumer<ServerMessage> {

	@Override
	public void accept(ServerMessage serverMessage) {
		switch (serverMessage.serverTaskType) {
		case LOGIN:
			
			break;

		default:
			break;
		}
		
	}

}
