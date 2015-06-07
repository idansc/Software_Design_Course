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
	}
	
	@Test
	public void noFriendShouldFailOnlineCheck(){
		ClientMsgApplication idan = buildClient("Idan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		ClientMsgApplication ofer = buildClient("Ofer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		assertEquals(Optional.empty(), idan.isOnline("Ofer"));
		idan.logout();
		ofer.logout();
	}
//	
//	@Test
//	public void onlineCheckReturnsGoodValues(){
//		ClientMsgApplication idan = buildClient("Idan");
//		idan.requestFriendship("Ofer");
//		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
//		ClientMsgApplication ofer = buildClient("Ofer");
//		ofer.login(x->{}, x -> true, (x, y) -> {});
//		
//		assertEquals(Optional.of(true), idan.isOnline("Ofer"));
//		ofer.logout();
//		assertEquals(Optional.of(false), idan.isOnline("Ofer"));
//	}	

}
