package ch.supertomcat.supertomcatutils.clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class for the clipboard
 */
public final class ClipboardUtil {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ClipboardUtil.class);

	/**
	 * The toolkit to work with the clipboard
	 */
	private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	/**
	 * Constructor
	 */
	private ClipboardUtil() {
	}

	/**
	 * Set content to clipboard
	 * 
	 * @param content Content
	 */
	public static void setClipboardContent(String content) {
		try {
			StringSelection data = new StringSelection(content);
			clipboard.setContents(data, data);
		} catch (Exception e) {
			logger.error("Could not set clipboard content", e);
		}
	}
}
