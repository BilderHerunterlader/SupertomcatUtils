package ch.supertomcat.supertomcatutils.http.cookies.opera.newformat;

import java.util.Map;

import ch.supertomcat.supertomcatutils.http.cookies.CookieStrategy;

/**
 * Strategy for Opera (from Version 13 or higher) Cookies in new format
 */
public class OperaNewCookieStrategy implements CookieStrategy {
	/**
	 * Key for cookie file option
	 */
	public static final String COOKIE_FILE_OPERA_NEW_KEY = "cookieFileOperaNew";

	@Override
	public String getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options) {
		String cookieFileOpera;

		if (options.containsKey(COOKIE_FILE_OPERA_NEW_KEY)) {
			cookieFileOpera = options.get(COOKIE_FILE_OPERA_NEW_KEY);
		} else {
			// Get default cookie file path
			cookieFileOpera = OperaNewCookies.getCookieFileForOpera();
		}

		return OperaNewCookies.getCookiesFromOpera(domain, hosts, paths, cookieFileOpera);
	}
}
