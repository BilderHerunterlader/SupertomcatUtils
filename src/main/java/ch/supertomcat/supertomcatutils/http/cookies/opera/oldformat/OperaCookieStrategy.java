package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat;

import java.util.List;
import java.util.Map;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
import ch.supertomcat.supertomcatutils.http.cookies.CookieStrategy;

/**
 * Strategy for Opera (up to Version 12) Cookies
 */
public class OperaCookieStrategy implements CookieStrategy {
	/**
	 * Key for cookie file option
	 */
	public static final String COOKIE_FILE_OPERA_KEY = "cookieFileOpera";

	@Override
	public List<BrowserCookie> getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options) {
		String cookieFileOpera;

		if (options.containsKey(COOKIE_FILE_OPERA_KEY)) {
			cookieFileOpera = options.get(COOKIE_FILE_OPERA_KEY);
		} else {
			// Get default cookie file path
			cookieFileOpera = OperaCookies.getCookieFileForOpera(true);
		}

		return OperaCookies.getCookiesFromOpera(domain, hosts, paths, cookieFileOpera);
	}
}
