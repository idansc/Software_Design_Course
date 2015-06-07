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
		server = new ServerMailApplication("Server456");//TODO
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
		ClientMsgApplication idan = buildClient("YYYIdan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("YYYOfer", "Hi wanna be my friend?");
		idan.sendMessage("YYYOfer", "Y U NO REPLY");
		idan.sendMessage("YYYOfer", "cri everytime :( :(");
		idan.sendMessage("YYYOfer", "Okay I get it :( nevermind");
		
		ClientMsgApplication ofer = buildClient("YYYOfer");
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
		ClientMsgApplication idan = buildClient("YYYIdan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("YYYOfer", "Hi wanna be my friend?");
		idan.requestFriendship("YYYOfer");
		ClientMsgApplication ofer = buildClient("YYYOfer");
		ofer.login(x->{}, x -> friendshipRequest.add(x), (x, y) -> {});
		assertEquals("YYYIdan", friendshipRequest.take());
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	@Test
	public void loginShouldGetOfflineFriendReplay() throws InterruptedException{
		ClientMsgApplication idan = buildClient("YYYIdan");
		idan.login(x -> {}, x ->(x!="Gal")? true:false, (x, y) -> {});
		idan.sendMessage("YYYOfer", "Hi wanna be my friend?");
		idan.requestFriendship("YYYOfer");

		idan.logout();
		ClientMsgApplication ofer = buildClient("YYYOfer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		idan.login(x->{}, x -> true,(x,y)->{
				assertEquals("YYYOfer", x);
				assertEquals(true, y);
			});
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	@Test
	public void noFriendShouldFailOnlineCheck(){
		ClientMsgApplication idan = buildClient("YYYIdan");
		idan.login(x -> {}, x -> true, (x, y) -> {});
		ClientMsgApplication ofer = buildClient("YYYOfer");
		ofer.login(x->{}, x -> true, (x, y) -> {});
		assertEquals(Optional.empty(), idan.isOnline("YYYOfer"));
		idan.logout();
		ofer.logout();
		
		idan.stop();
		ofer.stop();
	}
	
	@Test
	public void onlineCheckReturnsGoodValues(){
		ClientMsgApplication idan = buildClient("YYYIdan");
		idan.requestFriendship("YYYOfer");
		
		idan.login(x -> {}, x->true, (x, y) -> {});
//		ClientMsgApplication ofer = buildClient("YYYOfer");
//		ofer.login(x->{}, x -> true, (x, y) -> {});
//		
//		assertEquals(Optional.of(true), idan.isOnline("YYYOfer"));
//		
//		ofer.logout();
		
//		assertEquals(Optional.of(false), idan.isOnline("YYYOfer"));
		
		idan.logout();

		idan.stop();
//		ofer.stop();
	}	

}
