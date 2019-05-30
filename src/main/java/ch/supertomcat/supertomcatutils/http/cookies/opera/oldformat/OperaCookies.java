package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat;

import java.io.File;
import java.util.List;

import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.OperaCookie;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.OperaCookieRoot;

/**
 * 
 *
 */
public class OperaCookies {
	/**
	 * Returns cookies from Opera
	 * 
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param cookieFile
	 * @return Cookies
	 */
	public static String getCookiesFromOpera(String domain, String hosts[], String paths[], String cookieFile) {
		if (cookieFile.isEmpty()) {
			return "";
		}

		OperaCookieRoot operaCookieRoot = new OperaCookieRoot(cookieFile);
		if (!operaCookieRoot.read()) {
			return "";
		}

		List<OperaCookie> cookies = operaCookieRoot.getAllCookies();

		StringBuilder sbCookies = new StringBuilder();
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
				if (sbCookies.length() > 0) {
					sbCookies.append("; ");
				}
				sbCookies.append(currentCookie.getName() + "=" + currentCookie.getValue());
			}
		}
		return sbCookies.toString();
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
			if (checkFileExists(operaCookieFile) == false) {
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
