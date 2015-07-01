package il.ac.technion.cs.sd.app.msg;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerImpl;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.*;
import org.junit.rules.*;

public class TMsgTest {
	// giving a very large time, in case of messaging API failure and waiting
	private static final long	SHORT_TIME	= 5000;
	private static final int	LARGE_N			= 1000; //TODO: pass with 2.
	
	@Rule
	public Timeout				timeout		=  Timeout.seconds(100000); //TODO: Timeout.seconds(LARGE_N / 2 + 10)
	
	private static void swallowException(RunnableWithExceptions r) {
		try {
			r.run();
		} catch (Throwable e) {}
	}
	
	private static void resetCounter() {
		MessengerImpl.reset();
	}
	
	private static void maxMessages(int n) {
		assertThat(MessengerImpl.numberOfMessagesSent(), is(lessThanOrEqualTo(n)));
	}
	
	private static Random	r	= new Random();
	
	public static String generateRandomString(int n) throws Exception {
		StringBuilder sb = new StringBuilder();
		repeat(n, () -> sb.append((char)('a' + r.nextInt(26))));
		assert sb.length() == n;
		return sb.toString();
	}
	
	public static void repeat(int n, RunnableWithExceptions r) throws Exception {
		for (int i = 0; i < n; i++)
			r.run();
	}
	
	private InstantMessage pollMessage(String name) throws InterruptedException {
		return messages.get(name).poll(SHORT_TIME, TimeUnit.MILLISECONDS);
	}
	
	private boolean pollRequests(String name) throws InterruptedException {
		return requestsReplies.get(name).poll(SHORT_TIME, TimeUnit.MILLISECONDS);
	}
	
	@FunctionalInterface
	private static interface RunnableWithExceptions {
		void run() throws Exception;
	}
	
	private ServerMailApplication						server			= new ServerMailApplication("Server");
	// all listened to incoming messages will be written here
	// a blocking queue is used to overcome threading issues
	private Map<String, BlockingQueue<Boolean>>			requestsReplies	= new HashMap<>();
	private Map<String, BlockingQueue<InstantMessage>>	messages		= new HashMap<>();
	private Collection<ClientMsgApplication>			clients			= new LinkedList<>();
	
	private ClientMsgApplication buildClient(String login) {
		ClientMsgApplication $ = new ClientMsgApplication(server.getAddress(), login);
		clients.add($);
		return $;
	}
	
	private ClientMsgApplication loginClient(String name) {
		ClientMsgApplication $ = buildClient(name);
		return relogin($, name);
	}
	
	private ClientMsgApplication relogin(ClientMsgApplication $, String name) {
		requestsReplies.put(name, new LinkedBlockingQueue<>());
		messages.put(name, new LinkedBlockingQueue<>());
		$.login(x -> messages.get(name).add(x),
				x -> true,
				(x, y) -> requestsReplies.get(name).add(y));
		return $;
	}
	
	@Before public void setup() throws Exception {
		MessengerImpl.reset();
		forceClean();
		server = new ServerMailApplication("Server");
		swallowException(() -> {
			server.start();
			server.stop();
			server.clean();
			server = new ServerMailApplication("Server");
		});
		server.start(); // non-blocking
	}
	
	@After public void teardown() throws Exception {
		clients.forEach(c -> swallowException(() -> c.stop()));
		swallowException(() -> server.stop());
		swallowException(() -> server.clean());
		forceClean();
		Thread.sleep(1000);
	}

	// In case of timeout, sometimes clean isn't run propely
	// Let this be a lesson kids, on the perils of using singletons and global state
		private void forceClean() throws RemoteException, AccessException {
		Registry reg = LocateRegistry.getRegistry();
		for (String str: reg.list())
			swallowException(() -> reg.unbind(str));
		assertTrue(reg.list().length == 0);
	}
	
	@Test public void basicTest() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		assertEquals(Optional.empty(), gal.isOnline("Itay"));
		gal.sendMessage("Itay", "Hi");
		ClientMsgApplication itay = loginClient("Itay");
		assertEquals(pollMessage("Itay"), new InstantMessage("Gal", "Itay", "Hi"));
		gal.requestFriendship("Itay");
		assertEquals(true, pollRequests("Gal"));
		assertEquals(Optional.of(true), gal.isOnline("Itay"));
		itay.logout();
		assertEquals(Optional.of(false), gal.isOnline("Itay"));
		gal.logout();
	}
	
	@Test public void sendOnlineMessage() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		loginClient("Itay");
		gal.sendMessage("Itay", "Hello");
		assertEquals(messages.get("Itay").poll(SHORT_TIME, TimeUnit.MILLISECONDS),
				new InstantMessage("Gal", "Itay", "Hello"));
	}
	
	@Test public void sendOnlineFriendRequest() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		loginClient("Itay");
		gal.requestFriendship("Itay");
		assertEquals(true, pollRequests("Gal"));
	}
	
	@Test public void sendOnlineFriendRequestThatIsntAccepted() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		ClientMsgApplication lag = loginClient("Lag");
		ClientMsgApplication itay = buildClient("Itay");
		itay.login(x -> {}, s -> s.equals("Gal") ? true : false, (x, y) -> {});
		gal.requestFriendship("Itay");
		lag.requestFriendship("Itay");
		assertEquals(true, pollRequests("Gal"));
		assertEquals(false, pollRequests("Lag"));
		assertEquals(Optional.empty(), lag.isOnline("Itay")); // ;(
		itay.logout();
		assertEquals(Optional.empty(), lag.isOnline("Itay"));
		
	}
	
	@Test public void shouldntAnswerUntilClientLogsIn() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		assertEquals(Optional.empty(), gal.isOnline("Itay")); // ;(
	}
	
	@Test public void offlineMessaging() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		gal.sendMessage("Itay", "Howdy");
		gal.logout();
		ClientMsgApplication itay = loginClient("Itay");
		assertEquals(pollMessage("Itay"), new InstantMessage("Gal", "Itay", "Howdy"));
		itay.sendMessage("Gal", "Sup");
		itay.logout();
		relogin(gal, "Gal");
		assertEquals(pollMessage("Gal"), new InstantMessage("Itay", "Gal", "Sup"));
	}
	
	@Test public void offlineFriendRequests() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		ClientMsgApplication lag = loginClient("Lag");
		gal.requestFriendship("Itay");
		lag.requestFriendship("Itay");
		gal.logout();
		lag.logout();
		ClientMsgApplication itay = buildClient("Itay");
		itay.login(x -> {}, s -> s.equals("Gal") ? true : false, (x, y) -> {});
		itay.logout();
		gal = relogin(gal, "Gal");
		lag = relogin(lag, "Lag");
		assertEquals(true, requestsReplies.get("Gal").poll(SHORT_TIME, TimeUnit.MILLISECONDS));
		assertEquals(false, requestsReplies.get("Lag").poll(SHORT_TIME, TimeUnit.MILLISECONDS));
		assertEquals(Optional.of(false), gal.isOnline("Itay"));
		assertEquals(Optional.empty(), lag.isOnline("Itay"));
		itay.login(x -> {}, s -> s.equals("Gal") ? true : false, (x, y) -> {});
		assertEquals(Optional.of(true), gal.isOnline("Itay"));
		assertEquals(Optional.empty(), lag.isOnline("Itay"));
	}
	
	@Test public void changingFriendRequestPolicy() throws Exception {
		ClientMsgApplication lag = loginClient("Lag");
		lag.requestFriendship("Itay");
		ClientMsgApplication itay = buildClient("Itay");
		itay.login(x -> {}, s -> false, (x, y) -> {});
		assertEquals(false, requestsReplies.get("Lag").poll(SHORT_TIME, TimeUnit.MILLISECONDS));
		assertEquals(Optional.empty(), lag.isOnline("Itay"));
		itay.logout();
		assertEquals(Optional.empty(), lag.isOnline("Itay"));
		// policy change shouldn't matter, because no new request was issued
		itay.login(x -> {}, s -> true, (x, y) -> {});
		assertEquals(Optional.empty(), lag.isOnline("Itay"));
		lag.requestFriendship("Itay"); // request friendship again
		assertEquals(true, requestsReplies.get("Lag").poll(SHORT_TIME, TimeUnit.MILLISECONDS));
		itay.logout();
		assertEquals(Optional.of(false), lag.isOnline("Itay"));
		// policy change shouldn't matter, because we are already friends... FOREVER ಠ_ಠ
		itay.login(x -> {}, s -> false, (x, y) -> {});
		assertEquals(Optional.of(true), lag.isOnline("Itay"));
	}
	
	@Test public void messagesShouldComeInOrder() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		ClientMsgApplication lag = loginClient("Lag");
		ClientMsgApplication ytai = loginClient("Ytai");
		gal.sendMessage("Itay", "This");
		lag.sendMessage("Itay", "is");
		ytai.sendMessage("Itay", "a");
		lag.sendMessage("Itay", "test");
		loginClient("Itay");
		assertEquals(new InstantMessage("Gal", "Itay", "This"), pollMessage("Itay"));
		assertEquals(new InstantMessage("Lag", "Itay", "is"), pollMessage("Itay"));
		assertEquals(new InstantMessage("Ytai", "Itay", "a"), pollMessage("Itay"));
		assertEquals(new InstantMessage("Lag", "Itay", "test"), pollMessage("Itay"));
	}
	
	// message counting
	@Test public void loggingInShouldInvolveAtMost3Messages() throws Exception {
		repeat(LARGE_N, () -> {
			ClientMsgApplication $ = loginClient(generateRandomString(5));
			$.sendMessage("Itay", generateRandomString(5));
			$.logout();
			$.stop();
		});
		resetCounter();
		loginClient("Itay");
		repeat(LARGE_N, () -> pollMessage("Itay")); // makes sure all messages have been consumed
		maxMessages(2);
	}
	
	@Test public void sendingAMessageShouldInvolve2MessagesIfOffline() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		resetCounter();
		gal.sendMessage("Itay", "test");
		maxMessages(2);
	}
	
	@Test public void testContinuity() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		gal.sendMessage("Itay", "Yo");
		gal.requestFriendship("Itay");
		gal.logout();
		gal.stop();
		server.stop();
		server = new ServerMailApplication("Server");
		server.start();
		ClientMsgApplication itay = loginClient("Itay");
		assertEquals(pollMessage("Itay"), new InstantMessage("Gal", "Itay", "Yo"));
		itay.logout();
		
		server.stop();
		server = new ServerMailApplication("Server");
		server.start();
		gal = relogin(gal, "Gal");
		assertTrue(pollRequests("Gal"));
		gal.logout();
	}

	@Test public void sendingAMessageShouldInvolve4MessagesIfOnline() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		loginClient("Itay");
		resetCounter();
		gal.sendMessage("Itay", "test");
		pollMessage("Itay");
		maxMessages(4);
	}
	
	@Test public void sendingAFriendRequestShouldInvolve2MessagesIfOffline() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		resetCounter();
		gal.requestFriendship("Itay");
		maxMessages(2);
	}
	
	@Test public void sendingAFriendRequestShouldInvolve6MessagesIfOnline() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		ClientMsgApplication itay = loginClient("Itay");
		resetCounter();
		gal.requestFriendship("Itay");
		assertTrue(pollRequests("Gal"));
		maxMessages(6); // 2 for gal-server, 2 for server-itay, 2 for sending reply to gal
		itay.logout();
		itay.login(x -> {}, s -> false, (x, y) -> {});
		resetCounter();
		ClientMsgApplication lag = loginClient("Lag");
		lag.requestFriendship("Itay");
		maxMessages(6);
	}
	
	@Test public void isOnlineShouldInvolve2MessagesAlways() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		ClientMsgApplication itay = loginClient("Itay");
		gal.requestFriendship("Itay");
		Thread.sleep(300); //TODO: CRITICAL
		resetCounter();
		gal.isOnline("Itay");
		maxMessages(2);
		gal.requestFriendship("Itay");
		pollRequests("Gal");
		Thread.sleep(300); //TODO: CRITICAL
		resetCounter();
		gal.isOnline("Itay");
		// server shouldn't ask itay if he is off/online, it should know via login
		maxMessages(2);
		itay.logout();
		Thread.sleep(300); //TODO: added
		resetCounter();
		gal.isOnline("Itay");
		maxMessages(2);
		itay.login(x -> {}, s -> false, (x, y) -> {});
		Thread.sleep(300); //TODO: added
		resetCounter();
		gal.isOnline("Itay");
		maxMessages(2);
	}
	
	@Test public void logoutShouldInvolve2Messages() throws Exception {
		ClientMsgApplication gal = loginClient("Gal");
		resetCounter();
		gal.logout();
		maxMessages(2);
	}
	
	//timings
	private static long time(RunnableWithExceptions r) {
		long start = System.currentTimeMillis();
		swallowException(() -> r.run());
		return System.currentTimeMillis() - start;
	}
	
	private void fillServer() throws Exception {
		repeat(LARGE_N, () -> {
			// create lots of users that spam 
				ClientMsgApplication $ = loginClient(generateRandomString(10));
				$.sendMessage(generateRandomString(5), generateRandomString(5));
				$.logout();
				$.stop();
			});
		server.stop();
		server = new ServerMailApplication("Server");
		server.start();
	}
	
	@Test public void loginTiming() throws Exception {
		Supplier<Long> timedLogging = () -> time(() -> {
			ClientMsgApplication gal = loginClient("Gal");
			repeat(LARGE_N, () -> pollMessage("Gal"));
			gal.logout();
			gal.stop();
		});
		fillServer();
		repeat(LARGE_N, () -> {
			ClientMsgApplication $ = loginClient(generateRandomString(5));
			$.sendMessage("Gal", generateRandomString(5));
			$.logout();
			$.stop();
		});
		long t1 = timedLogging.get();
		fillServer();
		repeat(LARGE_N, () -> {
			ClientMsgApplication $ = loginClient(generateRandomString(5));
			$.sendMessage("Gal", generateRandomString(5));
			$.logout();
			$.stop();
		});
		long t2 = timedLogging.get();
		assertThat(t2, is(lessThanOrEqualTo(3 * t1)));
	}
	
	@Test public void sendMessageTimings() throws Exception {
		
		Supplier<Long> timedLogging = () -> time(() -> {
			ClientMsgApplication gal = loginClient("Gal");
			gal.sendMessage(generateRandomString(5), generateRandomString(5));
			gal.sendMessage("Itay", generateRandomString(5));
			ClientMsgApplication $ = loginClient(generateRandomString(10));
			$.sendMessage("Gal", generateRandomString(5));
			$.logout();
			$.stop();
			gal.logout();
			gal.stop();
		});
		fillServer();
		long t1 = timedLogging.get();
		fillServer();
		long t2 = timedLogging.get();
		assertThat(t2, is(lessThanOrEqualTo(10000 + t1))); // constant time, some overhead for failures
	}
	
	@Test public void requestFriendshipTimings() throws Exception {
		Supplier<Long> timedLogging = () -> time(() -> {
			ClientMsgApplication gal = loginClient("Gal");
			gal.requestFriendship(generateRandomString(5));
			gal.requestFriendship("Itay");
			ClientMsgApplication $ = loginClient(generateRandomString(10));
			$.requestFriendship("Gal");
			$.logout();
			$.stop();
			gal.logout();
			gal.stop();
		});
		fillServer();
		long t1 = timedLogging.get();
		fillServer();
		long t2 = timedLogging.get();
		assertThat(t2, is(lessThanOrEqualTo(10000 + t1))); // constant time, some overhead for failures
	}
	
	@Test public void isOnlineTiming() throws Exception {
		Supplier<Long> timedLogging = () -> time(() -> {
			ClientMsgApplication gal = loginClient("Gal");
			gal.requestFriendship(generateRandomString(5));
			gal.requestFriendship("Itay");
			gal.isOnline("Itay");
			gal.isOnline(generateRandomString(10));
			String randomName = generateRandomString(10);
			ClientMsgApplication $ = loginClient(randomName);
			$.requestFriendship("Gal");
			assertTrue(pollRequests(randomName));
			$.isOnline("Gal");
			$.logout();
			$.stop();
			gal.logout();
			gal.stop();
		});
		fillServer();
		long t1 = timedLogging.get();
		fillServer();
		long t2 = timedLogging.get();
		assertThat(t2, is(lessThanOrEqualTo(10000 + t1))); // constant time, some overhead for failures
	}
}
