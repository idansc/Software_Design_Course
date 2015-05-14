package il.ac.technion.cs.sd.msg;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.After;
import org.junit.Test;

public class MessengerTest {

	private final MessengerFactory 		factory		= new MessengerFactory();

	// a helper method and list for creating messengers,
	// so we can remember to kill them
	private final Collection<Messenger> messengers 	= new ArrayList<>();
	private Messenger startAndAddToList() throws Exception {
		Messenger $ = factory.start(messengers.size() + "");
		messengers.add($);
		return $;
	}

	@After
	public void teardown() throws Exception {
		// it is very important to kill all messengers,
		// to free their address and more importantly, their daemon threads
		for (Messenger m: messengers)
			try {
				m.kill();
			} catch (Exception e) {

			}
	}

	@Test(expected = RuntimeException.class)
	public void shouldThrowAnExceptionOnNullAddress() throws Exception {
		factory.start(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowAnExceptionOnEmptyAddress() throws Exception {
		factory.start("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowAnExceptionOnWhitespaceAddress() throws Exception {
		factory.start("    ");
	}

	@Test(expected = MessengerException.class)
	public void shouldThrowAnExceptionOnTwoIdentitcalNames() throws Exception {
		factory.start("a");
		factory.start("a");
	}

	@Test
	public void canCreateNewObjectWithSameAddressAfterKilled() throws Exception {
		factory.start("a").kill();
		factory.start("a");
	}

	@Test
	public void shouldReceiveASendMessage() throws Exception {
		Messenger m1 = startAndAddToList();
		Messenger m2 = startAndAddToList();
		m1.send(m2.getAddress(), "hello".getBytes());
		assertArrayEquals(m2.listen(), "hello".getBytes());
		m2.send(m1.getAddress(), "hi back".getBytes());
		assertArrayEquals(m1.listen(), "hi back".getBytes());

	}

	@Test
	public void tryListenShouldReturnEmptyWhenNoMessageExists()
			throws Exception {
		Messenger m1 = startAndAddToList();
		assertEquals(m1.tryListen(), Optional.empty());
	}

	@Test
	public void tryListenShouldReturnMessageWhenExists() throws Exception {
		Messenger m1 = startAndAddToList();
		Messenger m2 = startAndAddToList();
		m2.send(m1.getAddress(), "test".getBytes());
		assertArrayEquals(m1.tryListen().get(), "test".getBytes());
	}

	@Test(expected = MessengerException.class)
	public void cannotBeKilledTwice() throws Exception {
		Messenger $ = startAndAddToList();
		$.kill();
		$.kill();
	}

	@Test
	public void shouldBlockWhenNoMessage() throws Exception {
		Messenger $ = startAndAddToList();
		new Thread(() -> {
			try {
				$.listen();
				fail("Should have blocked");
			} catch (MessengerException e) {
				throw new AssertionError(e);
			}
		}).start();
		Thread.sleep(100);
	}

	@Test(timeout = 20)
	public void shouldntBlockWhenThereIsAMessage() throws Exception {
		Messenger $ = startAndAddToList();
		startAndAddToList().send($.getAddress(), new byte[0]);
		$.listen();
	}
}
