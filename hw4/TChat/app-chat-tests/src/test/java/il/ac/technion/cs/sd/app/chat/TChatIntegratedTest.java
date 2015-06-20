package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TChatIntegratedTest {

	private List<ClientChatApplication> clients = new LinkedList<>();
	
	private ServerChatApplication server;
	
	private void addClient(String serverAddress, String username)
	{
		ClientChatApplication $ = new ClientChatApplication(serverAddress,username);
		clients.add($);
	}
	
	
	@Before
	public void setUp() throws Exception {
		server = new ServerChatApplication("server_" + UUID.randomUUID());
		server.clean();
		server.start();
		
		addClient(server.getAddress(), "client_" + UUID.randomUUID());
	}

	@After
	public void tearDown() throws Exception {
		clients.stream().forEach(c -> {
			c.logout();
			c.stop();
		});
		
		server.stop();
		server.clean();
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
