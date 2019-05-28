package ch.supertomcat.suportomcatutils.queue;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Queue Task Interface
 * 
 * @param <T> Task Type
 * @param <R> Return Type
 */
public interface QueueTask<T, R> extends Callable<R> {
	/**
	 * @return Task
	 */
	public T getTask();

	/**
	 * @return Future or null if task not yet scheduled
	 */
	public Future<R> getFuture();

	/**
	 * Sets the future
	 * 
	 * @param future Future
	 */
	public void setFuture(Future<R> future);
}
