package ch.supertomcat.supertomcatutils.queue;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * ThreadFactory for QueueManagerBase
 */
public class QueueManagerBaseThreadFactory implements ThreadFactory {
	/**
	 * Base Thread Factory
	 */
	private final ThreadFactory baseThreadFactory = Executors.defaultThreadFactory();

	/**
	 * Thread Name Prefix
	 */
	private final String threadNamePrefix;

	/**
	 * Constructor
	 * 
	 * @param threadNamePrefix Thread Name Prefix
	 */
	public QueueManagerBaseThreadFactory(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = baseThreadFactory.newThread(r);
		t.setName(threadNamePrefix + t.threadId());
		return t;
	}
}
