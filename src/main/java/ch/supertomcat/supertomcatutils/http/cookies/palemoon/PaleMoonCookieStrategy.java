package ch.supertomcat.supertomcatutils.http.cookies.palemoon;

import java.util.List;
import java.util.Map;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
import ch.supertomcat.supertomcatutils.http.cookies.CookieStrategy;

/**
 * Strategy for Pale Moon Cookies
 */
public class PaleMoonCookieStrategy implements CookieStrategy {
	/**
	 * Key for cookie file option
	 */
	public static final String COOKIE_FILE_PALE_MOON_KEY = "cookieFilePaleMoon";

	@Override
	public List<BrowserCookie> getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options) {
		String cookieFilePaleMoon;

		if (options.containsKey(COOKIE_FILE_PALE_MOON_KEY)) {
			cookieFilePaleMoon = options.get(COOKIE_FILE_PALE_MOON_KEY);
		} else {
			// Get default cookie file path
			cookieFilePaleMoon = PaleMoonCookies.getPathForPaleMoon() + "cookies.sqlite";
		}

		return PaleMoonCookies.getCookiesFromPaleMoon(domain, hosts, paths, cookieFilePaleMoon);
	}
}
