package ch.supertomcat.supertomcatutils.exceptionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UncaughtExceptionHandler which logs uncaught exceptions to SLF4J
 */
public class SLF4JUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught Exception in Thread with ID '{}': {}", t.threadId(), t.getName(), e);
	}
}
