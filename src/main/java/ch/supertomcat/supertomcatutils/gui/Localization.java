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
	 * Actual Language
	 */
	private static Locale currentLocale;

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
		currentLocale = new Locale(language, country);
		msg = ResourceBundle.getBundle(baseName, currentLocale);
	}

	/**
	 * Returns the localized String for a key. If the key is not found the key prefixed by "NOSTRINGFOUND_" is returned
	 * 
	 * @param key Key
	 * @return Localized String
	 */
	public static String getString(String key) {
		return getString(key, "NOSTRINGFOUND_");
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
