package ch.supertomcat.supertomcatutils.application;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class for starting application
 */
public abstract class ApplicationMain {
	/**
	 * Application Short Name
	 */
	protected final String applicationShortName;

	/**
	 * Application Icon
	 */
	protected final Image applicationIcon;

	/**
	 * GUI Flag
	 */
	protected final boolean gui;

	/**
	 * Single Instance Flag
	 */
	protected final boolean singleInstance;

	/**
	 * Main Class
	 */
	protected final Class<?> mainClass;

	/**
	 * Additional Paths
	 */
	protected final Set<String> additionalPaths = new LinkedHashSet<>();

	/**
	 * Paths which can be overridden by directories.properties
	 */
	protected final Set<String> overwritablePaths = new LinkedHashSet<>();

	/**
	 * Constructor
	 * 
	 * @param applicationShortName Application Short Name (Only used for displaying or logging errors during startup)
	 * @param applicationIcon Application Icon (Only used for displaying errors during startup) or null
	 * @param gui True if application is a GUI application, false otherwise
	 * @param singleInstance True if there should be only a single instance of the program running, false otherwise
	 * @param mainClass Main Class (Used for loading resources and finding jar file)
	 */
	public ApplicationMain(String applicationShortName, Image applicationIcon, boolean gui, boolean singleInstance, Class<?> mainClass) {
		this(applicationShortName, applicationIcon, gui, singleInstance, mainClass, Collections.emptyList(), Collections.emptyList());
	}

	/**
	 * Constructor
	 * 
	 * @param applicationShortName Application Short Name (Only used for displaying or logging errors during startup)
	 * @param applicationIcon Application Icon (Only used for displaying errors during startup) or null
	 * @param gui True if application is a GUI application, false otherwise
	 * @param singleInstance True if there should be only a single instance of the program running, false otherwise
	 * @param mainClass Main Class (Used for loading resources and finding jar file)
	 * @param additionalPaths Additional Paths
	 */
	public ApplicationMain(String applicationShortName, Image applicationIcon, boolean gui, boolean singleInstance, Class<?> mainClass, List<String> additionalPaths) {
		this(applicationShortName, applicationIcon, gui, singleInstance, mainClass, additionalPaths, Collections.emptyList());
	}

	/**
	 * Constructor
	 * 
	 * @param applicationShortName Application Short Name (Only used for displaying or logging errors during startup)
	 * @param applicationIcon Application Icon (Only used for displaying errors during startup) or null
	 * @param gui True if application is a GUI application, false otherwise
	 * @param singleInstance True if there should be only a single instance of the program running, false otherwise
	 * @param mainClass Main Class (Used for loading resources and finding jar file)
	 * @param additionalPaths Additional Paths
	 * @param overwritablePaths Paths which can be overridden by directories.properties
	 */
	public ApplicationMain(String applicationShortName, Image applicationIcon, boolean gui, boolean singleInstance, Class<?> mainClass, List<String> additionalPaths, List<String> overwritablePaths) {
		this.applicationShortName = applicationShortName;
		this.applicationIcon = applicationIcon;
		this.gui = gui;
		this.singleInstance = singleInstance;
		this.mainClass = mainClass;
		this.additionalPaths.addAll(additionalPaths);
		if (overwritablePaths.isEmpty()) {
			this.overwritablePaths.add("LogsPath");
			this.overwritablePaths.addAll(additionalPaths);
		} else {
			this.overwritablePaths.addAll(overwritablePaths);
		}
	}

	/**
	 * Start Application
	 * 
	 * @param args Arguments
	 */
	public void start(String[] args) {
		initializeApplicationProperties();
		initializeJarFilenameAndApplicationPathProperties();

		String programUserDir = System.getProperty("user.home") + FileUtil.FILE_SEPERATOR + "." + ApplicationProperties.getProperty("ApplicationShortName") + FileUtil.FILE_SEPERATOR;
		initializeProfileAndLogsPathProperties(programUserDir);
		initializeAdditionalPathProperties(programUserDir);

		parseDefaultCommandLine(args);

		overrideDirectoryProperties();

		initializeLogging();

		if (singleInstance) {
			ensureSingleInstance(programUserDir, ApplicationProperties.getProperty("ApplicationShortName") + ".lock");
		}

		// Write some useful info to the logfile
		ApplicationUtil.logApplicationInfo();

		// Delete old log files
		deleteOldLogFiles();

		// Call program specific startup
		main(args);
	}

	/**
	 * Exit Application with exit code 0
	 */
	public void exit() {
		exit(0, false);
	}

	/**
	 * Exit Application with exit code 0
	 * 
	 * @param restart Restart Application
	 */
	public void exit(boolean restart) {
		exit(0, restart);
	}

	/**
	 * Exit Application
	 * 
	 * @param exitCode Exit Code
	 */
	public void exit(int exitCode) {
		exit(exitCode, false);
	}

	/**
	 * Exit Application
	 * 
	 * @param exitCode Exit Code
	 * @param restart Restart Application
	 */
	public void exit(int exitCode, boolean restart) {
		if (singleInstance) {
			releaseSingleInstanceLockFile();
		}

		if (restart) {
			restartApplication();
		}

		exitNow(exitCode);
	}

	/**
	 * Restart Application
	 * 
	 * @return True if application could be restarted, false otherwise
	 */
	protected boolean restartApplication() {
		if (!ApplicationProperties.getProperty("JarFilename").isEmpty()) {
			try {
				String applicationAbsolutePath = new File(ApplicationProperties.getProperty("ApplicationPath")).getAbsolutePath();
				if (!applicationAbsolutePath.endsWith(FileUtil.FILE_SEPERATOR)) {
					applicationAbsolutePath += FileUtil.FILE_SEPERATOR;
				}

				String jre = "";
				String jreJavaw = System.getProperty("java.home") + FileUtil.FILE_SEPERATOR + "bin" + FileUtil.FILE_SEPERATOR + "javaw";
				String jreJava = System.getProperty("java.home") + FileUtil.FILE_SEPERATOR + "bin" + FileUtil.FILE_SEPERATOR + "java";

				String os = System.getProperty("os.name").toLowerCase();

				File fJreJavaw = new File(os.contains("windows") ? jreJavaw + ".exe" : jreJavaw);
				File fJreJava = new File(os.contains("windows") ? jreJava + ".exe" : jreJava);

				if (fJreJavaw.exists()) {
					jre = "\"" + jreJavaw + "\" -jar \"" + applicationAbsolutePath + ApplicationProperties.getProperty("JarFilename") + "\"";
				} else {
					if (fJreJava.exists()) {
						jre = "\"" + jreJava + "\" -jar \"" + applicationAbsolutePath + ApplicationProperties.getProperty("JarFilename") + "\"";
					}
				}

				if (jre.isEmpty()) {
					return false;
				}

				List<String> lProcess = new ArrayList<>(Arrays.asList(jre.split(" ")));
				new ProcessBuilder(lProcess).start();

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				return true;
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Could not restart application", e);
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Actually Exit Application now
	 * 
	 * @param exitCode Exit Code
	 */
	protected void exitNow(int exitCode) {
		// Exit
		LoggerFactory.getLogger(getClass()).debug("Exit now!");
		System.exit(exitCode);
	}

	/**
	 * Initialize Application Properties from Application_Config.properties
	 */
	protected void initializeApplicationProperties() {
		try {
			ApplicationProperties.initProperties(mainClass.getResourceAsStream("/Application_Config.properties"));
		} catch (IOException e) {
			logStartupError("Could not initialize application properties", e);
			System.exit(1);
		}
	}

	/**
	 * Initialize JarFilename and ApplicationPath properties
	 */
	protected void initializeJarFilenameAndApplicationPathProperties() {
		String jarFilename = ApplicationUtil.getThisApplicationsJarFilename(mainClass);
		ApplicationProperties.setProperty("JarFilename", jarFilename);

		// Geth the program directory
		String appPath = ApplicationUtil.getThisApplicationsPath(!jarFilename.isEmpty() ? jarFilename : ApplicationProperties.getProperty("ApplicationShortName") + ".jar");
		ApplicationProperties.setProperty("ApplicationPath", appPath);
	}

	/**
	 * Initialize ProfilePath and LogsPath properties
	 * 
	 * @param programUserDir Program User Directory
	 */
	protected void initializeProfileAndLogsPathProperties(String programUserDir) {
		ApplicationProperties.setProperty("ProfilePath", programUserDir);
		ApplicationProperties.setProperty("LogsPath", programUserDir);
	}

	/**
	 * Initialize additional path properties
	 * 
	 * @param programUserDir Program User Directory
	 */
	protected void initializeAdditionalPathProperties(String programUserDir) {
		for (String additionalPath : additionalPaths) {
			ApplicationProperties.setProperty(additionalPath, programUserDir);
		}
	}

	/**
	 * Read the directories.txt from program folder if exists and
	 * override paths when definded in the file
	 */
	protected void overrideDirectoryProperties() {
		try {
			Properties directoriesProperties = readDirectoriesFile();
			if (directoriesProperties == null) {
				return;
			}

			for (String overwriteablePath : overwritablePaths) {
				String dir = directoriesProperties.getProperty(overwriteablePath);
				if (dir != null && !dir.isEmpty()) {
					ApplicationProperties.setProperty(overwriteablePath, dir);
				}
			}
		} catch (IOException e) {
			logStartupError("Could not read directories.properties", e);
			System.exit(1);
		}
	}

	/**
	 * The user can override the path of some directories.
	 * There must only be a textfile called directories.txt in
	 * the programm folder.
	 * 
	 * A line in the file must look like this
	 * Name Path
	 * Name and Path must be seperated by a tab.
	 * 
	 * @return Properties or null if no file exists
	 * @throws IOException
	 */
	protected Properties readDirectoriesFile() throws IOException {
		File file = new File(ApplicationProperties.getProperty("ApplicationPath") + "directories.properties");
		if (!file.exists()) {
			return null;
		}
		String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

		Properties directoriesProperties = new Properties();
		// We do the replace, because load will treat backslashes as escape characters
		directoriesProperties.load(new StringReader(content.replace("\\", "\\\\")));
		return directoriesProperties;
	}

	/**
	 * Initialize Logging
	 */
	protected void initializeLogging() {
		String logFilename = ApplicationProperties.getProperty("ApplicationShortName") + ".log";
		// Loggers can be created after this point
		System.setProperty("applicationlog4jlogfile", ApplicationProperties.getProperty("LogsPath") + FileUtil.FILE_SEPERATOR + logFilename);
		// Get logger to make sure logging is actually initiliazed
		LoggerFactory.getLogger(mainClass);
		ApplicationUtil.initializeSLF4JUncaughtExceptionHandler();
	}

	/**
	 * Parse default command line
	 * 
	 * @param args Arguments
	 */
	protected void parseDefaultCommandLine(String[] args) {
		for (String arg : args) {
			if (arg.equalsIgnoreCase("-version")) {
				System.out.print(ApplicationProperties.getProperty("ApplicationVersion"));
				System.exit(0);
			} else if (arg.equalsIgnoreCase("-versionNumber")) {
				System.out.print(ApplicationProperties.getProperty("ApplicationVersion").replaceAll("\\.", ""));
				System.exit(0);
			} else if (arg.equalsIgnoreCase("-help")) {
				String help = ApplicationProperties.getProperty("ApplicationName") + " v" + ApplicationProperties.getProperty("ApplicationVersion") + "\n\n";
				help += "Command Line Arguments:\n";
				help += "-version\t\tPrints the Version of the program (e.g. 1.2.0)\n\n";
				help += "-versionNumber\t\tPrints the VersionNumber of the program (e.g. 120)\n\n";
				System.out.print(help);
				System.exit(0);
			}
		}
	}

	/**
	 * Ensure single instance by locking lock file
	 * 
	 * @param lockFileDirectory Lock File Directory
	 * @param lockFilename Lock Filename
	 */
	protected void ensureSingleInstance(String lockFileDirectory, String lockFilename) {
		/*
		 * Now try to lock the file
		 * We do this, to make sure, there is only one instance of this program runnig.
		 */
		if (!ApplicationUtil.lockLockFile(lockFileDirectory, lockFilename)) {
			if (gui) {
				displayStartupError("Another Instance of the Application is running. Application is terminating.");
			} else {
				LoggerFactory.getLogger(mainClass).error("Another Instance of the Application is running. Application is terminating.");
			}
			System.exit(0);
		}
	}

	/**
	 * Release Single Instance Lock File
	 */
	public void releaseSingleInstanceLockFile() {
		// Release the lockfile
		LoggerFactory.getLogger(getClass()).debug("Releasing Lockfile");
		ApplicationUtil.releaseLockFile();
	}

	/**
	 * Log Startup Error to Basic Log File
	 * 
	 * @param message Message
	 * @param t Throwable
	 */
	protected void logStartupError(String message, Throwable t) {
		// Logger is not initialized at this point
		System.err.println(message);
		t.printStackTrace();
		ApplicationUtil.writeBasicErrorLogfile(new File(applicationShortName + "-Error.log"), message + ":\n" + ApplicationUtil.formatStackTrace(t));
	}

	/**
	 * Display Startup Error
	 * 
	 * @param message Message
	 */
	protected void displayStartupError(String message) {
		/*
		 * Display a frame, so that the program already shows up in the taskbar and can be switched to. Otherwise the user might not see that there was a dialog
		 * open
		 */
		JFrame frame = null;
		try {
			frame = ApplicationUtil.createInvisibleFrame(applicationShortName, applicationIcon);
			JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (frame != null) {
				frame.dispose();
			}
		}
	}

	/**
	 * Delete old log files
	 */
	protected void deleteOldLogFiles() {
		String logFilename = ApplicationProperties.getProperty("ApplicationShortName") + ".log";
		ApplicationUtil.deleteOldLogFiles(7, logFilename, ApplicationProperties.getProperty("LogsPath"));
	}

	/**
	 * Start Application implemented by sub class
	 * 
	 * @param args Arguments
	 */
	protected abstract void main(String[] args);
}
