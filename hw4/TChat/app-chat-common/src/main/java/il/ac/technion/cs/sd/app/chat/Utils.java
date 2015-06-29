package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.fail;


public class Utils {

	
	
	@FunctionalInterface
	public interface ThrowingRunnable
	{
		void run() throws Exception;
	}
	
	/**
	 * @param <T> The type of the exception that we expect consumer will throw when task is invoked.
	 */
	public static <T> void assertThrow(ThrowingRunnable task, Class<T> exceptionClass)
	{
		try{
			task.run();
		} catch (Exception e)
		{
			if (e.getClass().equals(exceptionClass))
				return;
		}
		fail();
	}
}
