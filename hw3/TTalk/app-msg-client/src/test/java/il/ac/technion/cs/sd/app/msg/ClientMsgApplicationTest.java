package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import il.ac.technion.cs.sd.lib.clientserver.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class ClientMsgApplicationTest {
	ClientMsgApplication idan;
	Client mockClient;
	@Before
	public void setUp() throws Exception {
		idan = new ClientMsgApplication("Tech", "Idan");
		mockClient = Mockito.mock(Client.class);
		idan.setClient(mockClient);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void loginShouldHandleOfflineMessage() throws InterruptedException {
		final BlockingQueue<InstantMessage>	messages = new LinkedBlockingQueue<>();
		List<MessageData> messageData = new LinkedList<MessageData>();
		messageData.add(new MessageData(ServerTaskType.SEND_MESSAGE_TASK,new InstantMessage("Idan", "Ofer", "Hi")));
		messageData.add(new MessageData(ServerTaskType.SEND_MESSAGE_TASK,new InstantMessage("Idan", "Ofer", "There")));
		messageData.add(new MessageData(ServerTaskType.SEND_MESSAGE_TASK,new InstantMessage("Idan", "Ofer", "My")));
		messageData.add(new MessageData(ServerTaskType.SEND_MESSAGE_TASK,new InstantMessage("Idan", "Ofer", "Friend")));
		Mockito.when(mockClient.sendAndBlockUntilResponseArrives(Mockito.any(), Mockito.any())).thenReturn(Optional.of(messageData));
		
		idan.login(x->messages.add(x), x -> true, (x, y) -> {});
		
		assertEquals("Hi", messages.take().getContent());
		assertEquals("There", messages.take().getContent());		
		assertEquals("My", messages.take().getContent());
		assertEquals("Friend", messages.take().getContent());
	}
	
	@Test
	public void loginShouldHandleOfflineFriendRequest() throws InterruptedException {
		final BlockingQueue<String> friendshipRequest	= new LinkedBlockingQueue<>();
		List<MessageData> messageData = new LinkedList<MessageData>();
		messageData.add(new MessageData(ServerTaskType.SEND_MESSAGE_TASK,new InstantMessage("Idan", "Ofer", "Hi")));
		MessageData friendRequestSend = new MessageData(ServerTaskType.REQUEST_FRIENDSHIP_TASK,"Idan");
		friendRequestSend._from="Ofer";
		messageData.add(friendRequestSend);
		Mockito.when(mockClient.sendAndBlockUntilResponseArrives(Mockito.any(), Mockito.any())).thenReturn(Optional.of(messageData));
		
		idan.login(x->{}, x ->{if(x=="Ofer") friendshipRequest.add("Ofer"); return true;}, (x, y) -> {});
		
		assertEquals("Ofer", friendshipRequest.take());
	}
	

}
