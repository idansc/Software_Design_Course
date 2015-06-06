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
	private ServerMailApplication			server				= new ServerMailApplication("Server");

	
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
		idan.sendMessage("Ofer", "Hi wanna be my friend?");
		idan.sendMessage("Ofer", "Y U NO REPLY");
		idan.sendMessage("Ofer", "cri everytime :( :(");
		idan.sendMessage("Ofer", "Okay I get it :( nevermind");
		
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->messages.add(x), x -> true, (x, y) -> {});
		
		assertEquals("Hi wanna be my friend?", messages.take());
		assertEquals("Y U NO REPLY", messages.take());		
		assertEquals("cri everytime :( :(", messages.take());
		assertEquals("Okay I get it :( nevermind", messages.take());
	}
	@Test
	public void testLogin() {

//
//		final BlockingQueue<Boolean> friendshipReplies	= new LinkedBlockingQueue<>();
//
//
//
//		ClientMsgApplication gal = buildClient("Gal");
//		ofer.login(x->{}, x->true, (x,y)->{});
//		ofer.requestFriendship("Gal");
//		gal.login(x -> {}, x -> true, (x, y) -> {});
//		idan.login(x -> {}, x ->(x!="Gal")? true:false
//			, (x, y) -> {});
//
//		idan.requestFriendship("Ofer");
//		
//
//		assertEquals(Optional.empty(), gal.isOnline("Itay")); // Itay isn't a friend
//		gal.sendMessage("Itay", "Hi");
//		ClientMsgApplication itay = buildClient("Itay");
//		itay.login(x -> messages.add(x), x -> true, (x, y) -> {});
//		assertEquals(messages.take(), new InstantMessage("Gal", "Itay", "Hi")); // Itay received the message as soon as he logged in
//		gal.requestFriendship("Itay");
//		assertEquals(true, friendshipReplies.take()); // itay auto replies yes
//		assertEquals(Optional.of(true), gal.isOnline("Itay")); // itay is a friend and is online
//		itay.logout();
//		assertEquals(Optional.of(false), gal.isOnline("Itay")); // itay is a friend and is offline
//		gal.logout();
	}

}
