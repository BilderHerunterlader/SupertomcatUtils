package ch.supertomcat.supertomcatutils.process.linux;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.process.EvelatedProcessExecutor;

/**
 * Evelated Process Executor for Linux
 */
public class LinuxEvelatedProcessExecutor implements EvelatedProcessExecutor {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean startProcess(String workingDirectory, String... commands) {
		return startProcess(workingDirectory, Arrays.asList(commands));
	}

	@Override
	public boolean startProcess(String workingDirectory, List<String> commands) {
		if (commands.isEmpty()) {
			throw new IllegalArgumentException("No command provided");
		}

		List<String> combinedCommands = new ArrayList<>();
		combinedCommands.add("pkexec");
		combinedCommands.addAll(commands);
		ProcessBuilder processBuilder = new ProcessBuilder(combinedCommands).inheritIO();
		if (workingDirectory != null) {
			processBuilder = processBuilder.directory(new File(workingDirectory));
		}
		try {
			Process process = processBuilder.start();
			int exitCode = process.waitFor();
			return exitCode == 0;
		} catch (IOException | InterruptedException e) {
			logger.error("Starting process failed with exit code", e);
			return false;
		}
	}
}
