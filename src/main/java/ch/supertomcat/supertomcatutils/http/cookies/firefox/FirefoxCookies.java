package ch.supertomcat.supertomcatutils.http.cookies.firefox;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
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
	public static List<BrowserCookie> getCookiesFromFirefox(String domain, String hosts[], String paths[], String cookieFile, String cookieFilev3) {
		logger.debug("Firefox: Cookiefile: {}", cookieFile);
		logger.debug("Firefox: Cookiefilev3: {}", cookieFilev3);
		Path file = Paths.get(cookieFile);
		Path filev3 = Paths.get(cookieFilev3);
		if (Files.exists(filev3) && filev3.getFileName().toString().endsWith(".sqlite")) {
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
				logger.error("Could not read cookies from: {}", file, ex);
				return new ArrayList<>();
			}
		} else if (Files.exists(file) && file.getFileName().toString().endsWith(".txt")) {
			logger.debug("Firefox: v2: Cookiefile exists, reading in the file...");
			try {
				return getCookiesFromFirefox2TextFile(cookieFile, domain, hosts, paths, "Firefox: v2");
			} catch (IOException ex) {
				logger.error("Could not read cookies from: {}", file, ex);
				return new ArrayList<>();
			}
		}
		return new ArrayList<>();
	}

	/**
	 * Reads out the cookies from firefox v2 text file
	 * 
	 * @param cookieFile Cookiefile (text)
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param browserName Browser Name for Logging
	 * @return Cookies
	 * @throws IOException
	 */
	public static List<BrowserCookie> getCookiesFromFirefox2TextFile(String cookieFile, final String domain, String[] hosts, String[] paths, String browserName) throws IOException {
		try (FileInputStream in = new FileInputStream(cookieFile); BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
			List<BrowserCookie> cookies = new ArrayList<>();

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
						BrowserCookie browserCookie = new BrowserCookie(cookie[5], cookie[6]);
						browserCookie.setDomain(cookie[0]);
						browserCookie.setPath(cookie[2]);
						browserCookie.setExpiryDate(Instant.ofEpochSecond(Long.parseLong(cookie[4])));
						browserCookie.setSecure("TRUE".equalsIgnoreCase(cookie[3]));
						cookies.add(browserCookie);
					}
				}
			}

			logger.debug("{}: Found cookies: {}", browserName, cookies.size());

			return cookies;
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
	public static List<BrowserCookie> getCookiesFromFirefox3Sqlite(String cookieFilev3, final String domain, String[] hosts, String[] paths, Object dbLockObject,
			String browserName) throws ClassNotFoundException, SQLException {
		StringBuilder sbSQLQuery = new StringBuilder("SELECT * FROM moz_cookies WHERE (host = '" + domain + "'");
		for (int i = 0; i < hosts.length; i++) {
			sbSQLQuery.append(" OR ");
			sbSQLQuery.append("host = '" + hosts[i] + "'");
		}
		sbSQLQuery.append(")");
		if (paths.length > 0) {
			sbSQLQuery.append(" AND (");
		}
		for (int i = 0; i < paths.length; i++) {
			sbSQLQuery.append("path = '" + paths[i] + "'");
			sbSQLQuery.append(" OR ");
			sbSQLQuery.append("path = '" + paths[i] + "/'");
			if (i < paths.length - 1) {
				sbSQLQuery.append(" OR ");
			}
		}
		if (paths.length > 0) {
			sbSQLQuery.append(")");
		}
		String sqlQuery = sbSQLQuery.toString();

		Class.forName("org.sqlite.JDBC");

		List<BrowserCookie> cookies = new ArrayList<>();

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
							String name = rs.getString("name");
							String value = rs.getString("value");
							String host = rs.getString("host");
							String path = rs.getString("path");

							// Expiry in seconds
							String strExpiry = rs.getString("expiry");
							long expiry = Long.parseLong(strExpiry);
							Instant expiryInstant = Instant.ofEpochSecond(expiry);

							// Last Accessed is in milliseconds
							@SuppressWarnings("unused")
							String strLastAccessed = rs.getString("lastAccessed");

							// creationTime is in milliseconds
							String strCreationTime = rs.getString("creationTime");
							long creationTime = Long.parseLong(strCreationTime);
							Instant creationTimeInstant = Instant.ofEpochMilli(creationTime);

							String strSecure = rs.getString("isSecure");
							boolean secure = "1".equals(strSecure);
							String strHttpOnly = rs.getString("isHttpOnly");
							boolean httpOnly = "1".equals(strHttpOnly);

							BrowserCookie cookie = new BrowserCookie(name, value);
							cookie.setDomain(host);
							cookie.setPath(path);
							cookie.setExpiryDate(expiryInstant);
							cookie.setCreationDate(creationTimeInstant);
							cookie.setSecure(secure);
							cookie.setHttpOnly(httpOnly);

							cookies.add(cookie);
						}
						logger.debug("{}: Found cookies: {}", browserName, cookies.size());
					}
				}
			} catch (SQLException se) {
				logger.error("Could not read cookies from file: {}", cookieFilev3, se);
				throw se;
			}
		}

		// Sort Cookies
		Collections.sort(cookies, cookieComparator);
		return cookies;
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

		Path folder = Paths.get(firefoxPath);
		if (!Files.exists(folder)) {
			return "";
		}

		Path profilesIniFile = Paths.get(firefoxPath, "profiles.ini");
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
		} catch (IOException | ConfigurationException e) {
			logger.error("Could not read profiles.ini: {}", profilesIniFile, e);
			return "";
		}
	}

	/**
	 * Comparator for cookies
	 */
	private static class FirefoxCookieComparator implements Comparator<BrowserCookie> {
		@Override
		public int compare(BrowserCookie o1, BrowserCookie o2) {
			int nameComp = o1.getName().compareTo(o2.getName());
			if (nameComp == 0) {
				// Cookies with longer paths should be before shorter paths
				if (o1.getPath().length() > o2.getPath().length()) {
					return -1;
				} else if (o1.getPath().length() < o2.getPath().length()) {
					return 1;
				}

				/*
				 * If there is a cookie with same name and same domain just with . before, then the one
				 * that better matches the actual domain should be before the other.
				 */
				if (o1.getDomain().equals("." + o2.getDomain())) {
					return 1;
				} else if (o2.getDomain().equals("." + o1.getDomain())) {
					return -1;
				}
			}

			return nameComp;
		}
	}
}
