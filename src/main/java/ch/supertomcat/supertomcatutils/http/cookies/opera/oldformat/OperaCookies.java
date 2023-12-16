package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.OperaCookie;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.OperaCookieRoot;

/**
 * Class for reading cookies of Opera
 */
public final class OperaCookies {
	/**
	 * Constructor
	 */
	private OperaCookies() {
	}

	/**
	 * Returns cookies from Opera
	 * 
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param cookieFile
	 * @return Cookies
	 */
	public static List<BrowserCookie> getCookiesFromOpera(String domain, String hosts[], String paths[], String cookieFile) {
		if (cookieFile.isEmpty()) {
			return new ArrayList<>();
		}

		OperaCookieRoot operaCookieRoot = new OperaCookieRoot(cookieFile);
		if (!operaCookieRoot.read()) {
			return new ArrayList<>();
		}

		List<OperaCookie> cookies = operaCookieRoot.getAllCookies();

		List<BrowserCookie> browserCookies = new ArrayList<>();
		for (OperaCookie currentCookie : cookies) {
			boolean matchedDomain = currentCookie.getDomain().equals(domain);
			if (!matchedDomain) {
				for (String host : hosts) {
					matchedDomain = currentCookie.getDomain().equals(host.substring(1));
					if (matchedDomain) {
						break;
					}
				}
			}

			boolean matchedPath = currentCookie.getPath().isEmpty();
			if (!matchedPath) {
				for (String path : paths) {
					matchedPath = currentCookie.getPath().equals(path) || currentCookie.getPath().equals(path + "/");
					if (matchedPath) {
						break;
					}
				}
			}

			if (matchedDomain && matchedPath) {
				BrowserCookie browserCookie = new BrowserCookie(currentCookie.getName(), currentCookie.getValue());
				browserCookie.setDomain(currentCookie.getDomain());
				browserCookie.setPath(currentCookie.getPath());
				browserCookie.setSecure(currentCookie.isSecure());
				browserCookie.setExpiryDate(Instant.ofEpochSecond(currentCookie.getExpires()));
				browserCookies.add(browserCookie);
			}
		}
		return browserCookies;
	}

	/**
	 * Returns the path to the cookie-file of Opera
	 * 
	 * @param checkExist Check Existance
	 * @return Cookie Pfad
	 */
	public static String getCookieFileForOpera(boolean checkExist) {
		String os = System.getProperty("os.name").toLowerCase();
		String operaCookieFile = "";
		if (os.contains("windows")) {
			operaCookieFile = System.getenv("APPDATA");
			operaCookieFile += "/Opera/Opera/profile/cookies4.dat";
			if (!checkFileExists(operaCookieFile)) {
				operaCookieFile = System.getenv("APPDATA");
				operaCookieFile += "/Opera/Opera/cookies4.dat";
			}
		} else if (os.contains("mac")) {
			operaCookieFile = System.getProperty("user.home");
			operaCookieFile += "Library/Preferences/Opera Preferences/cookies4.dat";
		} else {
			operaCookieFile = System.getProperty("user.home");
			operaCookieFile += "/.opera/cookies4.dat";
		}

		if (checkExist && !checkFileExists(operaCookieFile)) {
			return "";
		}

		return operaCookieFile;
	}

	private static boolean checkFileExists(String strFile) {
		return (new File(strFile)).exists();
	}
}
