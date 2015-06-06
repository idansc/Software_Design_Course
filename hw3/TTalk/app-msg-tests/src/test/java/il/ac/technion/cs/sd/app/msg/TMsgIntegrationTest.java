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
	private ServerMailApplication server = new ServerMailApplication("Server");

	
	private ClientMsgApplication buildClient(String login) {
		return new ClientMsgApplication(server.getAddress(), login);
	}
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
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

	@Test
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
	}
	
	@Test
	public void loginShouldGetOfflineFriendRequest() throws InterruptedException {
		final BlockingQueue<String> friendshipRequest	= new LinkedBlockingQueue<>();
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.requestFriendship("Ofer");
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> friendshipRequest.add(x), (x, y) -> {});
		assertEquals("Idan", friendshipRequest.take());
	}
	
	@Test
	public void loginShouldGetOfflineFriendReplay() throws InterruptedException{
		final BlockingQueue<Boolean> friendshipReply	= new LinkedBlockingQueue<>();
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.requestFriendship("Ofer");

		idan.logout();
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		idan.login(x->{}, x -> true,(x,y)->{if(x=="Ofer") friendshipReply.add(y);});
		assertEquals(true, friendshipReply.take());
	}
	
	@Test
	public void noFriendShouldFailOnlineCheck(){
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		assertEquals(Optional.empty(), idan.isOnline("Ofer"));
	}
	
	@Test
	public void onlineCheckReturnsGoodValues(){
		ClientMsgApplication idan = buildClient("Idan");
		idan.requestFriendship("Ofer");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		
		assertEquals(Optional.of(true), idan.isOnline("Ofer"));
		ofer.logout();
		assertEquals(Optional.of(false), idan.isOnline("Ofer"));
	}	

}
