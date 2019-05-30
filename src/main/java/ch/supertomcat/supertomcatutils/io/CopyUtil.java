package ch.supertomcat.supertomcatutils.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which provides methods to copy a file
 */
public final class CopyUtil {
	/**
	 * Buffer Size
	 */
	private static final int BUFFER_SIZE = 8192;

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(CopyUtil.class);

	/**
	 * Constructor
	 */
	private CopyUtil() {
	}

	/**
	 * Copy a file
	 * 
	 * @param source Source
	 * @param target Target
	 * @return True if successful
	 */
	public static boolean copy(String source, String target) {
		try (FileInputStream fis = new FileInputStream(source); FileOutputStream fos = new FileOutputStream(target)) {
			copy(fis, fos);
			return true;
		} catch (IOException e) {
			logger.error("File '{}' could not be copied to '{}'", source, target, e);
			return false;
		}
	}

	/**
	 * Copies InputStream to OutputStream
	 * 
	 * @param in InputStream
	 * @param out OutputStream
	 * @throws IOException
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
			out.flush();
		}
	}
}
