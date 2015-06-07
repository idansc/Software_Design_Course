package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientServerIntegratedTest {

	final static int CLIENTS_NUM = 4;

	private Server server1;
	private List<Client> clients = createClients(CLIENTS_NUM);


	Random rnd = new Random(); 

	int tmp; // for lambda closure.

	List<Client> createClients(int clientsNum)
	{
		List<Client> $ = new LinkedList<>();

		for (int i=0; i<clientsNum; i++)
		{
			$.add(new Client("client_"+UUID.randomUUID().toString()));
		}
		return $;
	}

	private class POJO1
	{
		public int i;
		public String str;
		POJO1(int i, String str) {
			this.i = i;
			this.str = str;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			POJO1 other = (POJO1) obj;
			if (i != other.i)
				return false;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		}
	}

	private class POJO2
	{
		public int i;
		public String str;

		POJO2(int i, String str, List<POJO1> pojos) {
			this.i = i;
			this.str = str;
			this.pojos = pojos;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			POJO2 other = (POJO2) obj;
			if (i != other.i)
				return false;
			if (pojos == null) {
				if (other.pojos != null)
					return false;
			} else if (!pojos.equals(other.pojos))
				return false;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		}
		public List<POJO1> pojos;
	}

	private POJO1 pojo1_a;
	private POJO1 pojo1_b;

	private POJO2 pojo2_a;

	private class  Pair<F,S>
	{
		Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}
		F first;
		S second;
	}

	BlockingQueue<Pair<POJO1,String>> biConsumer1_bq;
	private BiConsumer<POJO1,String> biConsumer1 = (p,from) ->
	{
		biConsumer1_bq.add(new Pair<POJO1,String>(p,from));
	};

	BlockingQueue<POJO1> consumer1_bq;
	private Consumer<POJO1> consumer1 = p ->
	{
		consumer1_bq.add(p);
	};

	BlockingQueue<POJO2> consumer2_bq;
	private Consumer<POJO2> consumer2 = p ->
	{
		consumer2_bq.add(p);
	};


	@Before
	public void setUp() throws Exception {

		pojo1_a = new POJO1(1, "hi");
		pojo1_b = new POJO1(2, "bye");

		List<POJO1> pojos = new LinkedList<>();
		pojos.add(pojo1_a);
		pojos.add(pojo1_b);

		pojo2_a = new POJO2(-19,"oh yea!",pojos);


		server1 = new Server("server1_"+UUID.randomUUID().toString());
		server1.clearPersistentData();

		biConsumer1_bq = new LinkedBlockingQueue<>(); 
		consumer1_bq = new LinkedBlockingQueue<>();
		consumer2_bq = new LinkedBlockingQueue<>();
	}

	@After
	public void tearDown() throws Exception {
		server1.clearPersistentData();
	}

	@Test
	public void saveAndThenLoadInreger() {

		Integer x = 4;

		server1.saveObjectToFile("Integer", x);

		Optional<POJO1> $ = server1.readObjectFromFile("Integer", Integer.class);
		assertEquals($.get(), x);
	}

	
	@Test
	public void saveAndThenLoadSimpleObject() {

		POJO1 pojo1 = new POJO1(1, "hi");

		server1.saveObjectToFile("pojo1", pojo1);

		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertEquals($.get(), pojo1);
	}

	@Test
	public void saveAndThenLoadTwoSimpleObjects() {

		server1.saveObjectToFile("pojo1", pojo1_a);
		server1.saveObjectToFile("pojo2", pojo1_b);

		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertEquals($.get(), pojo1_a);

		$ = server1.readObjectFromFile("pojo2", POJO1.class);
		assertEquals($.get(), pojo1_b);
	}


	@Test
	public void saveAndThenLoadComplexObjects() {		


		server1.saveObjectToFile("c", pojo2_a);

		Optional<POJO2> $ = server1.readObjectFromFile("c", POJO2.class );
		//Optional<POJO2> $ = server1.readObjectFromFile("c", new POJO2().getClass() );

		assertEquals($.get(), pojo2_a);
	}

	@Test
	public void saveAndThenLoadFromNewServerWithSameName() {

		POJO1 pojo1 = new POJO1(1, "hi");

		server1.startListenLoop(biConsumer1, POJO1.class);
		server1.saveObjectToFile("pojo1", pojo1);
		server1.stop();


		Server s = new Server(server1.getAddress());
		s.startListenLoop(biConsumer1, POJO1.class);
		Optional<POJO1> $ = s.readObjectFromFile("pojo1", POJO1.class);
		s.stop();

		assertEquals($.get(), pojo1);
	}


	@Test
	public void saveAndThenLoadAfterClear() {

		server1.saveObjectToFile("pojo1", pojo1_a);

		server1.clearPersistentData();

		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertFalse($.isPresent());
	}


	@Test(timeout=5000)
	public void clientSendsToServerMessage() throws InterruptedException {

		for (int k=0; k<5; k++)
		{
			clients.get(0).startListenLoop(server1.getAddress(), consumer1, POJO1.class);
			server1.startListenLoop(biConsumer1, POJO1.class);

			for (int i=0; i<4; i++)
			{
				clients.get(0).send(pojo1_a);
				Pair<POJO1,String> $ = biConsumer1_bq.take();
				assertEquals($.first, pojo1_a);
				assertEquals($.second, clients.get(0).getAddress());
			}

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}

	@Test(timeout=10000)
	public void serverSendsToClientMessage() throws InterruptedException {

		for (int k=0; k<3; k++)
		{
			clients.get(0).startListenLoop(server1.getAddress(), consumer1, POJO1.class);
			server1.startListenLoop(biConsumer1, POJO1.class);

			for (int i=0; i<10; i++)
			{
				server1.send(clients.get(0).getAddress(), pojo1_a, false);
				POJO1 $ = consumer1_bq.take();
				assertEquals($, pojo1_a);
			}

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}

	@Test(timeout=20000)
	public void clientAndServerSendMessagesBackAndForth() throws InterruptedException {

		for (int i=0; i<3; i++)
		{
			tmp = 0; // messages count;
			final int messagesNumToSend = 7;

			clients.get(0).startListenLoop(server1.getAddress(), (POJO1 p) ->
			{
				tmp++;
				if (p.i > 0)
				{
					POJO1 p2 = new POJO1(p.i-1, "");
					clients.get(0).send(p2);
				}
			}, POJO1.class);


			server1.startListenLoop( (POJO1 p, String from) ->
			{
				tmp++;
				if (p.i > 0)
				{
					POJO1 p2 = new POJO1(p.i-1, "");
					server1.send(clients.get(0).getAddress(), p2, false);
				}

				biConsumer1_bq.add(new Pair<POJO1,String>(p,from));
			}, POJO1.class);


			clients.get(0).send(new POJO1(messagesNumToSend-1, "aaa"));
			Thread.sleep(200 * messagesNumToSend);

			assertEquals(tmp, messagesNumToSend);

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}


	@Test(timeout=8000)
	public void serverSendsResponseBackToClient() throws InterruptedException {

		for (int k=0; k<2; k++)
		{
			clients.get(0).startListenLoop(server1.getAddress(), consumer1, POJO1.class);
			server1.startListenLoop((pojo, str) ->
			{
				assertEquals(str, clients.get(0).getAddress());
				server1.send(clients.get(0).getAddress(), pojo1_b, true);
			}, POJO1.class);


			for (int i=0; i<6; i++)
			{			
				POJO1 $ = clients.get(0).sendAndBlockUntilResponseArrives(pojo1_a, POJO1.class);
				assertEquals($,pojo1_b);
			}			

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}


	@Test(timeout=5000)
	public void serverSendsComplexResponseBackToClient() throws InterruptedException {
		clients.get(0).startListenLoop(server1.getAddress(), consumer2, POJO2.class);
		server1.startListenLoop((pojo, str) ->
		{
			assertEquals(str, clients.get(0).getAddress());
			server1.send(clients.get(0).getAddress(), pojo2_a, true);
		}, POJO2.class);



		POJO2 $ = clients.get(0).sendAndBlockUntilResponseArrives(pojo2_a, POJO2.class);
		assertEquals($,pojo2_a);


		clients.get(0).stopListenLoop();
		server1.stop();
	}



	@Test (timeout=100000)
	public void serverRandomlyComunicatesWithTwoClients() throws InterruptedException {


		for (int k=0; k<3; k++)
		{
			for (int i=0; i<CLIENTS_NUM; i++)
			{
				clients.get(i).startListenLoop(server1.getAddress(), consumer1, POJO1.class);
			}


			server1.startListenLoop(   (POJO1 pojo, String str) ->
			{
				server1.send(str, pojo, pojo.i > 0);
			}, POJO1.class);


			int expectedCharsNum = 0;
			int expectedQueueSize = 0;
			for (int i=0; i<20; i++)
			{	
				int r = rnd.nextInt(2);


				if (r == 0)
				{
					if (rnd.nextInt(2) == 0)
					{
						String str = "aaaaaaaaaa".substring(0,rnd.nextInt(5)+1);
						POJO1 p1 = new POJO1(0,str);
						expectedCharsNum += str.length();
						clients.get(rnd.nextInt(CLIENTS_NUM)).send(p1);
						expectedQueueSize++;
					}


				} else
				{

					Thread t1 = new Thread( () -> {
						POJO1 p = new POJO1(1, "bbbbbbbbb".substring(0,rnd.nextInt(5)+1));
						POJO1 $ = clients.get(0).sendAndBlockUntilResponseArrives(
								p, POJO1.class);
						assertEquals($, p);
					});

					Thread t2 = new Thread( () -> {
						POJO1 p = new POJO1(1, "bbbbbbbbb".substring(0,rnd.nextInt(5)+1));
						POJO1 $ = clients.get(1).sendAndBlockUntilResponseArrives(
								p, POJO1.class);
						assertEquals($, p);
					});

					boolean use_t1 = (rnd.nextInt(2) == 0);
					boolean use_t2 = (rnd.nextInt(2) == 0);

					if (use_t1)
						t1.start(); 
					if (use_t2)
						t2.start(); 		

					if (use_t1)
						t1.join();
					if (use_t2)
						t2.join();		
				}
			}

			for (int i=0; i<expectedQueueSize; i++)
			{
				POJO1 p = consumer1_bq.take();
				expectedCharsNum -= p.str.length();
			}

			assertEquals(expectedCharsNum,0);
			assertTrue(consumer1_bq.isEmpty());


			for (int i=0; i<CLIENTS_NUM; i++)
			{
				clients.get(i).stopListenLoop();
			}
			server1.stop();
		}

	}

	
	@Test//TODO(timeout=120000)
	public void clientSendsToServerMessagesBothFromConsumerAndFromUserThread() throws InterruptedException {

		for (int k=0; k<2; k++)//TODO
		{
			
			final Integer messagesToSendFromClient_block = 3*2 + 1; // MUST BE ODD FOR THIS TEST!
			final Integer messagesToSendFromClient_nonblock = 6;
			final Integer messagesToSendFromClient_total = 
					messagesToSendFromClient_block + messagesToSendFromClient_nonblock;
			
			clients.get(0).startListenLoop(server1.getAddress(), (Integer i) ->
			{
				if (i > 0)
				{ 
					i--;
					tmp++;
					assert(i%2 == 0);

					Integer $ = clients.get(0).sendAndBlockUntilResponseArrives(
							i,Integer.class);
					assertEquals((Integer)$, i);
						
					if (i>0)
					{
						i--;
						tmp++;
						assert(i%2 == 1);
						clients.get(0).send(i);
					}

				}
			}, Integer.class);


			server1.startListenLoop((Integer i,String from) ->
			{
				if (i<0)
				{
					tmp++;
					return;
				}
				boolean isResponse = (i%2 == 0);
				server1.<Integer>send(clients.get(0).getAddress(), i, isResponse);
			}, Integer.class);

			
			for (int i=0; i<2; i++)//TODO
			{
				/* counter of [messages From Client send from consumer + messages server received from
				 * user's thread */ 
				tmp = 0;
				
				server1.send(clients.get(0).getAddress(), messagesToSendFromClient_block , false);
				for (int s=0; s<messagesToSendFromClient_nonblock; s++)
				{
					clients.get(0).send(new Integer(-1));
				}
				
				int total_transmitions = 
						(3 * messagesToSendFromClient_block  + 2 * messagesToSendFromClient_nonblock );
				Thread.sleep( total_transmitions * 200 );
				
				assertEquals(messagesToSendFromClient_total, new Integer(tmp));
			}

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}
}