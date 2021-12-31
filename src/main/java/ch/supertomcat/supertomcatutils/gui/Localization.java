package ch.supertomcat.supertomcatutils.gui;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Localization-Class for GUITools
 */
public final class Localization {
	/**
	 * File with the localized Strings
	 */
	private static ResourceBundle msg;

	/**
	 * Flag if the NOSTRINGFOUND_ prefix should be added, if there is no translation found for a key
	 */
	private static boolean notFoundPrefix;

	/**
	 * Constructor
	 */
	private Localization() {
	}

	/**
	 * Initialize localized Strings
	 * 
	 * @param baseName BaseName for localization
	 * @param language Language
	 * @param country Country
	 */
	public static void init(String baseName, String language, String country) {
		init(baseName, language, country, true);
	}

	/**
	 * Initialize localized Strings
	 * 
	 * @param baseName BaseName for localization
	 * @param language Language
	 * @param country Country
	 * @param notFoundPrefix Flag if the NOSTRINGFOUND_ prefix should be added, if there is no translation found for a key
	 */
	public static void init(String baseName, String language, String country, boolean notFoundPrefix) {
		Locale currentLocale = new Locale(language, country);
		msg = ResourceBundle.getBundle(baseName, currentLocale);
		Localization.notFoundPrefix = notFoundPrefix;
	}

	/**
	 * Returns the localized String for a key. If the key is not found and notFoundPrefix is true then the key prefixed by "NOSTRINGFOUND_" is returned
	 * 
	 * @param key Key
	 * @return Localized String
	 */
	public static String getString(String key) {
		if (notFoundPrefix) {
			return getString(key, "NOSTRINGFOUND_");
		} else {
			return getString(key, "");
		}
	}

	/**
	 * Returns the localized String for a key
	 * 
	 * @param key Key
	 * @param notFoundPrefix Prefix which is added in front of the key, when the key was not found. null will add no prefix.
	 * @return Localized String
	 */
	public static String getString(String key, String notFoundPrefix) {
		if (msg != null) {
			try {
				return msg.getString(key);
			} catch (MissingResourceException mre) {
				if (notFoundPrefix == null) {
					return key;
				} else {
					return notFoundPrefix + key;
				}
			}
		}
		if (notFoundPrefix == null) {
			return key;
		} else {
			return notFoundPrefix + key;
		}
	}
}
