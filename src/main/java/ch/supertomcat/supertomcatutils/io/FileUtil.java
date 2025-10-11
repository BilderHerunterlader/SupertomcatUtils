package ch.supertomcat.supertomcatutils.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which provides methods to get directory, filename, ... from a path to a file
 */
public final class FileUtil {
	/**
	 * Path-Seperator for the actual operating system
	 */
	public static final String FILE_SEPERATOR = System.getProperty("file.separator");

	/**
	 * Path-Seperator from Windows
	 */
	public static final String FILE_SEPERATOR_WINDOWS = "\\";

	/**
	 * Path-Seperator from Windows
	 */
	public static final char FILE_SEPERATOR_CHAR_WINDOWS = '\\';

	/**
	 * Path-Seperator from Linux
	 */
	public static final String FILE_SEPERATOR_LINUX = "/";

	/**
	 * Path-Seperator from Linux
	 */
	public static final char FILE_SEPERATOR_CHAR_LINUX = '/';

	/**
	 * Windows Max Path Length (Minus 1, because of Null Terminator)
	 */
	public static final int WINDOWS_MAX_PATH_LENGTH = 260 - 1;

	/**
	 * Windows Min Filename Length (8 for name, 1 for dot, 3 for extension)
	 */
	public static final int WINDOWS_MIN_FILENAME_LENGTH = 8 + 1 + 3;

	/**
	 * Windows Max Path Length for a folder only.
	 */
	public static final int WINDOWS_MAX_FOLDER_PATH_LENGTH = WINDOWS_MAX_PATH_LENGTH - WINDOWS_MIN_FILENAME_LENGTH;

	/**
	 * Maximum length of a path without filename
	 */
	public static final int PATH_WITHOUT_FILENAME_LENGTH_LIMIT = 139;

	/**
	 * Maximum length of a filename
	 */
	public static final int FILENAME_LENGTH_LIMIT = 108;

	/**
	 * patternWindowsDriveLetter
	 */
	private static final Pattern PATTERN_WINDOWS_DRIVE_LETTER = Pattern.compile("^[a-zA-Z]:\\\\.*");

	/**
	 * Pattern for relative path component
	 */
	private static final Pattern RELATIVE_PATH_COMPONENT_REMOVAL_PATTERN = Pattern.compile("\\.[\\\\/]");

	/**
	 * Filter all non-ascii chars from paths and filenames
	 */
	public static final int FILENAME_ASCII_ONLY = 0;

	/**
	 * Filter all non-ascii and non-umlaut chars from paths and filenames
	 */
	public static final int FILENAME_ASCII_UMLAUT = 1;

	/**
	 * Filter only not allowed chars from paths and filenames
	 */
	public static final int FILENAME_ALL = 2;

	/**
	 * Pattern to trim spaces and points from the start of a string
	 */
	private static Pattern lTrimPatternPath = Pattern.compile("^[\\s\\.]+");

	/**
	 * Pattern to trim spaces and points from the end of a string
	 */
	private static Pattern rTrimPatternPath = Pattern.compile("[\\s\\.]+$");

	/**
	 * Pattern for searching number patterns in filenames
	 */
	private static Pattern patternZeroFilledNumber = Pattern.compile("#+");

	private static FilenameFilter filenameAsciiOnlyFilter = new FilenameAsciiOnlyFilter();

	private static FilenameFilter filenameAsciiUmlautFilter = new FilenameAsciiUmlautFilter();

	private static FilenameFilter filenameAllFilter = new FilenameFilter();

	/**
	 * Constructor
	 */
	private FileUtil() {
	}

	/**
	 * Returns the position of the last seperator
	 * 
	 * @param file Path to the file
	 * @return Position of the last seperator
	 */
	private static int getLastSeperatorPos(String file) {
		if (file == null || file.isEmpty()) {
			return -1;
		}

		int posBackslash = file.lastIndexOf(FILE_SEPERATOR_CHAR_WINDOWS);
		int posSlash = file.lastIndexOf(FILE_SEPERATOR_CHAR_LINUX);

		if (posBackslash < 0 && posSlash < 0) {
			return -1;
		}

		return (posBackslash > posSlash) ? posBackslash : posSlash;
	}

	/**
	 * Returns the folder
	 * 
	 * @param file Path to the file
	 * @return Pfad zum Verzeichnis
	 */
	public static String getDirectory(String file) {
		if (file == null) {
			return "";
		}
		int pos = getLastSeperatorPos(file);
		if (pos > -1) {
			return file.substring(0, pos + 1);
		}
		return "";
	}

	/**
	 * Returns the filename
	 * 
	 * @param file Path to the file
	 * @return Filename
	 */
	public static String getFilename(String file) {
		if (file == null) {
			return file;
		}
		int pos = getLastSeperatorPos(file);
		if (pos > -1) {
			return file.substring(pos + 1);
		}
		return file;
	}

	/**
	 * Returns position of the last dot
	 * 
	 * @param file Path to the file
	 * @return Position of the last dot
	 */
	private static int getLastDotPos(String file) {
		if (file == null || file.isEmpty()) {
			return -1;
		}
		return file.lastIndexOf(".");
	}

	/**
	 * Returns position of the file extension dot
	 * 
	 * @param file Path to the file extension dot
	 * @return Position of the file extension dot or -1
	 */
	private static int getFileExtensionDotPos(String file) {
		int posDot = getLastDotPos(file);
		if (posDot > -1) {
			int posPathSeparator = getLastSeperatorPos(file);
			if (posPathSeparator < 0 || posDot > posPathSeparator) {
				return posDot;
			}
		}
		return -1;
	}

	/**
	 * Returns the extension of a file
	 * 
	 * @param file Path to the file or filename
	 * @return Fileextension
	 */
	public static String getFileExtension(String file) {
		int pos = getFileExtensionDotPos(file);
		if (pos > -1) {
			return file.substring(pos + 1);
		}
		return "";
	}

	/**
	 * Returns the filename without the extension
	 * 
	 * @param file Path to the file or filename
	 * @return Filename without extension
	 */
	public static String getFilePrefix(String file) {
		String filename = getFilename(file);
		if (filename == null) {
			return file;
		}
		int pos = getLastDotPos(filename);
		if (pos > 0) {
			return filename.substring(0, pos);
		}
		return filename;
	}

	/**
	 * Returns the path of a file
	 * 
	 * @param file File
	 * @return Path
	 */
	public static String getPathFromFile(Path file) {
		if (Files.isDirectory(file)) {
			return file.toAbsolutePath().toString();
		} else {
			return getDirectory(file.toAbsolutePath().toString());
		}
	}

	/**
	 * Checks if path is a subfolder of parentPath
	 * It will also return true, if the path is not a direct subfolder.
	 * And if path and parentPath are the same, this method will also
	 * return true!
	 * This method does not check if the folders exist!
	 * 
	 * Example:
	 * parentPath = C:\
	 * path = C:\example
	 * -- this returns true
	 * 
	 * Example:
	 * parentPath = C:\
	 * path = C:\example\something\bla\blub
	 * -- this returns true
	 * 
	 * @param path Path
	 * @param parentPath Parent Path
	 * @return TRUE if it is the same folder or a subfolder
	 */
	public static boolean checkIsSameOrSubFolder(String path, String parentPath) {
		Path folderPath = Paths.get(path);
		Path folderParentPath = Paths.get(parentPath);
		String absolutPath = folderPath.toAbsolutePath().toString();
		String absolutParentPath = folderParentPath.toAbsolutePath().toString();

		if (PATTERN_WINDOWS_DRIVE_LETTER.matcher(absolutPath).matches()) {
			absolutPath = absolutPath.toLowerCase();
		}
		if (PATTERN_WINDOWS_DRIVE_LETTER.matcher(absolutParentPath).matches()) {
			absolutParentPath = absolutParentPath.toLowerCase();
		}

		absolutPath = RELATIVE_PATH_COMPONENT_REMOVAL_PATTERN.matcher(absolutPath).replaceAll("");
		absolutParentPath = RELATIVE_PATH_COMPONENT_REMOVAL_PATTERN.matcher(absolutParentPath).replaceAll("");

		if (!absolutPath.endsWith(FILE_SEPERATOR)) {
			absolutPath += FILE_SEPERATOR;
		}

		if (!absolutParentPath.endsWith(FILE_SEPERATOR)) {
			absolutParentPath += FILE_SEPERATOR;
		}

		return absolutPath.startsWith(absolutParentPath);
	}

	/**
	 * Trims the start of a path
	 * 
	 * @param str Path
	 * @return Path
	 */
	public static synchronized String lTrim(String str) {
		return lTrimPatternPath.matcher(str).replaceAll("");
	}

	/**
	 * Trims the end of a path
	 * 
	 * @param str Path
	 * @return Path
	 */
	public static synchronized String rTrim(String str) {
		return rTrimPatternPath.matcher(str).replaceAll("");
	}

	/**
	 * Trims a path
	 * 
	 * @param path Path
	 * @return Path
	 */
	public static String pathRTrim(String path) {
		if (path.isEmpty()) {
			return path;
		}

		char lastCharacter = path.charAt(path.length() - 1);
		boolean endsWithPathSeparator = lastCharacter == FILE_SEPERATOR_CHAR_WINDOWS || lastCharacter == FILE_SEPERATOR_CHAR_LINUX;

		if (endsWithPathSeparator) {
			String pathWihtoutSeparator = path.substring(0, path.length() - 1);
			return rTrim(pathWihtoutSeparator) + lastCharacter;
		} else {
			return rTrim(path);
		}
	}

	/**
	 * Returns a filename with "#" replaced by the given number
	 * If there are more # as the numbers length is than the # is replaced by zero.
	 * Example:
	 * The filename is "file###name" and the number is 2, then this method will
	 * return this: "file002name"
	 * 
	 * @param filename Filename
	 * @param number Number
	 * @return Filename
	 */
	public static String getNumberedFilename(String filename, int number) {
		if (!filename.contains("#")) {
			return filename;
		}

		StringBuffer sb = new StringBuffer();
		Matcher matcher = patternZeroFilledNumber.matcher(filename);
		while (matcher.find()) {
			String replacement = getZeroFilledNumber(number, matcher.group().length());
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Returns the number with leading zeros
	 * Example:
	 * Number is 2 and length is 4:
	 * This method returns: 0002
	 * If the length is smaller than the count of chars of the number
	 * the length is automatically increased
	 * 
	 * @param number Number
	 * @param len Length
	 * @return Number with leading zeros
	 */
	public static String getZeroFilledNumber(int number, int len) {
		String strNumber = String.valueOf(number);
		int numberLen = strNumber.length();
		if (len == numberLen) {
			return strNumber;
		} else {
			if (len < numberLen) {
				len = numberLen;
			}
			char[] chars = new char[len - numberLen];
			Arrays.fill(chars, '0');
			String zeroFilledPrefix = new String(chars);
			return zeroFilledPrefix + strNumber;
		}
	}

	/**
	 * Method to reduce length of path (without filename), because paths in Windows Explorer can only be have 255 chars.
	 * 
	 * @param folder Path without Filename
	 * @return Reduced Path
	 */
	public static String reducePathLength(String folder) {
		return reducePathLength(folder, PATH_WITHOUT_FILENAME_LENGTH_LIMIT);
	}

	/**
	 * Method to reduce length of path (without filename), because paths in Windows Explorer can only be have 255 chars.
	 * 
	 * @param folder Path without Filename
	 * @param lengthLimit Limit
	 * @return Reduced Path
	 */
	public static String reducePathLength(String folder, int lengthLimit) {
		int folderLength = folder.length();
		if (folderLength <= lengthLimit) {
			return folder;
		}

		char lastCharacter = folder.charAt(folderLength - 1);
		boolean endsWithPathSeparator = lastCharacter == FILE_SEPERATOR_CHAR_WINDOWS || lastCharacter == FILE_SEPERATOR_CHAR_LINUX;

		int limit;
		if (endsWithPathSeparator) {
			/*
			 * Reduce one more character and add path separater again later
			 */
			limit = lengthLimit - 1;
		} else {
			limit = lengthLimit;
		}
		if (limit > folder.length()) {
			limit = folder.length();
		}

		String reducedPath = folder.substring(0, limit);
		String trimmedPath = pathRTrim(reducedPath);

		if (endsWithPathSeparator) {
			return trimmedPath + lastCharacter;
		} else {
			return trimmedPath;
		}
	}

	/**
	 * Method to reduce length of filename, because paths in Windows Explorer can only be have 255 chars.
	 * 
	 * @param filename Filename
	 * @return Reduced Filename
	 */
	public static String reduceFilenameLength(String filename) {
		return reduceFilenameLength(filename, FILENAME_LENGTH_LIMIT);
	}

	/**
	 * Method to reduce length of filename, because paths in Windows Explorer can only be have 255 chars.
	 * 
	 * @param filename Filename
	 * @param lengthLimit Limit
	 * @return Reduced Filename
	 */
	public static String reduceFilenameLength(String filename, int lengthLimit) {
		if (filename.length() <= lengthLimit) {
			return filename;
		}

		String filenamePrefix = getFilePrefix(filename);
		String ext = getFileExtension(filename);
		int extLength = !ext.isEmpty() ? ext.length() + 1 : 0;

		int limit = lengthLimit - extLength;
		if (limit < 1) {
			// Prevent empty filename prefix
			limit = 1;
		}
		if (limit > filenamePrefix.length()) {
			limit = filenamePrefix.length();
		}

		String result = filenamePrefix.substring(0, limit);

		if (!ext.isEmpty()) {
			return result + "." + ext;
		} else {
			return result;
		}
	}

	/**
	 * Method to reduce lenght of a String which is not a filename or a path.
	 * 
	 * @param text Text
	 * @param limit Limit
	 * @return Reduced Text
	 */
	public static String reduceTextLength(String text, int limit) {
		if (limit < 0 || limit > text.length()) {
			return text;
		}
		return text.substring(0, limit);
	}

	/**
	 * This method filters not allowed chars in paths or filenames
	 * You must set noPath to true, if the string is only a filename.
	 * 
	 * @param str String
	 * @param noPath Is no path (only filename)
	 * @param mode Mode
	 * @return Filtered String
	 */
	private static String filterPath(String str, boolean noPath, int mode) {
		FilenameFilter filter = switch (mode) {
			case FILENAME_ASCII_ONLY -> filenameAsciiOnlyFilter;
			case FILENAME_ASCII_UMLAUT -> filenameAsciiUmlautFilter;
			case FILENAME_ALL -> filenameAllFilter;
			default -> filenameAllFilter;
		};
		return filter.filter(str, noPath);
	}

	/**
	 * This method filters not allowed chars in paths (including filename if available)
	 * 
	 * @param path Path
	 * @param mode Mode
	 * @return Filtered path
	 */
	public static String filterPath(String path, int mode) {
		return filterPath(path, false, mode);
	}

	/**
	 * This method filters not allowed chars in filenames
	 * 
	 * @param filename Filename
	 * @param mode Mode
	 * @return Filtered Filename
	 */
	public static String filterFilename(String filename, int mode) {
		return filterPath(filename, true, mode);
	}

	/**
	 * Class for filtering filenames
	 */
	private static class FilenameFilter {
		protected final Pattern notAllowedFilenameCharsPattern = Pattern.compile("[\"*<>?|/:\\\\]");

		protected final Pattern notAllowedFolderCharsPattern = Pattern.compile("[\"*<>?|]");

		protected final Map<Pattern, String> patterns = new LinkedHashMap<>();

		/**
		 * Filter Filename
		 * 
		 * @param filename Filename
		 * @param noPath Is no path (only filename)
		 * @return Filtered Filename
		 */
		public String filter(String filename, boolean noPath) {
			// Filter first with patterns of specific implementations
			String result = filterSpecialPatterns(filename);

			// Filter quote
			result = result.replace("\"", "'");

			if (noPath) {
				// if the String is a filename (not a path) we filter more chars
				result = notAllowedFilenameCharsPattern.matcher(result).replaceAll("");
			} else {
				result = notAllowedFolderCharsPattern.matcher(result).replaceAll("");

				/*
				 * If we have a Windows-Path we need to also remove : chars except the one after the drive letter
				 */
				Matcher matcherWindowsDriveLetter = PATTERN_WINDOWS_DRIVE_LETTER.matcher(result);
				if (matcherWindowsDriveLetter.matches()) {
					String driveLetter = result.substring(0, 2);
					String remainingPath = result.substring(2);
					remainingPath = remainingPath.replace(":", "");
					result = driveLetter + remainingPath;
				}
			}
			return result;
		}

		private String filterSpecialPatterns(String filename) {
			String result = filename;
			for (Map.Entry<Pattern, String> entry : patterns.entrySet()) {
				result = entry.getKey().matcher(result).replaceAll(entry.getValue());
			}
			return result;
		}
	}

	/**
	 * Class for filtering filename, allowing only ASCII chars
	 */
	private static class FilenameAsciiOnlyFilter extends FilenameFilter {
		/**
		 * Constructor
		 */
		public FilenameAsciiOnlyFilter() {
			// Replace some not allowed chars by suitable alternatives
			patterns.put(Pattern.compile("[ö]"), "oe");
			patterns.put(Pattern.compile("[òóôõ]"), "o");
			patterns.put(Pattern.compile("[Ö]"), "OE");
			patterns.put(Pattern.compile("[ÒÓÔÕ]"), "O");

			patterns.put(Pattern.compile("[ä]"), "ae");
			patterns.put(Pattern.compile("[àáâãå]"), "a");
			patterns.put(Pattern.compile("[Ä]"), "AE");
			patterns.put(Pattern.compile("[ÀÁÂÃÅ]"), "A");

			patterns.put(Pattern.compile("[ü]"), "ue");
			patterns.put(Pattern.compile("[ùúûµ]"), "u");
			patterns.put(Pattern.compile("[Ü]"), "UE");
			patterns.put(Pattern.compile("[ÙÚÛ]"), "U");

			patterns.put(Pattern.compile("[èéêë]"), "e");
			patterns.put(Pattern.compile("[ÈÉÊË]"), "E");

			patterns.put(Pattern.compile("[ìíîï]"), "i");
			patterns.put(Pattern.compile("[ÌÍÎÏ]"), "I");

			patterns.put(Pattern.compile("[ß]"), "ss");

			// If the user only allows ASCII chars then filter all other
			patterns.put(Pattern.compile("[^\\x20-\\x7E]"), "");
		}
	}

	/**
	 * Class for filtering filename, allowing only ASCII and umlaut chars
	 */
	private static class FilenameAsciiUmlautFilter extends FilenameFilter {
		/**
		 * Constructor
		 */
		public FilenameAsciiUmlautFilter() {
			// If the user only allows ASCII chars and umlauts then filter all other
			patterns.put(Pattern.compile("[^\\x20-\\x7E\\xA0-\\xFF]"), "");
		}
	}
}
