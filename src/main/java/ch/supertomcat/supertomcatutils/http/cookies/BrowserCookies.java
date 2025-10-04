package ch.supertomcat.supertomcatutils.http.cookies;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.http.cookies.firefox.FirefoxCookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.ie.IECookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.opera.newformat.OperaNewCookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.OperaCookieStrategy;
import ch.supertomcat.supertomcatutils.http.cookies.palemoon.PaleMoonCookieStrategy;

/**
 * Utility class for loading cookies from browsers
 */
public final class BrowserCookies {
	/**
	 * No Cookies
	 */
	public static final int BROWSER_NO_COOKIES = 0;

	/**
	 * Cookies from Internet Explorer
	 */
	public static final int BROWSER_IE = 1;

	/**
	 * Cookies from Firefox
	 */
	public static final int BROWSER_FIREFOX = 2;

	/**
	 * Cookies from Opera up to Version 12
	 */
	public static final int BROWSER_OPERA = 3;

	/**
	 * Cookies from Firefox
	 */
	public static final int BROWSER_PALE_MOON = 4;

	/**
	 * Cookies from Opera from Version 13 or higher
	 */
	public static final int BROWSER_OPERA_NEW = 5;

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(BrowserCookies.class);

	private static final Map<Integer, CookieStrategy> cookieStrategies = new HashMap<>();

	static {
		cookieStrategies.put(BROWSER_NO_COOKIES, new NoCookieStrategy());
		cookieStrategies.put(BROWSER_IE, new IECookieStrategy());
		cookieStrategies.put(BROWSER_FIREFOX, new FirefoxCookieStrategy());
		cookieStrategies.put(BROWSER_OPERA, new OperaCookieStrategy());
		cookieStrategies.put(BROWSER_PALE_MOON, new PaleMoonCookieStrategy());
		cookieStrategies.put(BROWSER_OPERA_NEW, new OperaNewCookieStrategy());
	}

	/**
	 * Constructor
	 */
	private BrowserCookies() {
	}

	/**
	 * Sets the cookie strategy for the key
	 * 
	 * @param key Key
	 * @param cookieStrategy Cookie Strategy
	 */
	public static void setCookieStrategy(int key, CookieStrategy cookieStrategy) {
		cookieStrategies.put(key, cookieStrategy);
	}

	/**
	 * Returns the cookie strategy for the key
	 * 
	 * @param key Key
	 * @return Cookie strategy for the key
	 */
	public static CookieStrategy getCookieStrategy(int key) {
		return cookieStrategies.get(key);
	}

	/**
	 * @return Copy of the cookie strategy Map
	 */
	public static Map<Integer, CookieStrategy> getCookieStrategies() {
		return new HashMap<>(cookieStrategies);
	}

	/**
	 * Returns the Cookies for an URL from the given Browser
	 * 
	 * @param url URL
	 * @param browser Browser
	 * @return Cookies
	 */
	public static String getCookies(String url, int browser) {
		return convertToString(getBrowserCookies(url, browser));
	}

	/**
	 * Returns the Cookies for an URL from the given Browser
	 * 
	 * @param url URL
	 * @param browser Browser
	 * @return Cookies
	 */
	public static List<BrowserCookie> getBrowserCookies(String url, int browser) {
		if (!cookieStrategies.containsKey(browser)) {
			return new ArrayList<>();
		}
		CookieStrategy cookieStrategy = cookieStrategies.get(browser);
		return getBrowserCookies(url, cookieStrategy);
	}

	/**
	 * Returns the Cookies for an URL from the given Browser
	 * 
	 * @param url URL
	 * @param browser Browser
	 * @param cookieStrategyOptions Cookie Strategy Options
	 * @return Cookies
	 */
	public static String getCookies(String url, int browser, Map<String, String> cookieStrategyOptions) {
		return convertToString(getBrowserCookies(url, browser, cookieStrategyOptions));
	}

	/**
	 * Returns the Cookies for an URL from the given Browser
	 * 
	 * @param url URL
	 * @param browser Browser
	 * @param cookieStrategyOptions Cookie Strategy Options
	 * @return Cookies
	 */
	public static List<BrowserCookie> getBrowserCookies(String url, int browser, Map<String, String> cookieStrategyOptions) {
		if (!cookieStrategies.containsKey(browser)) {
			return new ArrayList<>();
		}
		CookieStrategy cookieStrategy = cookieStrategies.get(browser);
		return getBrowserCookies(url, cookieStrategy, cookieStrategyOptions);
	}

	/**
	 * Returns the Cookies for an URL
	 * 
	 * @param url URL
	 * @param cookieStrategy Cookie Strategy
	 * @return Cookies
	 */
	public static String getCookies(String url, CookieStrategy cookieStrategy) {
		return convertToString(getBrowserCookies(url, cookieStrategy));
	}

	/**
	 * Returns the Cookies for an URL
	 * 
	 * @param url URL
	 * @param cookieStrategy Cookie Strategy
	 * @return Cookies
	 */
	public static List<BrowserCookie> getBrowserCookies(String url, CookieStrategy cookieStrategy) {
		return getBrowserCookies(url, cookieStrategy, new HashMap<>());
	}

	/**
	 * Returns the Cookies for an URL
	 * 
	 * @param url URL
	 * @param cookieStrategy Cookie Strategy
	 * @param cookieStrategyOptions Cookie Strategy Options
	 * @return Cookies
	 */
	public static String getCookies(String url, CookieStrategy cookieStrategy, Map<String, String> cookieStrategyOptions) {
		return convertToString(getBrowserCookies(url, cookieStrategy, cookieStrategyOptions));
	}

	/**
	 * Returns the Cookies for an URL
	 * 
	 * @param url URL
	 * @param cookieStrategy Cookie Strategy
	 * @param cookieStrategyOptions Cookie Strategy Options
	 * @return Cookies
	 */
	public static List<BrowserCookie> getBrowserCookies(String url, CookieStrategy cookieStrategy, Map<String, String> cookieStrategyOptions) {
		URL completeURL;
		try {
			completeURL = HTTPUtil.parseURL(url);
		} catch (MalformedURLException mue) {
			return new ArrayList<>();
		}

		String domain = completeURL.getHost();
		if (domain.startsWith("[") && domain.endsWith("]")) {
			// Remove brackets for IPv6 Addresses
			domain = domain.substring(1, domain.length() - 1);
		}
		String path = completeURL.getPath();
		if (path.isEmpty()) {
			path = "/";
		}

		String[] hostsArr = domain.split("\\.");
		String[] pathsArr = path.split("/");

		String[] hosts = new String[hostsArr.length - 1];
		for (int i = 0; i < hostsArr.length - 1; i++) {
			hosts[i] = ".";
			for (int x = i; x < hostsArr.length; x++) {
				hosts[i] += hostsArr[x];
				if (x < (hostsArr.length - 1)) {
					hosts[i] += ".";
				}
			}
		}

		String[] paths = new String[(pathsArr.length > 0) ? pathsArr.length : 1];
		paths[0] = "/";
		for (int i = 1; i < pathsArr.length; i++) {
			paths[i] = paths[i - 1] + (i == 1 ? "" : "/") + pathsArr[i];
		}
		List<BrowserCookie> cookies = cookieStrategy.getCookies(url, domain, hosts, paths, cookieStrategyOptions);
		if (logger.isDebugEnabled()) {
			logger.debug("Cookies for '{}': {}", domain, convertToString(cookies));
		}
		return cookies;
	}

	/**
	 * Convert cookies to String
	 * 
	 * @param cookies Cookies
	 * @return Cookies as String
	 */
	public static String convertToString(List<BrowserCookie> cookies) {
		StringJoiner sjCookies = new StringJoiner("; ");
		for (BrowserCookie cookie : cookies) {
			sjCookies.add(cookie.getName() + "=" + cookie.getValue());
		}
		return sjCookies.toString();
	}
}
