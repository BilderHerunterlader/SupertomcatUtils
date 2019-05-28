package ch.supertomcat.supertomcatutils.queue;

import java.util.concurrent.Future;

/**
 * Queue Task Interface
 * 
 * @param <T> Task Type
 * @param <R> Return Type
 */
public abstract class QueueTaskBase<T, R> implements QueueTask<T, R> {
	/**
	 * Task
	 */
	protected final T task;

	/**
	 * Future
	 */
	protected Future<R> future;

	/**
	 * Constructor
	 * 
	 * @param task Task
	 */
	public QueueTaskBase(T task) {
		this.task = task;
	}

	@Override
	public T getTask() {
		return task;
	}

	@Override
	public Future<R> getFuture() {
		return future;
	}

	@Override
	public void setFuture(Future<R> future) {
		this.future = future;
	}
}
