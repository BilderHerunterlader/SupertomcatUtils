package ch.supertomcat.supertomcatutils.queue;

/**
 * Queue Task Factory
 * 
 * @param <T> Task Type
 * @param <R> Return Type
 */
public interface QueueTaskFactory<T, R> {
	/**
	 * Create Callable for Task
	 * 
	 * @param task Task
	 * @return Callable for Task
	 */
	public QueueTask<T, R> createTaskCallable(T task);
}
