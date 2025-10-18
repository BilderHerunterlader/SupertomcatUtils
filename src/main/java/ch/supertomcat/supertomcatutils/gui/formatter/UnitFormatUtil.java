package ch.supertomcat.supertomcatutils.gui.formatter;

import java.text.DecimalFormat;

/**
 * Utility class for formatting units
 */
public final class UnitFormatUtil {
	/**
	 * Display progressbar and only percent
	 */
	public static final int PROGRESSBAR_PERCENT = 0;

	/**
	 * Display progressbar and only filesize
	 */
	public static final int PROGRESSBAR_SIZE = 1;

	/**
	 * Display no progressbar and only percent
	 */
	public static final int NOPROGRESSBAR_PERCENT = 2;

	/**
	 * Display no progressbar and only filesize
	 */
	public static final int NOPROGRESSBAR_SIZE = 3;

	/**
	 * Takes automatically a suitable unit for displaying filesize
	 */
	public static final int AUTO_CHANGE_SIZE = 0;

	/**
	 * Display filesize only as bytes
	 */
	public static final int ONLY_B = 1;

	/**
	 * Display filesize only as kibibytes
	 */
	public static final int ONLY_KIB = 2;

	/**
	 * Display filesize only as mebibytes
	 */
	public static final int ONLY_MIB = 3;

	/**
	 * Display filesize only as gibibytes
	 */
	public static final int ONLY_GIB = 4;

	/**
	 * Display filesize only as tebibytes
	 */
	public static final int ONLY_TIB = 5;

	private static final String[] SIZE_UNITS = { "B", "KiB", "MiB", "GiB", "TiB" };

	private static final String[] BITRATE_UNITS = { "B/s", "KiB/s", "MiB/s", "GiB/s", "TiB/s" };

	private static final DecimalFormat SIZE_DECIMAL_FORMAT = new DecimalFormat("0.##");

	/**
	 * Constructor
	 */
	private UnitFormatUtil() {
	}

	/**
	 * Returns the bytes read in a suitable unit, or a specific
	 * unit, if the user defined that in the options
	 * 
	 * @param size Number of bytes read
	 * @param mode
	 * @return Progress-String
	 */
	public static String getSizeString(long size, int mode) {
		double sizec = size;
		int limit = SIZE_UNITS.length - 1;
		if ((mode > AUTO_CHANGE_SIZE) && (mode <= SIZE_UNITS.length)) {
			limit = mode - 1;
		}
		int c = 0;

		if (mode > AUTO_CHANGE_SIZE) {
			while (c < limit) {
				sizec /= 1024;
				c++; // ;-)
			}
		} else {
			while ((sizec > 1024) && (c < limit)) {
				sizec /= 1024;
				c++; // ;-)
			}
		}
		sizec = Math.round(sizec * 100) / 100.0;
		return SIZE_DECIMAL_FORMAT.format(sizec) + " " + SIZE_UNITS[c];
	}

	/**
	 * Returns the progress as percent
	 * 
	 * @param size Number of bytes read
	 * @param max Filesize
	 * @return Percent-String
	 */
	public static String getPercentString(long size, long max) {
		return (int)(((double)size / max) * 100.0d) + "%";
	}

	/**
	 * Calculates and returns the bitrate. If not possible -1 is returned
	 * 
	 * @param size Number of bytes read
	 * @param max Filesize
	 * @param timeStarted Start-Time
	 * @param timeNow Now
	 * @return Bitrate
	 */
	public static double getBitrate(long size, long max, long timeStarted, long timeNow) {
		if (max >= size) {
			long milliSecondsPassed = timeNow - timeStarted;
			if (milliSecondsPassed <= 0) {
				return -1;
			}
			double secondsPassed = milliSecondsPassed / 1000.0d;
			if (secondsPassed < 1) {
				// interpolate size to 1 second
				return 1000.0d * size / milliSecondsPassed;
			}
			return size / secondsPassed;
		}
		return -1;
	}

	/**
	 * Returns a String with the download rate
	 * 
	 * @param rate Bitrate
	 * @return String with the download rate
	 */
	public static String getBitrateString(double rate) {
		if (rate < 0) {
			return "";
		}
		int c = 0;

		while ((rate > 1024) && (c < BITRATE_UNITS.length - 1)) {
			rate /= 1024;
			c++; // ;-)
		}
		if (c >= 2) {
			return (Math.round(rate * 100) / 100.0) + " " + BITRATE_UNITS[c];
		} else {
			return (long)Math.rint(rate) + " " + BITRATE_UNITS[c];
		}
	}
}
