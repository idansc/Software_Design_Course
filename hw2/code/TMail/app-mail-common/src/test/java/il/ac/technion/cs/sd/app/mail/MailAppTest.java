/**
 * 
 */
package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author idansc
 *
 */
public class MailAppTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		List<String> usernames = Arrays.asList("Idan","Ofer","Gal");
		List<Mail> mailList = new ArrayList<Mail>();
		mailList.add(new Mail("Idan","Gal","Assignment 2 delay"));
		mailList.add(new Mail("Gal","Idan","Assignment 2 delayed"));
		mailList.add(new Mail("Ofer","Idan","We have more time"));
		mailList.add(new Mail("Ofer","Idan","I completed the lib"));
		mailList.add(new Mail("Gal","OFer","WOW such Assignment solution"));
	} 

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	

}
