package ch.supertomcat.supertomcatutils.application;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.exceptionhandler.SLF4JUncaughtExceptionHandler;

/**
 * Utility class to initialize an application
 */
public final class ApplicationUtil {
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
			Pattern p = Pattern.compile("(?i)(.+?)[/\\\\]bin$");
			for (String path : paths) {
				Matcher m = p.matcher(path);
				if (m.matches()) {
					// If the pattern matched, we get the path without the "\bin" or "/bin"
					File f = new File(m.replaceAll("$1"));
					if (f.exists()) {
						String rootPath = f.getPath() + System.getProperty("file.separator");
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
				File f = new File(m.replaceAll("$1"));
				if (f.exists()) {
					String rootPath = f.getPath() + System.getProperty("file.separator");
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
			File jarFile = new File(classInJarFile.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			if (jarFile.isFile()) {
				// Logging is not intialized at this time, so we write the warning to sysout
				System.out.println("Jar-Filename detected: " + jarFile.getName());
				return jarFile.getName();
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
		File folder = new File(strLockFilePath);
		if (!folder.exists() && !folder.mkdirs()) {
			logger.error("Could not create directory: {}", folder.getAbsolutePath());
			return false;
		}

		// Create lock file
		File file = new File(strLockFilePath, strLockFilename);
		if (!file.exists()) {
			try {
				// If file does not exist, create it
				if (!file.createNewFile()) {
					logger.error("Could not create Lock-File: {}", file.getAbsolutePath());
					return false;
				}
			} catch (IOException e) {
				logger.error("Could not create Lock-File: {}", file.getAbsolutePath(), e);
				return false;
			}
		}

		try {
			// Lock the file
			lockFile = new RandomAccessFile(file, "rw").getChannel().tryLock();
			return lockFile != null;
		} catch (IOException e) {
			logger.error("Could not lock Lock-File: {}", file.getAbsolutePath(), e);
			return false;
		}
	}

	/**
	 * Release lock file
	 * 
	 * @return True if successful, false otherwise
	 */
	public static synchronized boolean releaseLockFile() {
		if (lockFile != null) {
			try {
				lockFile.release();
				lockFile = null;
				return true;
			} catch (IOException e) {
				Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);
				logger.error("Could not release Lock-File", e);
				return false;
			}
		}
		return true;
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

		final Pattern patternLogFiles = Pattern.compile(logFileName + "\\.([0-9]{4}-[0-9]{2}-[0-9]{2})");
		File logDir = new File(logPath);
		if (logDir.exists() && logDir.isDirectory()) {
			File logFiles[] = logDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return patternLogFiles.matcher(name).matches();
				}
			});
			if (logFiles == null) {
				logger.error("Could not get file list of directory: {}", logDir.getAbsolutePath());
				return;
			}

			final long millisecondsPerDay = 24 * 60 * 60 * 1000;
			Date beforeXDays = new Date((new Date().getTime()) - (millisecondsPerDay * days));

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			for (File logFile : logFiles) {
				try {
					Matcher matcher = patternLogFiles.matcher(logFile.getName());
					String strDateOfFile = matcher.replaceAll("$1");
					Date dateOfFile = df.parse(strDateOfFile);

					if (beforeXDays.after(dateOfFile) && !logFile.delete()) {
						logger.error("Could not delete old Log-File: {}", logFile.getAbsolutePath());
					}
				} catch (NumberFormatException | ParseException e) {
					logger.error("Could not check Log-File: {}", logFile.getAbsolutePath(), e);
				}
			}
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
		logger.info("{} {}, Java-Version: {}, Java-Vendor: {}, OS: {} {} {}, Processors: {}, VM Max Memory: {}", ApplicationProperties.getProperty("ApplicationName"), ApplicationProperties
				.getProperty("ApplicationVersion"), System.getProperty("java.version"), System.getProperty("java.vendor"), System
						.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().maxMemory());
	}

	/**
	 * Writes an error logfile for the case when no Loggers are initialized. If an Exception occurs while writing
	 * the file, the Stacktrace is printed to the console.
	 * 
	 * @param file File
	 * @param errorMessage Error Message
	 */
	public static void writeBasicErrorLogfile(File file, String errorMessage) {
		try (FileOutputStream out = new FileOutputStream(file, true); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateTime = dateFormat.format(new Date());
			bw.write(dateTime + ": " + errorMessage);
			bw.flush();
		} catch (IOException e) {
			System.err.println("Could not write error log file: " + file.getAbsolutePath());
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
	public static void deleteOldBackupFiles(File folder, final String filename, int daysToKeepBackup) {
		Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);

		final long now = System.currentTimeMillis();
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -daysToKeepBackup);
		final Date backupDeleteDate = cal.getTime();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS");
		final long backupDeleteTime = daysToKeepBackup * 24 * 60 * 60 * 1000;

		// Delete old backup-Files
		File backupFiles[] = folder.listFiles(new FileFilter() {
			private final Pattern oldBackupPattern = Pattern.compile("^" + filename + ".bak-([0-9]+)$");
			private final Pattern backupPattern = Pattern.compile("^" + filename + "-([0-9]{4}-[0-9]{2}-[0-9]{2}--[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3})$");

			@Override
			public boolean accept(File pathname) {
				Matcher matcher = backupPattern.matcher(pathname.getName());
				if (matcher.matches()) {
					try {
						Date backupDate = dateFormat.parse(matcher.group(1));
						if (backupDate.before(backupDeleteDate)) {
							return true;
						}
					} catch (ParseException e) {
						logger.error("Could not parse datetime of backup file: {}", pathname.getAbsolutePath(), e);
					}
					return false;
				}

				matcher = oldBackupPattern.matcher(pathname.getName());
				if (matcher.matches()) {
					try {
						long backupTime = Long.parseLong(matcher.group(1));
						if ((now - backupTime) > backupDeleteTime) {
							return true;
						}
					} catch (NumberFormatException nfe) {
						logger.error("Could not parse datetime of backup file: {}", pathname.getAbsolutePath(), nfe);
					}
					return false;
				}
				return false;
			}
		});
		if (backupFiles != null) {
			for (File oldBackupFile : backupFiles) {
				if (!oldBackupFile.delete()) {
					logger.error("Could not delete old backup file: {}", oldBackupFile.getAbsolutePath());
				}
			}
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
