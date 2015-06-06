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
	private BlockingQueue<Boolean>			friendshipReplies	= new LinkedBlockingQueue<>();
	private BlockingQueue<InstantMessage>	messages			= new LinkedBlockingQueue<>();
	
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
	public void testLogin() {
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false
			, (x, y) -> friendshipReplies.add(y));
		idan.sendMessage("OFer", "Hi wanna be my friend?");
		idan.sendMessage("OFer", "Y U NO REPLY");
		idan.sendMessage("OFer", "cri everytime :( :(");
		idan.sendMessage("OFer", "Okay I get it :( nevermind");
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{
			if(x=="Hi wanna be my friend?")
				ofer.sendMessage("Idan", "Yes);
			if(x=="cri everytime :( :(")
				ofer.
		}, friendshipRequestHandler, friendshipReplyConsumer);
		assertEquals(Optional.empty(), gal.isOnline("Itay")); // Itay isn't a friend
		gal.sendMessage("Itay", "Hi");
		ClientMsgApplication itay = buildClient("Itay");
		itay.login(x -> messages.add(x), x -> true, (x, y) -> {});
		assertEquals(messages.take(), new InstantMessage("Gal", "Itay", "Hi")); // Itay received the message as soon as he logged in
		gal.requestFriendship("Itay");
		assertEquals(true, friendshipReplies.take()); // itay auto replies yes
		assertEquals(Optional.of(true), gal.isOnline("Itay")); // itay is a friend and is online
		itay.logout();
		assertEquals(Optional.of(false), gal.isOnline("Itay")); // itay is a friend and is offline
		gal.logout();
	}

}
