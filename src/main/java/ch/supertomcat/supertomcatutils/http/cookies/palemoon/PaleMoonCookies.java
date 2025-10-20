package ch.supertomcat.supertomcatutils.http.cookies.palemoon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
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
	public static List<BrowserCookie> getCookiesFromPaleMoon(String domain, String[] hosts, String[] paths, String cookieFile) {
		logger.debug("Pale Moon: Cookiefile: {}", cookieFile);
		Path file = Paths.get(cookieFile);
		if (Files.exists(file) && file.getFileName().toString().endsWith(".sqlite")) {
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
				return getCookiesFromPaleMoonSqlite(newCookieFile, domain, hosts, paths);
			} catch (ClassNotFoundException | SQLException ex) {
				logger.error("Could not read cookies from: {}", file, ex);
				return new ArrayList<>();
			}
		}
		return new ArrayList<>();
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
	private static List<BrowserCookie> getCookiesFromPaleMoonSqlite(String cookieFile, String domain, String[] hosts, String[] paths) throws ClassNotFoundException, SQLException {
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

		Path folder = Paths.get(paleMoonPath);
		if (!Files.exists(folder)) {
			return "";
		}

		Path profilesIniFile = Paths.get(paleMoonPath, "profiles.ini");
		try (InputStream inputStream = Files.newInputStream(profilesIniFile); InputStreamReader reader = new InputStreamReader(inputStream, Charset.defaultCharset())) {
			int defaultProfileIndex = -1;
			List<String> paths = new ArrayList<>();

			INIConfiguration ini = new INIConfiguration();
			ini.read(reader);

			for (String sectionName : ini.getSections()) {
				logger.debug("Found section: {}", sectionName);

				if (!sectionName.startsWith("Profile")) {
					continue;
				}

				logger.debug("Section is a profile: {}", sectionName);
				SubnodeConfiguration section = ini.getSection(sectionName);

				String relative = section.getString("IsRelative", null);
				String profilePath = section.getString("Path", null);
				String profileDefault = section.getString("Default", null);

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
		} catch (IOException | ConfigurationException e) {
			logger.error("Could not read profiles.ini: {}", profilesIniFile, e);
			return "";
		}
	}
}
