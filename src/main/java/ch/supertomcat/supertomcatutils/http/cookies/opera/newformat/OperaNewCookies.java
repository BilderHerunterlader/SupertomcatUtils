package ch.supertomcat.supertomcatutils.http.cookies.opera.newformat;

import ch.supertomcat.supertomcatutils.http.cookies.webkit.WebkitCookies;

/**
 * Class for reading cookies of Opera (from Version 13 or higher)
 */
public class OperaNewCookies {
	/**
	 * Returns cookies from Opera
	 * Because of problems, when two thread are trying to read
	 * cookies from Opera at the same time, i had to synchronize
	 * this method.
	 * 
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param cookieFile CookieFile for Opera
	 * @return Cookies
	 */
	public static String getCookiesFromOpera(String domain, String hosts[], String paths[], String cookieFile) {
		return WebkitCookies.getCookiesFromWebkit(domain, hosts, paths, cookieFile);
	}

	/**
	 * Returns the path where the cookie-file from Opera is stored
	 * 
	 * TODO Implement correct path selection for Linux and MacOS
	 * 
	 * @return Path
	 */
	public static String getCookieFileForOpera() {
		String os = System.getProperty("os.name").toLowerCase();
		String operaCookieFile = "";
		if (os.contains("windows")) {
			operaCookieFile = System.getenv("APPDATA");
			operaCookieFile += "/Opera Software/Opera Stable/Cookies";
		} else if (os.contains("mac")) {
			operaCookieFile = System.getProperty("user.home");
			// TODO What is the actual path on Mac? This here is just an assumption...
			operaCookieFile += "Library/Preferences/Opera Preferences/Cookies";
		} else {
			operaCookieFile = System.getProperty("user.home");
			// TODO What is the actual path on Linux? This here is just an assumption...
			operaCookieFile += "/.opera/Cookies";
		}
		return operaCookieFile;
	}
}
