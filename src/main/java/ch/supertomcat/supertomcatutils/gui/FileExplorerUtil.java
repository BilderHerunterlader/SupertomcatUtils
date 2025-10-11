package ch.supertomcat.supertomcatutils.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for opening directory in file explorer
 */
public final class FileExplorerUtil {
	/**
	 * Constructor
	 */
	private FileExplorerUtil() {
	}

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(FileExplorerUtil.class);

	/**
	 * Opens the given directory in the filemanager of the operating system
	 * 
	 * @param dir Directory
	 */
	public static void openDirectory(String dir) {
		openDirectory(Paths.get(dir));
	}

	/**
	 * Opens the given directory in the filemanager of the operating system
	 * 
	 * @param dir Directory
	 */
	public static void openDirectory(Path dir) {
		if (Desktop.isDesktopSupported()) {
			if (Files.exists(dir)) {
				try {
					Desktop.getDesktop().open(dir.toFile());
				} catch (IOException e) {
					logger.error("Could not open directory: {}", dir, e);
				}
			}
		} else {
			logger.error("Could not open directory, because Desktop is not supported: {}", dir);
		}
	}

	/**
	 * Open E-Mail
	 * 
	 * @param emailAddress E-Mail Address
	 */
	public static void openEMail(String emailAddress) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.MAIL)) {
				try {
					desktop.mail(new URI("mailto:" + emailAddress));
				} catch (URISyntaxException | IOException e) {
					logger.error("Could not open email: {}", emailAddress, e);
				}
			}
		} else {
			logger.error("Could not open email, because Desktop is not supported: {}", emailAddress);
		}
	}
}
