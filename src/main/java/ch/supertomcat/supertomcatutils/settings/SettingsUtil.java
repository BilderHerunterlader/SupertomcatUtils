package ch.supertomcat.supertomcatutils.settings;

/**
 * Class which provides methods to parse values from String
 */
public final class SettingsUtil {
	/**
	 * Constructor
	 */
	private SettingsUtil() {
	}

	/**
	 * Parse Value
	 * 
	 * @param dataType Data Type
	 * @param strValue Value as String
	 * @return Parsed Value
	 */
	public static Object parseValue(String dataType, String strValue) {
		if (dataType.equals("boolean")) {
			return parseBooleanValue(strValue, false);
		} else if (dataType.equals("int")) {
			return parseIntValue(strValue, 0);
		} else if (dataType.equals("long")) {
			return parseLongValue(strValue, 0);
		} else if (dataType.equals("string")) {
			return strValue;
		} else if (dataType.equals("byte")) {
			return parseByteValue(strValue, (byte)0);
		} else if (dataType.equals("short")) {
			return parseShortValue(strValue, (short)0);
		} else if (dataType.equals("float")) {
			return parseFloatValue(strValue, 0);
		} else if (dataType.equals("double")) {
			return parseDoubleValue(strValue, 0);
		} else {
			throw new IllegalArgumentException("Data Type is not supported: " + dataType);
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	public static int parseIntValue(String value, int defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	public static long parseLongValue(String value, long defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	public static boolean parseBooleanValue(String value, boolean defVal) {
		if (value == null) {
			return defVal;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	public static short parseShortValue(String value, short defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Short.parseShort(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	public static byte parseByteValue(String value, byte defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Byte.parseByte(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	public static float parseFloatValue(String value, float defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	public static double parseDoubleValue(String value, double defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}
}
