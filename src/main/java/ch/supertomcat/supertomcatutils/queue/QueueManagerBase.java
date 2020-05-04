package ch.supertomcat.supertomcatutils.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the download-slots.
 * It contains also the counters and restrictions for domains.
 * So this class allows a Pic to download or let them wait
 * until a slot is free.
 * 
 * @param <T> Task Type
 * @param <R> Task Return Type
 */
public abstract class QueueManagerBase<T, R> {
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Synchronization Object
	 */
	protected final Object syncObject = new Object();

	/**
	 * Queue
	 */
	protected final List<T> queue = new ArrayList<>();

	/**
	 * Queue Task Factory
	 */
	protected final QueueTaskFactory<T, R> queueTaskFactory;

	/**
	 * Tasks which are currently executing
	 */
	protected final List<QueueTask<T, R>> executingTasks = new ArrayList<>();

	/**
	 * Flag for indicating that it should be checked if any tasks from the queue can be executed
	 */
	protected boolean checkScheduleTasks = true;

	/**
	 * Counters to handle max connections per host
	 */
	protected final Map<String, AtomicInteger> counters = new HashMap<>();

	/**
	 * Scheduler Thread
	 */
	protected Thread schedulerThread = null;

	/**
	 * Queue Completion Thread
	 */
	protected Thread queueCompletionThread = null;

	/**
	 * Maximum connection count
	 */
	protected int maxConnectionCount;

	/**
	 * Maximum connection count per host
	 */
	protected int maxConnectionCountPerHost;

	/**
	 * Files since application started
	 */
	protected int sessionFiles = 0;

	/**
	 * Bytes since application started
	 */
	protected long sessionBytes = 0;

	/**
	 * Open Slots
	 */
	protected int openSlots;

	/**
	 * Thread Pool
	 */
	protected ThreadPoolExecutor threadPool = null;

	/**
	 * completionService
	 */
	protected CompletionService<R> completionService = null;

	/**
	 * Stop Flag
	 */
	protected boolean stop = false;

	/**
	 * Constructor
	 * 
	 * @param queueTaskFactory Queue Task Factory
	 * @param maxConnectionCount Max Connection Count
	 * @param maxConnectionCountPerHost Max Connection Count per Host
	 */
	public QueueManagerBase(QueueTaskFactory<T, R> queueTaskFactory, int maxConnectionCount, int maxConnectionCountPerHost) {
		this.queueTaskFactory = queueTaskFactory;
		this.maxConnectionCount = maxConnectionCount;
		this.maxConnectionCountPerHost = maxConnectionCountPerHost;
		this.openSlots = maxConnectionCount;
	}

	/**
	 * Initialize
	 */
	public synchronized void init() {
		if (schedulerThread != null && schedulerThread.isAlive()) {
			return;
		}

		synchronized (syncObject) {
			stop = false;
			checkScheduleTasks = true;

			for (Map.Entry<String, AtomicInteger> entry : counters.entrySet()) {
				entry.getValue().set(0);
			}

			threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool(new QueueManagerBaseThreadFactory("BaseQueueThread-"));
			completionService = new ExecutorCompletionService<>(threadPool);
			applyMaxConnectionCount();
		}

		schedulerThread = new Thread(new QueueSchedulerThread());
		schedulerThread.setName("QueueSchedulerThread-" + schedulerThread.getId());
		schedulerThread.start();

		queueCompletionThread = new Thread(new QueueCompletionThread());
		queueCompletionThread.setName("QueueCompletionThread-" + queueCompletionThread.getId());
		queueCompletionThread.start();
	}

	/**
	 * Stop
	 */
	public synchronized void stop() {
		if (schedulerThread == null) {
			return;
		}
		synchronized (syncObject) {
			stop = true;

			cancelTasks(true);

			threadPool.shutdownNow();

			checkScheduleTasks = true;
			syncObject.notifyAll();
		}

		try {
			schedulerThread.join();
		} catch (InterruptedException e) {
			logger.error("Wait for scheduler thread to finish was interrupted", e);
		}

		queueCompletionThread.interrupt();

		try {
			queueCompletionThread.join();
		} catch (InterruptedException e) {
			logger.error("Wait for queue completion thread to finish was interrupted", e);
		}

		try {
			threadPool.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Wait for thread pool to finish was interrupted", e);
		}

		removeTaskCallables();

		schedulerThread = null;
		queueCompletionThread = null;
	}

	/**
	 * Stop tasks. Clears the queue and cancels are executing tasks
	 * 
	 * @param cancelAlreadyExecutingTasks Flag if already running tasks should be cancelled
	 */
	public synchronized void cancelTasks(boolean cancelAlreadyExecutingTasks) {
		synchronized (syncObject) {
			for (T task : queue) {
				removedTaskFromQueue(task, false);
			}
			queue.clear();

			if (cancelAlreadyExecutingTasks) {
				cancelTaskCallables(true);
			}

			checkScheduleTasks = true;
			syncObject.notifyAll();
		}
	}

	/**
	 * Returns the maxConnectionCount
	 * 
	 * @return maxConnectionCount
	 */
	public int getMaxConnectionCount() {
		return maxConnectionCount;
	}

	/**
	 * Returns the maxConnectionCountPerHost
	 * 
	 * @return maxConnectionCountPerHost
	 */
	public int getMaxConnectionCountPerHost() {
		return maxConnectionCountPerHost;
	}

	/**
	 * Sets the maxConnectionCount
	 * 
	 * @param maxConnectionCount maxConnectionCount
	 */
	public void setMaxConnectionCount(int maxConnectionCount) {
		synchronized (syncObject) {
			this.maxConnectionCount = maxConnectionCount;
			applyMaxConnectionCount();
			updateOpenSlots();
		}
	}

	/**
	 * Apply Max Connection Count
	 */
	protected void applyMaxConnectionCount() {
		synchronized (syncObject) {
			if (threadPool != null) {
				threadPool.setCorePoolSize(maxConnectionCount);
				threadPool.setMaximumPoolSize(maxConnectionCount);
			}
		}
	}

	/**
	 * Sets the maxConnectionCountPerHost
	 * 
	 * @param maxConnectionCountPerHost maxConnectionCountPerHost
	 */
	public void setMaxConnectionCountPerHost(int maxConnectionCountPerHost) {
		synchronized (syncObject) {
			this.maxConnectionCountPerHost = maxConnectionCountPerHost;
		}
	}

	/**
	 * Returns the sessionFiles
	 * 
	 * @return sessionFiles
	 */
	public int getSessionFiles() {
		return sessionFiles;
	}

	/**
	 * Increases the session files since application started by 1
	 */
	public synchronized void increaseSessionFiles() {
		this.sessionFiles++;
	}

	/**
	 * Returns the sessionBytes
	 * 
	 * @return sessionBytes
	 */
	public long getSessionBytes() {
		return sessionBytes;
	}

	/**
	 * Increases the bytes since application started
	 * 
	 * @param bytes Bytes
	 */
	public synchronized void increaseSessionBytes(long bytes) {
		this.sessionBytes += bytes;
	}

	/**
	 * @return Queue size
	 */
	public int getQueueSize() {
		return queue.size();
	}

	/**
	 * @return Count of currently executing tasks
	 */
	public int getTaskCount() {
		return executingTasks.size();
	}

	/**
	 * @return True if tasks are executing, false otherwise
	 */
	public boolean isExecutingTasks() {
		return !queue.isEmpty() && !executingTasks.isEmpty();
	}

	/**
	 * Update Open Slots
	 */
	protected void updateOpenSlots() {
		synchronized (syncObject) {
			int openSlotsTemp = maxConnectionCount - executingTasks.size();
			if (openSlotsTemp < 0) {
				/*
				 * Handle case when maxConnectionCount was lowered, but there are still more tasks executing
				 */
				openSlots = 0;
			}
			openSlots = openSlotsTemp;
		}
	}

	/**
	 * @return Open slots
	 */
	public int getOpenSlots() {
		return openSlots;
	}

	/**
	 * Add task to queue
	 * 
	 * @param task Task
	 */
	public void addTaskToQueue(T task) {
		synchronized (syncObject) {
			if (!queue.contains(task)) {
				queue.add(task);
				checkScheduleTasks = true;
				syncObject.notifyAll();
			}
		}
	}

	/**
	 * Add tasks to queue
	 * 
	 * @param tasks Tasks
	 */
	public void addTasksToQueue(List<T> tasks) {
		synchronized (syncObject) {
			for (T task : tasks) {
				if (!queue.contains(task)) {
					queue.add(task);
				}
			}
			checkScheduleTasks = true;
			syncObject.notifyAll();
		}
	}

	/**
	 * Called when a task was removed from the queue, when the queue was stopped or when a task could not be scheduled for execution
	 * 
	 * @param task Task
	 * @param executeFailure True if task could not be scheduled for execution, false otherwise
	 */
	protected abstract void removedTaskFromQueue(T task, boolean executeFailure);

	/**
	 * @param task Task
	 */
	protected void addTaskToExecutingTasks(QueueTask<T, R> task) {
		synchronized (syncObject) {
			executingTasks.add(task);
			updateOpenSlots();
		}
	}

	/**
	 * Cancel executing tasks
	 * 
	 * @param interruptTaskIfRunning True if running tasks should be interrupted, false otherwise
	 */
	protected void cancelTaskCallables(boolean interruptTaskIfRunning) {
		synchronized (syncObject) {
			for (QueueTask<T, R> task : executingTasks) {
				task.getFuture().cancel(interruptTaskIfRunning);
			}
		}
	}

	/**
	 * Remove Task Callables
	 */
	protected void removeTaskCallables() {
		synchronized (syncObject) {
			Iterator<QueueTask<T, R>> it = executingTasks.iterator();
			while (it.hasNext()) {
				QueueTask<T, R> task = it.next();
				if (task.getFuture() != null && task.getFuture().isDone()) {
					// Update Counter
					Restriction restriction = getRestrictionForTask(task.getTask());
					String restrictionKey = restriction.getRestrictionKey();
					AtomicInteger count = counters.get(restrictionKey);
					if (count != null) {
						// Decrement count, prevent negative value
						count.updateAndGet(value -> value > 0 ? value - 1 : value);
					}

					// Call callback
					completedTaskCallable(task);

					// Remove task
					it.remove();
				}
			}

			updateOpenSlots();

			checkScheduleTasks = true;
			syncObject.notifyAll();
		}
	}

	/**
	 * Remove Task Callables
	 * 
	 * @param future Future
	 */
	protected void removeTaskCallable(Future<R> future) {
		synchronized (syncObject) {
			QueueTask<T, R> taskForFuture = null;

			for (QueueTask<T, R> task : executingTasks) {
				if (future.equals(task.getFuture())) {
					taskForFuture = task;
					break;
				}
			}

			if (taskForFuture != null) {
				// Update Counter
				Restriction restriction = getRestrictionForTask(taskForFuture.getTask());
				String restrictionKey = restriction.getRestrictionKey();
				AtomicInteger count = counters.get(restrictionKey);
				if (count != null) {
					// Decrement count, prevent negative value
					count.updateAndGet(value -> value > 0 ? value - 1 : value);
				}

				// Call callback
				completedTaskCallable(taskForFuture);

				// Remove task
				executingTasks.remove(taskForFuture);
			} else {
				logger.error("Task not found for future: {}", future);
			}

			updateOpenSlots();

			checkScheduleTasks = true;
			syncObject.notifyAll();
		}
	}

	/**
	 * Called when a task completed, failed or was cancelled
	 * 
	 * @param task Task
	 */
	protected abstract void completedTaskCallable(QueueTask<T, R> task);

	/**
	 * Restriction for task
	 * 
	 * @param task Task
	 * @return Restriction for task
	 */
	protected abstract Restriction getRestrictionForTask(T task);

	/**
	 * @param restrictionKey Restriction Key
	 * @return Current Restricted Count
	 */
	protected int getRestrictedCount(String restrictionKey) {
		synchronized (syncObject) {
			AtomicInteger count = counters.get(restrictionKey);
			if (count != null) {
				return count.get();
			}
			return 0;
		}
	}

	/**
	 * Get maximum connection count for task
	 * 
	 * @param task Task
	 * @return Maximum connection count for task
	 */
	protected int getMaxConnectionCount(T task) {
		Restriction restriction = getRestrictionForTask(task);
		int max = restriction.getMaxConnectionCount();
		synchronized (syncObject) {
			if (max <= 0 || max > maxConnectionCountPerHost) {
				max = maxConnectionCountPerHost;
			}
		}
		return max;
	}

	/**
	 * Queue Scheduler Thread which executes tasks
	 */
	private class QueueSchedulerThread implements Runnable {

		@Override
		public void run() {
			while (!stop) {
				synchronized (syncObject) {
					while (!checkScheduleTasks || queue.isEmpty() || executingTasks.size() >= maxConnectionCount) {
						try {
							syncObject.wait();
						} catch (InterruptedException e) {
							logger.error("Wait for open slot was interrupted");
						}
						if (stop) {
							break;
						}
					}

					Iterator<T> itQueue = queue.iterator();
					while (itQueue.hasNext()) {
						if (executingTasks.size() >= maxConnectionCount) {
							break;
						}

						T task = itQueue.next();
						Restriction restriction = getRestrictionForTask(task);
						String restrictionKey = restriction.getRestrictionKey();
						AtomicInteger count = counters.get(restrictionKey);
						if (count == null) {
							count = new AtomicInteger();
							counters.put(restrictionKey, count);
						}

						int currentCountPerHost = count.get();
						int maxCountPerHost = getMaxConnectionCount(task);
						if (maxCountPerHost > 0 && currentCountPerHost >= maxCountPerHost) {
							// No more connections allowed for this host
							logger.info("xxxxxxxxxx Task is not executable: {}", task);
							continue;
						}

						// Download is allowed for this task, so remove it from queue and add it to executing tasks
						itQueue.remove();

						logger.info("xxxxxxxxxx Executing Task: {}", task);
						count.incrementAndGet();

						QueueTask<T, R> taskCallable = queueTaskFactory.createTaskCallable(task);

						try {
							Future<R> future = completionService.submit(taskCallable);
							taskCallable.setFuture(future);
							addTaskToExecutingTasks(taskCallable);
						} catch (Exception e) {
							logger.error("Could not schedule task: {}", task, e);
							removedTaskFromQueue(task, true);
						}
					}

					/*
					 * Set flag to false, because now all tasks, which are able to execute were executed and as long as no new tasks are added or tasks finished
					 * executing there is no need to check if tasks can be executed
					 */
					checkScheduleTasks = false;
				}
			}
		}
	}

	/**
	 * Queue Completion Thread which waits for completed tasks
	 */
	private class QueueCompletionThread implements Runnable {

		@Override
		public void run() {
			while (!stop) {
				try {
					Future<R> future = completionService.take();
					removeTaskCallable(future);
				} catch (InterruptedException e) {
					if (!stop) {
						logger.error("Wait for task to complete was interrupted");
					}
					continue;
				}
			}
		}
	}
}
