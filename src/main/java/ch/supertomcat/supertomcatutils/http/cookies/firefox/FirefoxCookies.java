package ch.supertomcat.supertomcatutils.http.cookies.firefox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.io.CopyUtil;

/**
 * Class for reading cookies of Firefox
 */
public final class FirefoxCookies {
	private static final Pattern FIREFOX_TEXTFILE_COOKIE_PATTERN = Pattern.compile("^.*\t(FALSE|TRUE)\t\\/\t(FALSE|TRUE)\t.*\t.*\t.*");

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(FirefoxCookies.class);

	/**
	 * Object to synchronize the copying of the firefox db
	 */
	private static final Object firefoxCopyDBLock = new Object();

	/**
	 * Object to synchronize the firefox-db-requests
	 */
	private static final Object firefoxDBLock = new Object();

	/**
	 * Timestamp of last copying of the firefox-sqlite-file
	 */
	private static long lastFirefoxSqliteCopy = 0;

	private static FirefoxCookieComparator cookieComparator = new FirefoxCookieComparator();

	/**
	 * Constructor
	 */
	private FirefoxCookies() {
	}

	/**
	 * Returns cookies from Firefox
	 * Because of problems, when two thread are trying to read
	 * cookies from firefox 3 at the same time, i had to synchronize
	 * this method.
	 * 
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param cookieFile CookieFile for Firefox Version 1/2
	 * @param cookieFilev3 CookieFile for Firefox Version 3 and higher
	 * @return Cookies
	 */
	public static String getCookiesFromFirefox(String domain, String hosts[], String paths[], String cookieFile, String cookieFilev3) {
		logger.debug("Firefox: Cookiefile: " + cookieFile);
		logger.debug("Firefox: Cookiefilev3: " + cookieFilev3);
		File file = new File(cookieFile);
		File filev3 = new File(cookieFilev3);
		if (filev3.exists() && filev3.getName().endsWith(".sqlite")) {
			logger.debug("Firefox: v3: Cookie file exist, opening database...");

			/*
			 * Firefox 3.5 seems to lock the cookies.sqlite all the time, so
			 * we get a database locked exception, so the best way to avoid this,
			 * is copy the sqlite-file and work here with the copy.
			 */
			String newCookieFilev3 = cookieFilev3 + "bh_copy.sqlite";

			synchronized (firefoxCopyDBLock) {
				if (System.currentTimeMillis() > (lastFirefoxSqliteCopy + 180000)) {
					CopyUtil.copy(cookieFilev3, newCookieFilev3);
					lastFirefoxSqliteCopy = System.currentTimeMillis();
				}
			}

			try {
				return getCookiesFromFirefox3Sqlite(newCookieFilev3, domain, hosts, paths, firefoxDBLock, "Firefox: v3");
			} catch (ClassNotFoundException | SQLException ex) {
				logger.error("Could not read cookies from: {}", file.getAbsolutePath(), ex);
				return "";
			}
		} else if (file.exists() && file.getName().endsWith(".txt")) {
			logger.debug("Firefox: v2: Cookiefile exists, reading in the file...");
			try {
				return getCookiesFromFirefox2TextFile(cookieFile, domain, hosts, paths, firefoxDBLock, "Firefox: v2");
			} catch (IOException ex) {
				logger.error("Could not read cookies from: {}", file.getAbsolutePath(), ex);
				return "";
			}
		}
		return "";
	}

	/**
	 * Reads out the cookies from firefox v2 text file
	 * 
	 * @param cookieFile Cookiefile (text)
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param dbLockObject Lock Object for reading database
	 * @param browserName Browser Name for Logging
	 * @return Cookies
	 * @throws IOException
	 */
	public static String getCookiesFromFirefox2TextFile(String cookieFile, final String domain, String hosts[], String paths[], Object dbLockObject, String browserName) throws IOException {
		try (FileInputStream in = new FileInputStream(cookieFile); BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
			List<String[]> v = new ArrayList<>();

			String row = null;
			while ((row = br.readLine()) != null) {
				if (row.isEmpty() || row.startsWith("# ")) {
					continue;
				}

				if (FIREFOX_TEXTFILE_COOKIE_PATTERN.matcher(row).matches()) {
					String[] cookie = row.split("\t");
					if (cookie[0].startsWith("#HttpOnly_")) {
						cookie[0] = cookie[0].substring(10);
					}

					boolean matchedDomain = cookie[0].equals(domain);
					if (!matchedDomain) {
						for (String host : hosts) {
							matchedDomain = cookie[0].equals(host);
							if (matchedDomain) {
								break;
							}
						}
					}

					boolean matchedPath = false;
					for (String path : paths) {
						matchedPath = cookie[2].equals(path) || cookie[2].equals(path + "/");
						if (matchedPath) {
							break;
						}
					}

					if (matchedDomain && matchedPath) {
						v.add(cookie);
					}
				}
			}

			logger.debug("Firefox: v2: Found cookies: " + v.size());

			StringBuilder sbCookies = new StringBuilder();
			for (String[] cookiex : v) {
				if (sbCookies.length() > 0) {
					sbCookies.append("; ");
				}
				sbCookies.append(cookiex[5] + "=" + cookiex[6]);
			}
			return sbCookies.toString();
		} catch (IOException e) {
			logger.error("Could not read cookies from file: {}", cookieFile, e);
			throw e;
		}
	}

	/**
	 * Reads out the cookies from firefox v3 sqlite file
	 * 
	 * @param cookieFilev3 Cookiefile (sqlite)
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param dbLockObject Lock Object for reading database
	 * @param browserName Browser Name for Logging
	 * @return Cookies
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static String getCookiesFromFirefox3Sqlite(String cookieFilev3, final String domain, String hosts[], String paths[], Object dbLockObject,
			String browserName) throws ClassNotFoundException, SQLException {
		StringBuilder sbSQLQuery = new StringBuilder("SELECT * FROM moz_cookies WHERE (host = '" + domain + "' OR ");
		for (int i = 0; i < hosts.length; i++) {
			sbSQLQuery.append("host = '" + hosts[i] + "'");
			if (i < hosts.length - 1) {
				sbSQLQuery.append(" OR ");
			}
		}
		sbSQLQuery.append(") AND (");
		for (int i = 0; i < paths.length; i++) {
			sbSQLQuery.append("path = '" + paths[i] + "'");
			sbSQLQuery.append(" OR ");
			sbSQLQuery.append("path = '" + paths[i] + "/'");
			if (i < paths.length - 1) {
				sbSQLQuery.append(" OR ");
			}
		}
		sbSQLQuery.append(")");
		String sqlQuery = sbSQLQuery.toString();

		Class.forName("org.sqlite.JDBC");

		List<String[]> v = new ArrayList<>();

		synchronized (dbLockObject) {
			logger.debug(browserName + ": SQL-Query: " + sqlQuery);
			try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + cookieFilev3)) {
				try (Statement stat = con.createStatement()) {
					try (ResultSet rs = stat.executeQuery(sqlQuery)) {
						while (rs.next()) {
							/*
							 * 1: id
							 * 2: name
							 * 3: value
							 * 4: host
							 * 5: path
							 * 6: expiry
							 * 7: lastAccessed
							 * 8: isSecure
							 * 9: isHttpOnly
							 */
							String[] cookie = new String[8];
							cookie[0] = rs.getString("name");
							cookie[1] = rs.getString("value");
							cookie[2] = rs.getString("host");
							cookie[3] = rs.getString("path");
							cookie[4] = rs.getString("expiry");
							cookie[5] = rs.getString("lastAccessed");
							cookie[6] = rs.getString("isSecure");
							cookie[7] = rs.getString("isHttpOnly");
							v.add(cookie);
						}
						logger.debug(browserName + ": Found cookies: " + v.size());
					}
				}
			} catch (SQLException se) {
				logger.error("Could not read cookies from file: {}", cookieFilev3, se);
				throw se;
			}
		}

		// Sort Cookies
		Collections.sort(v, cookieComparator);

		StringBuilder sbCookies = new StringBuilder();
		for (String[] cookiex : v) {
			if (sbCookies.length() > 0) {
				sbCookies.append("; ");
			}
			sbCookies.append(cookiex[0] + "=" + cookiex[1]);
		}
		return sbCookies.toString();
	}

	/**
	 * Returns the path where the cookie-file from Firefox is stored
	 * 
	 * @return Path
	 */
	public static String getPathForFirefox() {
		String os = System.getProperty("os.name").toLowerCase();
		String firefoxPath;
		if (os.contains("windows")) {
			firefoxPath = System.getenv("APPDATA");
			firefoxPath += "/Mozilla/Firefox/";
		} else if (os.contains("mac")) {
			firefoxPath = System.getProperty("user.home");
			firefoxPath += "/Library/Application Support/Firefox/";
		} else {
			firefoxPath = System.getProperty("user.home");
			firefoxPath += "/.mozilla/firefox/";
		}

		File folder = new File(firefoxPath);
		if (!folder.exists()) {
			return "";
		}

		File profilesIniFile = new File(firefoxPath, "profiles.ini");
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
					absoluteProfilePath = firefoxPath + profilePath + System.getProperty("file.separator");
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

	/**
	 * Comparator for cookies
	 */
	private static class FirefoxCookieComparator implements Comparator<String[]> {
		@Override
		public int compare(String[] o1, String[] o2) {
			int nameComp = o1[0].compareTo(o2[0]);
			if (nameComp == 0) {
				// Cookies with longer paths should be before shorter paths
				if (o1[3].length() > o2[3].length()) {
					return -1;
				} else if (o1[3].length() < o2[3].length()) {
					return 1;
				}

				/*
				 * If there is a cookie with same name and same domain just with . before, then the one
				 * that better matches the actual domain should be before the other.
				 */
				if (o1[2].equals("." + o2[2])) {
					return 1;
				} else if (o2[2].equals("." + o1[2])) {
					return -1;
				}
			}

			return nameComp;
		}
	}
}
