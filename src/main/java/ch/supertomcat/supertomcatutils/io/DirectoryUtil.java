package ch.supertomcat.supertomcatutils.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory Util
 */
public final class DirectoryUtil {
	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory.getLogger(DirectoryUtil.class);

	/**
	 * Constructor
	 */
	private DirectoryUtil() {
	}

	/**
	 * Copy directory recursively
	 * 
	 * @param sourceDirectory Source Directory
	 * @param targetDirectory Target Directory
	 * @throws IOException
	 */
	public static void copyDirectoryRecursive(final Path sourceDirectory, final Path targetDirectory) throws IOException {
		Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetSubFolder = targetDirectory.resolve(sourceDirectory.relativize(dir).toString());
				logger.info("Create Directories: {}", targetSubFolder);
				Files.createDirectories(targetSubFolder);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path targetFile = targetDirectory.resolve(sourceDirectory.relativize(file).toString());
				logger.info("Copy '{}' to '{}'", file, targetFile);
				Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Clear Directory recursive
	 * 
	 * @param directory Directory
	 * @throws IOException
	 */
	public static void clearDirectoryRecursive(final Path directory) throws IOException {
		deleteDirectoryRecursive(directory, false);
	}

	/**
	 * Delete Directory recursive
	 * 
	 * @param directory Directory
	 * @throws IOException
	 */
	public static void deleteDirectoryRecursive(final Path directory) throws IOException {
		deleteDirectoryRecursive(directory, true);
	}

	/**
	 * Clear Directory recursive
	 * 
	 * @param directory Directory
	 * @param deleteGivenDirectory True if the given directory should also be deleted, false otherwise
	 * @throws IOException
	 */
	public static void deleteDirectoryRecursive(final Path directory, boolean deleteGivenDirectory) throws IOException {
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				logger.info("Delete File: {}", file);
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (!deleteGivenDirectory && directory.equals(dir)) {
					return FileVisitResult.CONTINUE;
				}
				logger.info("Delete Directory: {}", dir);
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
