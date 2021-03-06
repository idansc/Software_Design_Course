package il.ac.technion.cs.sd.app.mail;

import java.util.ArrayList;

import il.ac.technion.cs.sd.lib.clientserver.Client;
import il.ac.technion.cs.sd.lib.clientserver.MessageData;
import il.ac.technion.cs.sd.msg.MessengerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


/*************************************************************
 * *************************************************************
 * *************************************************************
 * Note:
 * This unit test uses Mockito to test basic behavior of 
 * ClientMailApplicationTest, independently from the server.
 * The "TMailIntegrationTest" under app-mail-tests tests the logic of
 * the app extensively.
 * *************************************************************
 * *************************************************************
 *************************************************************/


public class ClientMailApplicationTest {

	ClientMailApplication user1;
	Client clientMock;
	
	
	@Before
	public void setUp() throws Exception {
		user1 = new ClientMailApplication("server1", "user1");
		clientMock = Mockito.mock(Client.class);
		Mockito.when(clientMock.sendToServerAndGetAnswer(
				Mockito.any(), Mockito.any())).thenReturn(new MessageData(""));
		user1.setClient(clientMock);
	}

	@After
	public void tearDown() throws Exception {
	}
	

	@Test
	public void verifySendMailMessage() throws MessengerException {
		
						
		user1.sendMail("Ofer", "hi");
		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("Ofer");
		strings.add("hi");
		MessageData data = new MessageData(
				TaskType.SEND_MAIL_TASK.toString(),	strings);

		
		Mockito.verify(clientMock,Mockito.only()).sendToServerAndGetAnswer("server1", data);
		
	}

	 
	@Test
	public void verifyCorrespondencesMessage() throws MessengerException {
	
						
		user1.getCorrespondences("Idan", 4);
		
		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("Idan");
		strings.add("4");
		MessageData data = new MessageData(
				TaskType.GET_CORRESPONDENCES_TASK.toString(),	strings);
		
		Mockito.verify(clientMock,Mockito.only()).sendToServerAndGetAnswer("server1", data);
		
	}
	
	
	
	@Test
	public void verifyMessageSentMail() throws MessengerException {
	
						
		user1.getSentMails(4);
		
		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("4");
		MessageData data = new MessageData(
				TaskType.GET_SENT_MAILS_TASK.toString(),strings);
		
		Mockito.verify(clientMock,Mockito.only()).sendToServerAndGetAnswer("server1", data);
		
	}
	
	
	
	@Test
	public void verifyIncomingMailMessage() throws MessengerException {
	
						
		user1.getIncomingMail(4);
		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("4");
		MessageData data = new MessageData(
				TaskType.GET_INCOMING_MAIL_TASK.toString(),strings);
		
		Mockito.verify(clientMock,Mockito.only()).sendToServerAndGetAnswer("server1", data);
	}
	
	
	@Test
	public void verifyAllMailMessage() throws MessengerException {
	
						
		user1.getAllMail(4);
		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("4");
		MessageData data = new MessageData(
				TaskType.GET_ALL_MAIL_TASK.toString(),strings);
		
		Mockito.verify(clientMock,Mockito.only()).sendToServerAndGetAnswer("server1", data);
	}
	
	@Test
	public void verifyNewMailMessage() throws MessengerException {
	
						
		user1.getNewMail();
		
		ArrayList<String> strings = new ArrayList<String>();
		MessageData data = new MessageData(
				TaskType.GET_NEW_MAIL_TASK.toString(),strings);
		
		Mockito.verify(clientMock,Mockito.only()).sendToServerAndGetAnswer("server1", data);
	}
	
	
	
	@Test
	public void verifyContactsMessage() throws MessengerException {
	
						
		user1.getContacts(4);
		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("4");
		MessageData data = new MessageData(
				TaskType.GET_CONTACTS_TASK.toString(),strings);
		
		Mockito.verify(clientMock,Mockito.only()).sendToServerAndGetAnswer("server1", data);
	}
	
	

}
