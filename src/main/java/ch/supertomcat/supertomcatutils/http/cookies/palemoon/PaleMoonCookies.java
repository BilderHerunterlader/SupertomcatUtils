package ch.supertomcat.supertomcatutils.http.cookies.palemoon;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.http.cookies.firefox.FirefoxCookies;
import ch.supertomcat.supertomcatutils.io.CopyUtil;

/**
 * Class for reading cookies of Pale Moon
 */
public final class PaleMoonCookies {

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(PaleMoonCookies.class);

	/**
	 * Object to synchronize the copying of the Pale Moon db
	 */
	private static final Object paleMoonCopyDBLock = new Object();

	/**
	 * Object to synchronize the Pale Moon-db-requests
	 */
	private static final Object paleMoonDBLock = new Object();

	/**
	 * Timestamp of last copying of the Pale Moon-sqlite-file
	 */
	private static long lastPaleMoonSqliteCopy = 0;

	/**
	 * Constructor
	 */
	private PaleMoonCookies() {
	}

	/**
	 * Returns cookies from Pale Moon
	 * Because of problems, when two thread are trying to read
	 * cookies from Pale Moon at the same time, i had to synchronize
	 * this method.
	 * 
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param cookieFile CookieFile for PaleMoon
	 * @return Cookies
	 */
	public static String getCookiesFromPaleMoon(String domain, String[] hosts, String[] paths, String cookieFile) {
		String retval = "";
		logger.debug("Pale Moon: Cookiefile: {}", cookieFile);
		File file = new File(cookieFile);
		if (file.exists() && file.getName().endsWith(".sqlite")) {
			logger.debug("Pale Moon: Cookie file exist, opening database...");

			/*
			 * Pale Moon seems to lock the cookies.sqlite all the time, so
			 * we get a database locked exception, so the best way to avoid this,
			 * is copy the sqlite-file and work here with the copy.
			 */
			String newCookieFile = cookieFile + "bh_copy.sqlite";

			synchronized (paleMoonCopyDBLock) {
				if (System.currentTimeMillis() > (lastPaleMoonSqliteCopy + 180000)) {
					CopyUtil.copy(cookieFile, newCookieFile);
					lastPaleMoonSqliteCopy = System.currentTimeMillis();
				}
			}

			try {
				retval = getCookiesFromPaleMoonSqlite(newCookieFile, domain, hosts, paths);
			} catch (ClassNotFoundException | SQLException ex) {
				logger.error("Could not read cookies from: {}", file.getAbsolutePath(), ex);
			}
		}
		return retval;
	}

	/**
	 * Reads out the cookies from Pale Moon v3 sqlite file
	 * 
	 * @param cookieFile Cookiefile (sqlite)
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @return Cookies
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private static String getCookiesFromPaleMoonSqlite(String cookieFile, String domain, String[] hosts, String[] paths) throws ClassNotFoundException, SQLException {
		return FirefoxCookies.getCookiesFromFirefox3Sqlite(cookieFile, domain, hosts, paths, paleMoonDBLock, "Pale Moon");
	}

	/**
	 * Returns the path where the cookie-file from Pale Moon is stored
	 * 
	 * @return Path
	 */
	public static String getPathForPaleMoon() {
		String os = System.getProperty("os.name").toLowerCase();
		String paleMoonPath;
		if (os.contains("windows")) {
			paleMoonPath = System.getenv("APPDATA");
			paleMoonPath += "/Moonchild Productions/Pale Moon/";
		} else if (os.contains("mac")) {
			// TODO Is this the correct path for Pale Moon on Mac OS (So far this is just an assumption)
			paleMoonPath = System.getProperty("user.home");
			paleMoonPath += "/Library/Application Support/Pale Moon/";
		} else {
			paleMoonPath = System.getProperty("user.home");
			paleMoonPath += "/.moonchild productions/pale moon/";
		}

		File folder = new File(paleMoonPath);
		if (!folder.exists()) {
			return "";
		}

		File profilesIniFile = new File(paleMoonPath, "profiles.ini");
		try {
			int defaultProfileIndex = -1;
			List<String> paths = new ArrayList<>();

			Wini ini = new Wini(profilesIniFile);
			for (Map.Entry<String, Section> entry : ini.entrySet()) {
				logger.debug("Found section: {}", entry.getKey());

				if (!entry.getKey().startsWith("Profile")) {
					continue;
				}

				logger.debug("Section is a profile: {}", entry.getKey());
				Section section = entry.getValue();

				String relative = section.get("IsRelative", String.class, null);
				String profilePath = section.get("Path", String.class, null);
				String profileDefault = section.get("Default", String.class, null);
				if (relative == null || profilePath == null) {
					continue;
				}

				String absoluteProfilePath;
				if (relative.equals("1")) {
					absoluteProfilePath = paleMoonPath + profilePath + System.getProperty("file.separator");
				} else {
					absoluteProfilePath = profilePath + System.getProperty("file.separator");
				}
				paths.add(absoluteProfilePath);
				if (profileDefault != null && profileDefault.equals("1")) {
					defaultProfileIndex = paths.size() - 1;
				}
			}

			if (defaultProfileIndex > -1) {
				return paths.get(defaultProfileIndex);
			} else {
				if (!paths.isEmpty()) {
					return paths.get(0);
				} else {
					return "";
				}
			}
		} catch (IOException e) {
			logger.error("Could not read profiles.ini: {}", profilesIniFile.getAbsolutePath(), e);
			return "";
		}
	}
}
