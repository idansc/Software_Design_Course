/**
 * 
 */
package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author idansc
 *
 */
public class TMsgIntegrationTest {
	private ServerMailApplication server;

	
	private ClientMsgApplication buildClient(String login) {
		return new ClientMsgApplication(server.getAddress(), login);
	}
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		server = new ServerMailApplication("Server");
		server.start(); // non-blocking
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		server.stop();
		
		server.clean();
		
	}

	@Test(timeout=3000)
	public void loginShouldGetOfflineMessages() throws InterruptedException{
		final BlockingQueue<InstantMessage>	messages = new LinkedBlockingQueue<>();
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.sendMessage("Ofer", "Y U NO REPLY");
		idan.sendMessage("Ofer", "cri everytime :( :(");
		idan.sendMessage("Ofer", "Okay I get it :( nevermind");
		
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->messages.add(x), x -> true, (x, y) -> {});
		
		assertEquals("Hi wanna be my friend?", messages.take().getContent());
		assertEquals("Y U NO REPLY", messages.take().getContent());		
		assertEquals("cri everytime :( :(", messages.take().getContent());
		assertEquals("Okay I get it :( nevermind", messages.take().getContent());
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	

	@Test(timeout=3000)
	public void loginShouldGetOfflineMessagesWithPersistent() throws InterruptedException{
		final BlockingQueue<InstantMessage>	messages = new LinkedBlockingQueue<>();
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.sendMessage("Ofer", "Y U NO REPLY");
		
		server.stop();
		server = new ServerMailApplication("Server");
		server.start();
		
		
		idan.sendMessage("Ofer", "cri everytime :( :(");
		idan.sendMessage("Ofer", "Okay I get it :( nevermind");
		

		
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->messages.add(x), x -> true, (x, y) -> {});
		
		assertEquals("Hi wanna be my friend?", messages.take().getContent());
		assertEquals("Y U NO REPLY", messages.take().getContent());		
		assertEquals("cri everytime :( :(", messages.take().getContent());
		assertEquals("Okay I get it :( nevermind", messages.take().getContent());
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	
	@Test(timeout=3000)
	public void loginShouldGetOfflineFriendRequest() throws InterruptedException {
		final BlockingQueue<String> friendshipRequest	= new LinkedBlockingQueue<>();
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.requestFriendship("Ofer");
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> friendshipRequest.add(x), (x, y) -> {});
		assertEquals("Idan", friendshipRequest.take());
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	@Test
	public void loginShouldGetOfflineFriendReplay() throws InterruptedException{
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.requestFriendship("Ofer");

		idan.logout();
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		idan.login(x->{}, x -> true,(x,y)->{
				assertEquals("Ofer", x);
				assertEquals(true, y);
			});
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	
	@Test
	public void loginShouldGetOfflineFriendReplayWithPersistant() throws InterruptedException{
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.requestFriendship("Ofer");

		server.stop();
		server = new ServerMailApplication("Server");
		server.start();
		
		idan.logout();
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		idan.login(x->{}, x -> true,(x,y)->{
				assertEquals("Ofer", x);
				assertEquals(true, y);
			});
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	
	@Test
	public void loginShouldGetOfflineFriendReplayWithPersistantInSameSever() throws InterruptedException{
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.requestFriendship("Ofer");

		server.stop();
		server.start();
		
		idan.logout();
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		idan.login(x->{}, x -> true,(x,y)->{
				assertEquals("Ofer", x);
				assertEquals(true, y);
			});
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	@Test
	public void noFriendShouldFailOnlineCheck(){
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x -> true, (x, y) -> {});
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		assertEquals(Optional.empty(), idan.isOnline("Ofer"));
		idan.logout();
		ofer.logout();

	}
	
	@Test
	public void noFriendShouldFailOnlineCheckWithPersistent(){
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x -> true, (x, y) -> {});
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		
		server.stop();
		server = new ServerMailApplication("Server");
		server.start();
		
		assertEquals(Optional.empty(), idan.isOnline("Ofer"));
		idan.logout();
		ofer.logout();
	}
	
	
	@Test
	public void onlineCheckReturnsGoodValues() throws InterruptedException{
		final BlockingQueue<String>	replayFriendRequest = new LinkedBlockingQueue<>();
		ClientMsgApplication EEEIdan = buildClient("EEEIdan");
		ClientMsgApplication EEEOfer = buildClient("EEEOfer");
		EEEIdan.login(x->{}, x->true, (x, y) -> {replayFriendRequest.add(x);});
		EEEOfer.login(x->{}, x -> true, (x, y) -> {});
		assertEquals(Optional.empty(), EEEIdan.isOnline("EEEOfer"));
		EEEIdan.requestFriendship("EEEOfer");
		assertEquals("EEEOfer", replayFriendRequest.take());
		assertEquals(Optional.empty(), EEEIdan.isOnline("Gal"));
		assertEquals(Optional.of(true), EEEIdan.isOnline("EEEOfer"));
		EEEOfer.logout();
		assertEquals(Optional.of(false), EEEIdan.isOnline("EEEOfer"));
		EEEIdan.logout();
	}
	
	
	@Test
	public void onlineCheckReturnsGoodValuesWithPersistent() throws InterruptedException{
		final BlockingQueue<String>	replayFriendRequest = new LinkedBlockingQueue<>();
		ClientMsgApplication EEEIdan = buildClient("EEEIdan");
		ClientMsgApplication EEEOfer = buildClient("EEEOfer");
		EEEIdan.login(x->{}, x->true, (x, y) -> {replayFriendRequest.add(x);});
		EEEOfer.login(x->{}, x -> true, (x, y) -> {});
		assertEquals(Optional.empty(), EEEIdan.isOnline("EEEOfer"));
		EEEIdan.requestFriendship("EEEOfer");
		assertEquals("EEEOfer", replayFriendRequest.take());
		
		server.stop();
		server.start();
		
		assertEquals(Optional.empty(), EEEIdan.isOnline("Gal"));
		
		server.stop();
		server = new ServerMailApplication("Server");
		server.start();
		
		assertEquals(Optional.of(true), EEEIdan.isOnline("EEEOfer"));
		EEEOfer.logout();
		assertEquals(Optional.of(false), EEEIdan.isOnline("EEEOfer"));
		EEEIdan.logout();
	}	
	

}
