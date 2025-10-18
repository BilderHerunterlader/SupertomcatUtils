package ch.supertomcat.supertomcatutils.application;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class for starting application
 */
public abstract class ApplicationMain {
	/**
	 * Application Name Property Name
	 */
	public static final String APPLICATION_NAME = "ApplicationName";

	/**
	 * Application Short Name Property Name
	 */
	public static final String APPLICATION_SHORT_NAME = "ApplicationShortName";

	/**
	 * Application Version Property Name
	 */
	public static final String APPLICATION_VERSION = "ApplicationVersion";

	/**
	 * Application Path Property Name
	 */
	public static final String APPLICATION_PATH = "ApplicationPath";

	/**
	 * Program Data Path Property Name
	 */
	public static final String PROGRAM_DATA_PATH = "ProgramDataPath";

	/**
	 * Jar Filename Property Name
	 */
	public static final String JAR_FILENAME = "JarFilename";

	/**
	 * Profile Path Property Name
	 */
	public static final String PROFILE_PATH = "ProfilePath";

	/**
	 * Logs Path Property Name
	 */
	public static final String LOGS_PATH = "LogsPath";

	/**
	 * License Name Property Name
	 */
	public static final String LICENSE_NAME = "LicenseName";

	/**
	 * License Text Property Name
	 */
	public static final String LICENSE_TEXT = "LicenseText";

	/**
	 * Database Path Property Name
	 */
	public static final String DATABASE_PATH = "DatabasePath";

	/**
	 * Settings Path Property Name
	 */
	public static final String SETTINGS_PATH = "SettingsPath";

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
	 * Shutdown Hook Threads
	 */
	protected final List<Thread> shutdownHookThreads = new CopyOnWriteArrayList<>();

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
			this.overwritablePaths.add(LOGS_PATH);
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

		String jarFilename = getJarFilename();
		String applicationPath = getApplicationPath();

		initializeJarFilenameAndApplicationPathProperties(jarFilename, applicationPath);

		String programUserDir = System.getProperty("user.home") + FileUtil.FILE_SEPERATOR + "." + ApplicationProperties.getProperty(APPLICATION_SHORT_NAME) + FileUtil.FILE_SEPERATOR;
		initializeProfileAndLogsPathProperties(programUserDir);

		initializeProgramDataPathProperties(applicationPath, programUserDir);

		initializeAdditionalPathProperties(programUserDir);

		parseDefaultCommandLine(args);

		overrideDirectoryProperties();

		initializeLogging();

		if (singleInstance) {
			ensureSingleInstance(programUserDir, ApplicationProperties.getProperty(APPLICATION_SHORT_NAME) + ".lock");
		}

		// Write some useful info to the logfile
		ApplicationUtil.logApplicationInfo();

		// Delete old log files
		deleteOldLogFiles();

		// Set System LookAndFeel
		if (gui) {
			setSystemLookAndFeel();
		}

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
		String jarFilename = ApplicationProperties.getProperty(JAR_FILENAME);
		if (jarFilename == null || jarFilename.isEmpty()) {
			LoggerFactory.getLogger(getClass()).error("Could not restart application: JarFilename Application Property is null or empty: {}", jarFilename);
			return false;
		}

		try {
			String applicationAbsolutePath = Paths.get(ApplicationProperties.getProperty(APPLICATION_PATH)).toAbsolutePath().toString();
			if (!applicationAbsolutePath.endsWith(FileUtil.FILE_SEPERATOR)) {
				applicationAbsolutePath += FileUtil.FILE_SEPERATOR;
			}

			String javaExePath = getJavaExePath();
			if (javaExePath == null) {
				LoggerFactory.getLogger(getClass()).error("Could not restart application: Could not find java executable");
				return false;
			}

			List<String> arguments = new ArrayList<>();
			arguments.add(javaExePath);
			arguments.add("-jar");
			arguments.add(applicationAbsolutePath + ApplicationProperties.getProperty(JAR_FILENAME));

			new ProcessBuilder(arguments).start();

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// Do not log exception
			}
			return true;
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Could not restart application", e);
			return false;
		}
	}

	/**
	 * @return Java Executable Path or null
	 */
	protected String getJavaExePath() {
		String jreBinPath = System.getProperty("java.home") + FileUtil.FILE_SEPERATOR + "bin" + FileUtil.FILE_SEPERATOR;

		String jreJavaw = jreBinPath + "javaw";
		String jreJava = jreBinPath + "java";

		Path fJreJavaw;
		Path fJreJava;
		if (Platform.isWindows()) {
			fJreJavaw = Paths.get(jreJavaw + ".exe");
			fJreJava = Paths.get(jreJava + ".exe");
		} else {
			fJreJavaw = Paths.get(jreJavaw);
			fJreJava = Paths.get(jreJava);
		}

		if (Files.exists(fJreJavaw)) {
			return jreJavaw;
		}

		if (Files.exists(fJreJava)) {
			return jreJava;
		}

		return null;
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
		try (InputStream in = mainClass.getResourceAsStream("/Application_Config.properties")) {
			ApplicationProperties.initProperties(in);
		} catch (IOException e) {
			logStartupError("Could not initialize application properties", e);
			System.exit(1);
		}
	}

	/**
	 * Get Jar Filename
	 * 
	 * @return Jar Filename
	 */
	protected String getJarFilename() {
		return ApplicationUtil.getThisApplicationsJarFilename(mainClass);
	}

	/**
	 * Application Path
	 * 
	 * @return Application Path
	 */
	protected String getApplicationPath() {
		String jarFilename = ApplicationUtil.getThisApplicationsJarFilename(mainClass);
		return ApplicationUtil.getThisApplicationsPath(!jarFilename.isEmpty() ? jarFilename : ApplicationProperties.getProperty(APPLICATION_SHORT_NAME) + ".jar");
	}

	/**
	 * Initialize JarFilename and ApplicationPath properties
	 * 
	 * @param jarFilename Jar Filename
	 * @param applicationPath Application Path
	 */
	protected void initializeJarFilenameAndApplicationPathProperties(String jarFilename, String applicationPath) {
		ApplicationProperties.setProperty(JAR_FILENAME, jarFilename);
		ApplicationProperties.setProperty(APPLICATION_PATH, applicationPath);
	}

	/**
	 * Initialize ProfilePath and LogsPath properties
	 * 
	 * @param programUserDir Program User Directory
	 */
	protected void initializeProfileAndLogsPathProperties(String programUserDir) {
		ApplicationProperties.setProperty(PROFILE_PATH, programUserDir);
		ApplicationProperties.setProperty(LOGS_PATH, programUserDir);
	}

	/**
	 * Initialize ProfilePath and LogsPath properties
	 * 
	 * @param applicationPath Application Path
	 * @param programUserDir Program User Directory
	 */
	protected void initializeProgramDataPathProperties(String applicationPath, String programUserDir) {
		String programDataPath;
		if (Platform.isWindows()) {
			String programDataEnvPath = System.getenv("ProgramData");
			if (programDataEnvPath != null) {
				programDataPath = programDataEnvPath + FileUtil.FILE_SEPERATOR + ApplicationProperties.getProperty(APPLICATION_NAME) + FileUtil.FILE_SEPERATOR;
			} else {
				programDataPath = applicationPath;
			}
		} else {
			programDataPath = applicationPath;
		}

		ApplicationProperties.setProperty(PROGRAM_DATA_PATH, programDataPath);
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
		Path file = Paths.get(ApplicationProperties.getProperty(APPLICATION_PATH) + "directories.properties");
		if (!Files.exists(file)) {
			return null;
		}
		String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);

		Properties directoriesProperties = new Properties();
		// We do the replace, because load will treat backslashes as escape characters
		directoriesProperties.load(new StringReader(content.replace("\\", "\\\\")));
		return directoriesProperties;
	}

	/**
	 * Initialize Logging
	 */
	protected void initializeLogging() {
		String logFilename = ApplicationProperties.getProperty(APPLICATION_SHORT_NAME) + ".log";
		// Loggers can be created after this point
		System.setProperty("applicationlog4jlogfile", ApplicationProperties.getProperty(LOGS_PATH) + FileUtil.FILE_SEPERATOR + logFilename);
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
				System.out.print(ApplicationProperties.getProperty(APPLICATION_VERSION));
				System.exit(0);
			} else if (arg.equalsIgnoreCase("-versionNumber")) {
				System.out.print(ApplicationProperties.getProperty(APPLICATION_VERSION).replace(".", ""));
				System.exit(0);
			} else if (arg.equalsIgnoreCase("-help")) {
				String help = ApplicationProperties.getProperty(APPLICATION_NAME) + " v" + ApplicationProperties.getProperty(APPLICATION_VERSION) + "\n\n";
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
		ApplicationUtil.writeBasicErrorLogfile(Paths.get(applicationShortName + "-Error.log"), message + ":\n" + ApplicationUtil.formatStackTrace(t));
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
		String logFilename = ApplicationProperties.getProperty(APPLICATION_SHORT_NAME) + ".log";
		ApplicationUtil.deleteOldLogFiles(7, logFilename, ApplicationProperties.getProperty(LOGS_PATH));
	}

	/**
	 * Change Look and Feel to System LookAndFeel
	 * 
	 * @return True if successfull, false otherwise
	 */
	protected boolean setSystemLookAndFeel() {
		return changeLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

	/**
	 * Change Look and Feel
	 * 
	 * @param lookAndFeelClassName LookAndFeel Class Name
	 * @return True if successfull, false otherwise
	 */
	protected boolean changeLookAndFeel(String lookAndFeelClassName) {
		try {
			UIManager.setLookAndFeel(lookAndFeelClassName);
			return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			LoggerFactory.getLogger(getClass()).error("Could not set look and feel: {}", lookAndFeelClassName, e);
			return false;
		}
	}

	/**
	 * Add Default Shutdown Hook, which will call the {@link #shutdownHookExit()}
	 * method.
	 */
	protected void addDefaultShutdownHook() {
		// Create and register the Shutdown-Thread
		Thread shutdownThread = new Thread(new Runnable() {
			@Override
			public void run() {
				shutdownHookExit();
			}
		});
		shutdownThread.setName("Shutdown-Thread-" + shutdownThread.threadId());
		addShutdownHook(shutdownThread);
	}

	/**
	 * Method which is called when default shutdown hook was added with {@link #addDefaultShutdownHook}. By default this method calls the {@link #exit()}
	 * method.
	 */
	protected void shutdownHookExit() {
		exit();
	}

	/**
	 * Add Shutdown Hook
	 * 
	 * @param shutdownHookThread Shutdown Hook Thread
	 */
	protected void addShutdownHook(Thread shutdownHookThread) {
		Runtime.getRuntime().addShutdownHook(shutdownHookThread);
		if (!shutdownHookThreads.contains(shutdownHookThread)) {
			shutdownHookThreads.add(shutdownHookThread);
		}
	}

	/**
	 * Remove Shutdown Hook
	 * 
	 * @param shutdownHookThread Shutdown Hook Thread
	 */
	protected void removeShutdownHook(Thread shutdownHookThread) {
		Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
		shutdownHookThreads.remove(shutdownHookThread);
	}

	/**
	 * Remove All Shutdown Hooks (which were previously added with {@link #addShutdownHook(Thread)}
	 */
	protected void removeAllShutdownHooks() {
		for (Thread shutdownHookThread : shutdownHookThreads) {
			Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
		}
		shutdownHookThreads.clear();
	}

	/**
	 * Start Application implemented by sub class
	 * 
	 * @param args Arguments
	 */
	protected abstract void main(String[] args);
}
