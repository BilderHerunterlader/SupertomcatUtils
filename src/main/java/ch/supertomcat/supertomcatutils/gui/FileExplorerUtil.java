package ch.supertomcat.supertomcatutils.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

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
	public static void openDirectoryInFilemanager(String dir) {
		if (Desktop.isDesktopSupported()) {
			File fDir = new File(dir);
			if (fDir.exists()) {
				try {
					Desktop.getDesktop().open(fDir);
				} catch (IOException e) {
					logger.error("Could not open directory: {}", fDir.getAbsolutePath(), e);
				}
			}
		}
	}
}
