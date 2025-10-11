package ch.supertomcat.supertomcatutils.http.cookies.webkit;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.Crypt32Util;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
import ch.supertomcat.supertomcatutils.io.CopyUtil;

/**
 * Class for reading cookies of Webkit based browsers
 */
public class WebkitCookies {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(WebkitCookies.class);

	/**
	 * Object to synchronize the copying of the db
	 */
	private static final Object copyDBLock = new Object();

	/**
	 * Object to synchronize the db-requests
	 */
	private static final Object dbLock = new Object();

	/**
	 * Timestamp of last copying of the sqlite-file
	 */
	private static long lastSqliteCopy = 0;

	private static WebkitCookieComparator cookieComparator = new WebkitCookieComparator();

	/**
	 * Constructor
	 */
	private WebkitCookies() {
	}

	/**
	 * Returns cookies from Webkit based browsers
	 * Because of problems, when two thread are trying to read
	 * cookies from Webkit based browsers at the same time, i had to synchronize
	 * this method.
	 * 
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param cookieFile CookieFile for Opera
	 * @return Cookies
	 */
	public static List<BrowserCookie> getCookiesFromWebkit(String domain, String[] hosts, String[] paths, String cookieFile) {
		logger.debug("Cookiefile: {}", cookieFile);

		/*
		 * Opera seems to lock the cookies.sqlite all the time, so
		 * we get a database locked exception, so the best way to avoid this,
		 * is copy the sqlite-file and work here with the copy.
		 */
		String newCookieFile = cookieFile + "bh_copy.sqlite";

		synchronized (copyDBLock) {
			if (System.currentTimeMillis() > (lastSqliteCopy + 180000)) {
				CopyUtil.copy(cookieFile, newCookieFile);
				lastSqliteCopy = System.currentTimeMillis();
			}
		}

		try {
			return getCookiesFromWebkitSqlite(newCookieFile, domain, hosts, paths);
		} catch (ClassNotFoundException | SQLException ex) {
			logger.error("Could not read cookies from: {}", cookieFile, ex);
			return new ArrayList<>();
		}
	}

	/**
	 * Reads out the cookies from Webkit based browser's sqlite file
	 * 
	 * @param cookieFile Cookiefile (sqlite)
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @return Cookies
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static List<BrowserCookie> getCookiesFromWebkitSqlite(String cookieFile, final String domain, String[] hosts, String[] paths) throws ClassNotFoundException, SQLException {
		StringBuilder sbSQLQuery = new StringBuilder("SELECT * FROM cookies WHERE (host_key = '" + domain + "'");
		for (int i = 0; i < hosts.length; i++) {
			sbSQLQuery.append(" OR ");
			sbSQLQuery.append("host_key = '" + hosts[i] + "'");
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

		synchronized (dbLock) {
			logger.debug("SQL-Query: {}", sqlQuery);
			try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + cookieFile)) {
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
							 * 10: encrypted value
							 */

							String name = rs.getString("name");
							String value = rs.getString("value");
							String cookieDomain = rs.getString("host_key");
							String path = rs.getString("path");

							// expires_utc is in milliseconds
							String strExpiryDate = rs.getString("expires_utc");
							long expiryDate = Long.parseLong(strExpiryDate);
							Instant expiryDateInstant = Instant.ofEpochMilli(expiryDate);

							// last_access_utc is in milliseconds
							@SuppressWarnings("unused")
							String strLastAccess = rs.getString("last_access_utc");

							String strSecure = rs.getString("secure");
							boolean secure = "1".equals(strSecure);

							String strHttpOnly = rs.getString("httponly");
							boolean httpOnly = "1".equals(strHttpOnly);

							byte[] encryptedValue = rs.getBytes("encrypted_value");
							String decryptedValue = decryptValue(encryptedValue, name, cookieDomain);

							BrowserCookie cookie = new BrowserCookie(name, value);
							cookie.setDomain(cookieDomain);
							cookie.setPath(path);
							cookie.setExpiryDate(expiryDateInstant);
							cookie.setSecure(secure);
							cookie.setHttpOnly(httpOnly);

							// If encrypted value available overwrite value
							if (decryptedValue != null) {
								cookie.setValue(decryptedValue);
							}

							cookies.add(cookie);
						}
						logger.debug("Found cookies: {}", cookies.size());
					}
				}
			} catch (SQLException se) {
				logger.error("Could not read cookies from file: {}", cookieFile, se);
				throw se;
			}
		}

		// Sort Cookies
		Collections.sort(cookies, cookieComparator);
		return cookies;
	}

	/**
	 * Decrypt the encrypted cookie value
	 * 
	 * TODO Implement decryption for Linux and MacOS
	 * 
	 * @param encryptedValue Encrypted Cookie Value
	 * @param cookieName Cookie Name (for logging)
	 * @param domain Domain (for logging)
	 * @return Decrypted cookie value or an empty String if decryption failed
	 */
	private static String decryptValue(byte[] encryptedValue, String cookieName, String domain) {
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			byte[] decryptedData = Crypt32Util.cryptUnprotectData(encryptedValue);
			return new String(decryptedData);
		} else {
			logger.error("Could not decrypt cookie value for cookie in domain '{}', because OS is not supported: {}", domain, cookieName);
			return null;
		}
	}

	/**
	 * Comparator for cookies
	 */
	private static class WebkitCookieComparator implements Comparator<BrowserCookie> {
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
