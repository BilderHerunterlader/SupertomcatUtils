package ch.supertomcat.supertomcatutils.application;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.exceptionhandler.SLF4JUncaughtExceptionHandler;

/**
 * Utility class to initialize an application
 */
public final class ApplicationUtil {
	/**
	 * Pattern for Bin Path
	 */
	private static final Pattern BIN_PATH_PATTERN = Pattern.compile("(?i)(.+?)[/\\\\]bin$");

	/**
	 * Log File Date Format
	 */
	public static final DateTimeFormatter LOG_FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * Backup File Date Format
	 */
	public static final DateTimeFormatter BACKUP_FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss-SSS");

	/**
	 * Basic Error Log Date Format
	 */
	private static final DateTimeFormatter BASIC_ERROR_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * LockFile Channel
	 */
	private static FileChannel lockFileChannel;

	/**
	 * LockFile
	 */
	private static FileLock lockFile = null;

	/**
	 * Constructor
	 */
	private ApplicationUtil() {
	}

	/**
	 * Returns the path of this application
	 * 
	 * @param jarFilename Filename of the Jar
	 * @return Path
	 */
	public static String getThisApplicationsPath(String jarFilename) {
		// Get the classpath
		String classpath = System.getProperty("java.class.path");
		if (classpath.contains(";")) {
			// if the classpath contains more then one path, we split the string
			String[] paths = classpath.split(";");

			// We check if there is a path, that ends on "/bin" or "\bin"
			for (String path : paths) {
				Matcher m = BIN_PATH_PATTERN.matcher(path);
				if (m.matches()) {
					// If the pattern matched, we get the path without the "\bin" or "/bin"
					Path f = Paths.get(m.replaceAll("$1"));
					if (Files.exists(f)) {
						String rootPath = f + System.getProperty("file.separator");
						// Logging is not intialized at this time, so we write the warning to sysout
						System.out.println("Rootpath of application detected: " + rootPath);
						return rootPath;
					}
				}
			}
		} else {
			// if the classpath does not contain more than one path, we search for the name of the jarFile
			Pattern p = Pattern.compile("(?i)(.+?)[/\\\\]" + jarFilename + "$");
			Matcher m = p.matcher(classpath);
			if (classpath.equalsIgnoreCase(jarFilename)) {
				return "./";
			} else if (m.matches()) {
				Path f = Paths.get(m.replaceAll("$1"));
				if (Files.exists(f)) {
					String rootPath = f + System.getProperty("file.separator");
					// Logging is not intialized at this time, so we write the warning to sysout
					System.out.println("Rootpath of application detected: " + rootPath);
					return rootPath;

				}
			}
		}
		// If we can not exactly detect what the path is, we return just ./
		return "./";
	}

	/**
	 * Returns the filename of this application's Jar File or an empty string, if the filename could not be detected
	 * 
	 * @param classInJarFile Class which is in Jar File
	 * @return Jar Filename or an empty String
	 */
	public static String getThisApplicationsJarFilename(Class<?> classInJarFile) {
		try {
			Path jarFile = Paths.get(classInJarFile.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (Files.exists(jarFile) && Files.isRegularFile(jarFile)) {
				// Logging is not intialized at this time, so we write the warning to sysout
				System.out.println("Jar-Filename detected: " + jarFile.getFileName());
				return jarFile.getFileName().toString();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// Logging is not intialized at this time, so we write the warning to sysout
		System.err.println("Could not detect Jar-Filename");
		return "";
	}

	/**
	 * Compares two Version-Strings
	 * 
	 * @param v1 First Version-String
	 * @param v2 Second Version-String
	 * @return 0 or less than 0 or more than 0
	 */
	public static int compareVersions(String v1, String v2) {
		String[] strV1 = v1.split("\\.");
		String[] strV2 = v2.split("\\.");
		int diff = strV1.length - strV2.length;
		int len = strV1.length;
		if (diff < 0) {
			len = strV2.length;
		}
		int[] strV1C = new int[len];
		int[] strV2C = new int[len];
		try {
			for (int a = 0; a < len; a++) {
				if (strV1.length <= a) {
					strV1C[a] = 0;
				} else {
					strV1C[a] = Integer.parseInt(strV1[a]);
				}
			}
			for (int a = 0; a < len; a++) {
				if (strV2.length <= a) {
					strV2C[a] = 0;
				} else {
					strV2C[a] = Integer.parseInt(strV2[a]);
				}
			}
		} catch (NumberFormatException nfe) {
			return 0;
		}

		for (int i = 0; i < len; i++) {
			int comp = Integer.compare(strV1C[i], strV2C[i]);
			if (comp != 0) {
				return comp;
			}
		}
		return 0;
	}

	/**
	 * Try to lock the lockfile
	 * 
	 * @param strLockFilePath Lockfile Path
	 * @param strLockFilename Lockfile Filename
	 * @return True if successful
	 */
	public static synchronized boolean lockLockFile(String strLockFilePath, String strLockFilename) {
		Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);

		// If directory does not exist, create it
		Path folder = Paths.get(strLockFilePath);
		try {
			Files.createDirectories(folder);
		} catch (IOException e) {
			logger.error("Could not create directory: {}", folder);
			return false;
		}

		// Create lock file
		Path file = Paths.get(strLockFilePath, strLockFilename);
		if (!Files.exists(file)) {
			try {
				// If file does not exist, create it
				Files.createFile(file);
			} catch (IOException e) {
				logger.error("Could not create Lock-File: {}", file, e);
				return false;
			}
		}

		try {
			FileChannel channel = FileChannel.open(file, StandardOpenOption.APPEND);
			lockFileChannel = channel;
			// Lock the file
			lockFile = lockFileChannel.tryLock();
			if (lockFile == null) {
				logger.error("Could not lock Lock-File: {}", file);
				channel.close();
				lockFileChannel = null;
				return false;
			}
			return true;
		} catch (IOException e) {
			logger.error("Could not lock Lock-File: {}", file, e);
			closeLockFileChannel();
			return false;
		}
	}

	/**
	 * Release lock file
	 * 
	 * @return True if successful, false otherwise
	 */
	public static synchronized boolean releaseLockFile() {
		try {
			if (lockFile == null) {
				return true;
			}

			try {
				lockFile.release();
				lockFile = null;
				return true;
			} catch (IOException e) {
				Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);
				logger.error("Could not release Lock-File", e);
				return false;
			}
		} finally {
			closeLockFileChannel();
		}
	}

	private static synchronized void closeLockFileChannel() {
		if (lockFileChannel != null) {
			try {
				lockFileChannel.close();
				lockFileChannel = null;
			} catch (IOException e) {
				Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);
				logger.error("Could not close Lock-File channel", e);
			}
		}
	}

	/**
	 * @return FileLock or null
	 */
	public static synchronized FileLock getLockFile() {
		return lockFile;
	}

	/**
	 * @param days Days
	 * @param logFileName Logfile Name
	 * @param logPath Logfile Path
	 */
	public static void deleteOldLogFiles(int days, String logFileName, String logPath) {
		Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);

		Path logDir = Paths.get(logPath);
		if (!Files.exists(logDir) || !Files.isDirectory(logDir)) {
			return;
		}

		final Pattern patternLogFiles = Pattern.compile(logFileName + "\\.(\\d{4}-\\d{2}-\\d{2})");

		LocalDate thresholdDate = LocalDate.now().minusDays(days);

		try (Stream<Path> stream = Files.list(logDir)) {
			stream.filter(x -> patternLogFiles.matcher(x.getFileName().toString()).matches()).forEach(x -> deleteOldLogFileIfNecessary(thresholdDate, patternLogFiles, x));
		} catch (IOException e) {
			logger.error("Could not delete old logfiles in folder: {}", logDir);
		}
	}

	private static void deleteOldLogFileIfNecessary(LocalDate thresholdDate, Pattern patternLogFiles, Path file) {
		try {
			Matcher matcher = patternLogFiles.matcher(file.getFileName().toString());
			String strDateOfFile = matcher.replaceAll("$1");
			LocalDate dateOfFile = LocalDate.parse(strDateOfFile, LOG_FILE_DATE_FORMAT);
			if (thresholdDate.isAfter(dateOfFile)) {
				Files.delete(file);
			}
		} catch (DateTimeParseException | IOException e) {
			Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);
			logger.error("Could not check or delete Log-File: {}", file, e);
		}
	}

	/**
	 * Sets the default UncaughtExceptionHandler to a handler, which logs exceptions to SLF4J
	 */
	public static void initializeSLF4JUncaughtExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new SLF4JUncaughtExceptionHandler());
	}

	/**
	 * Logs application info
	 */
	public static void logApplicationInfo() {
		Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);
		logger.info("{} {}, Java-Version: {}, Java-Vendor: {}, OS: {} {} {}, Processors: {}, VM Max Memory: {}", ApplicationProperties
				.getProperty(ApplicationMain.APPLICATION_NAME), ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION), System.getProperty("java.version"), System
						.getProperty("java.vendor"), System
								.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().maxMemory());
	}

	/**
	 * Writes an error logfile for the case when no Loggers are initialized. If an Exception occurs while writing
	 * the file, the Stacktrace is printed to the console.
	 * 
	 * @param file File
	 * @param errorMessage Error Message
	 */
	public static void writeBasicErrorLogfile(Path file, String errorMessage) {
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {
			String dateTime = LocalDateTime.now().format(BASIC_ERROR_LOG_DATE_FORMAT);
			writer.write(dateTime + ": " + errorMessage);
			writer.flush();
		} catch (IOException e) {
			System.err.println("Could not write error log file: " + file);
			e.printStackTrace();
		}
	}

	/**
	 * Deletes old backup files
	 * 
	 * @param folder Folder
	 * @param filename Filename
	 * @param daysToKeepBackup Days to keep backups
	 */
	public static void deleteOldBackupFiles(Path folder, final String filename, int daysToKeepBackup) {
		Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);

		if (!Files.exists(folder) || !Files.isDirectory(folder)) {
			return;
		}

		final Pattern backupPattern = Pattern.compile("^" + filename + "-(\\d{4}-\\d{2}-\\d{2}--\\d{2}-\\d{2}-\\d{2}-\\d{3})$");

		LocalDateTime thresholdDateTime = LocalDateTime.now().minusDays(daysToKeepBackup);

		try (Stream<Path> stream = Files.list(folder)) {
			stream.filter(x -> backupPattern.matcher(x.getFileName().toString()).matches()).forEach(x -> deleteOldBackupFileIfNecessary(thresholdDateTime, backupPattern, x));
		} catch (IOException e) {
			logger.error("Could not delete old backup files in folder: {}", folder);
		}
	}

	private static void deleteOldBackupFileIfNecessary(LocalDateTime thresholdDateTime, Pattern backupPattern, Path file) {
		try {
			Matcher matcher = backupPattern.matcher(file.getFileName().toString());
			String strDateOfFile = matcher.replaceAll("$1");
			LocalDateTime dateTimeOfFile = LocalDateTime.parse(strDateOfFile, BACKUP_FILE_DATE_FORMAT);
			if (thresholdDateTime.isAfter(dateTimeOfFile)) {
				Files.delete(file);
			}
		} catch (DateTimeParseException | IOException e) {
			Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);
			logger.error("Could not check or delete Backup-File: {}", file, e);
		}
	}

	/**
	 * Create invisible frame, which can be used as parent for JOptionPane dialogs, so that the message is displayed in the taskbar
	 * 
	 * @param title Window Title
	 * @return Invisible Frame
	 */
	public static JFrame createInvisibleFrame(String title) {
		return createInvisibleFrame(title, null);
	}

	/**
	 * Create invisible frame, which can be used as parent for JOptionPane dialogs, so that the message is displayed in the taskbar
	 * 
	 * @param title Window Title
	 * @param icon Icon
	 * @return Invisible Frame
	 */
	public static JFrame createInvisibleFrame(String title, Image icon) {
		JFrame frame = new JFrame(title);
		frame.setIconImage(icon);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		return frame;
	}

	/**
	 * Format Stacktrace to String
	 * 
	 * @param throwable Throwable
	 * @return Stacktrace as String
	 */
	public static String formatStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}
