package ch.supertomcat.supertomcatutils.http.cookies.firefox;

import java.util.List;
import java.util.Map;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
import ch.supertomcat.supertomcatutils.http.cookies.CookieStrategy;

/**
 * Strategy for Firefox Cookies
 */
public class FirefoxCookieStrategy implements CookieStrategy {
	/**
	 * Key for cookie file (for Version 1 or 2 of Firefox) option
	 */
	public static final String COOKIE_FILE_FF_KEY = "cookieFileFF";

	/**
	 * Key for cookie file (for Version 3 or above of Firefox) option
	 */
	public static final String COOKIE_FILE_FF_V3_KEY = "cookieFileFFv3";

	@Override
	public List<BrowserCookie> getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options) {
		String cookieFileFF;
		String cookieFileFFv3;

		if (options.containsKey(COOKIE_FILE_FF_KEY)) {
			cookieFileFF = options.get(COOKIE_FILE_FF_KEY);
		} else {
			// Get default cookie file path
			cookieFileFF = FirefoxCookies.getPathForFirefox() + "cookies.txt";
		}

		if (options.containsKey(COOKIE_FILE_FF_V3_KEY)) {
			cookieFileFFv3 = options.get(COOKIE_FILE_FF_V3_KEY);
		} else {
			// Get default cookie file path
			cookieFileFFv3 = FirefoxCookies.getPathForFirefox() + "cookies.sqlite";
		}

		return FirefoxCookies.getCookiesFromFirefox(domain, hosts, paths, cookieFileFF, cookieFileFFv3);
	}
}
