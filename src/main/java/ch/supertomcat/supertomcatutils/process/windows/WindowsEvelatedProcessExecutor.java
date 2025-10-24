package ch.supertomcat.supertomcatutils.process.windows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShellAPI.SHELLEXECUTEINFO;
import com.sun.jna.platform.win32.WinUser;

import ch.supertomcat.supertomcatutils.process.EvelatedProcessExecutor;

/**
 * Evelated Process Executor for Windows
 */
public class WindowsEvelatedProcessExecutor implements EvelatedProcessExecutor {
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
		SHELLEXECUTEINFO execInfo = new SHELLEXECUTEINFO();
		execInfo.lpVerb = "runas";
		execInfo.lpFile = commands.get(0);
		if (commands.size() > 1) {
			execInfo.lpParameters = commands.stream().skip(1).collect(Collectors.joining(" "));
		}
		if (workingDirectory != null) {
			execInfo.lpDirectory = workingDirectory;
		}
		execInfo.nShow = WinUser.SW_SHOW;

		boolean result = Shell32.INSTANCE.ShellExecuteEx(execInfo);
		if (!result) {
			int error = Kernel32.INSTANCE.GetLastError();
			logger.error("Starting process failed with exit code: {}", error);
		}
		return result;
	}
}
