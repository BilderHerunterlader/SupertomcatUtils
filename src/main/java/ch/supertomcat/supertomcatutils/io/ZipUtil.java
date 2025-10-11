package ch.supertomcat.supertomcatutils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zip Util
 */
public final class ZipUtil {
	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);

	/**
	 * Constructor
	 */
	private ZipUtil() {
	}

	/**
	 * Extract Zip File
	 * 
	 * @param zipFile Zip File
	 * @param targetDirectory Target Directory
	 * @throws IOException
	 */
	public static void extractZipFile(Path zipFile, Path targetDirectory) throws IOException {
		logger.info("Extract zip file '{}' to: {}", zipFile, targetDirectory);
		try (InputStream in = Files.newInputStream(zipFile); ZipInputStream zis = new ZipInputStream(in)) {
			byte[] buffer = new byte[8192];
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				String entryName = zipEntry.getName();
				Path outputPath = targetDirectory.resolve(entryName);
				logger.info("Zip Entry: Name: {}, Directory: {}, Size: {}", entryName, zipEntry.isDirectory(), zipEntry.getSize());

				if (zipEntry.isDirectory()) {
					logger.info("Create directory: {}", outputPath);
					Files.createDirectories(outputPath);
				} else {
					logger.info("Extract file '{}' to: {}", entryName, outputPath);
					try (OutputStream out = Files.newOutputStream(outputPath)) {
						int read;
						while ((read = zis.read(buffer)) != -1) {
							out.write(buffer, 0, read);
							out.flush();
						}
					}

					try {
						FileTime lastModifiedTime = zipEntry.getLastModifiedTime();
						FileTime creationTime = zipEntry.getCreationTime();

						BasicFileAttributeView attributes = Files.getFileAttributeView(outputPath, BasicFileAttributeView.class);
						attributes.setTimes(lastModifiedTime, null, creationTime);
					} catch (IOException e) {
						logger.error("Could not set file times for: {}", outputPath, e);
					}
				}
				zis.closeEntry();
			}
		}
	}
}
