package ch.supertomcat.supertomcatutils.process;

import java.util.List;

/**
 * Evelated Process Executor
 */
public interface EvelatedProcessExecutor {
	/**
	 * Start Process
	 * 
	 * @param workingDirectory Working Directory or null
	 * @param commands Command and parameters
	 * @return True if successful, false otherwise
	 */
	public boolean startProcess(String workingDirectory, List<String> commands);

	/**
	 * Start Process
	 * 
	 * @param workingDirectory Working Directory or null
	 * @param commands Command and parameters
	 * @return True if successful, false otherwise
	 */
	public boolean startProcess(String workingDirectory, String... commands);
}
