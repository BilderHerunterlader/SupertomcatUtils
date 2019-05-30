package ch.supertomcat.supertomcatutils.http.cookies.webkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.Crypt32Util;

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
	public WebkitCookies() {
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
	public static String getCookiesFromWebkit(String domain, String hosts[], String paths[], String cookieFile) {
		String retval = "";
		logger.debug("Cookiefile: " + cookieFile);
		File file = new File(cookieFile);

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
			retval = getCookiesFromWebkitSqlite(newCookieFile, domain, hosts, paths, dbLock);
		} catch (ClassNotFoundException | SQLException ex) {
			logger.error("Could not read cookies from: {}", file.getAbsolutePath(), ex);
		}

		return retval;
	}

	/**
	 * Reads out the cookies from Webkit based browser's sqlite file
	 * 
	 * @param cookieFile Cookiefile (sqlite)
	 * @param domain Domain
	 * @param hosts Hosts-Array
	 * @param paths Paths-Array
	 * @param dbLockObject Lock Object for reading database
	 * @return Cookies
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static String getCookiesFromWebkitSqlite(String cookieFile, final String domain, String hosts[], String paths[], Object dbLockObject) throws ClassNotFoundException, SQLException {
		StringBuilder sbSQLQuery = new StringBuilder("SELECT * FROM cookies WHERE (host_key = '" + domain + "' OR ");
		for (int i = 0; i < hosts.length; i++) {
			sbSQLQuery.append("host_key = '" + hosts[i] + "'");
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
			logger.debug("SQL-Query: " + sqlQuery);
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
							String[] cookie = new String[9];
							cookie[0] = rs.getString("name");
							cookie[1] = rs.getString("value");
							cookie[2] = rs.getString("host_key");
							cookie[3] = rs.getString("path");
							cookie[4] = rs.getString("expires_utc");
							cookie[5] = rs.getString("last_access_utc");
							cookie[6] = rs.getString("secure");
							cookie[7] = rs.getString("httponly");
							byte[] encryptedValue = rs.getBytes("encrypted_value");
							cookie[8] = decryptValue(encryptedValue, cookie[0], cookie[2]);
							v.add(cookie);
						}
						logger.debug("Found cookies: " + v.size());
					}
				}
			} catch (SQLException se) {
				logger.error("Could not read cookies from file: {}", cookieFile, se);
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
			if (!cookiex[8].isEmpty()) {
				sbCookies.append(cookiex[0] + "=" + cookiex[8]);
			} else {
				sbCookies.append(cookiex[0] + "=" + cookiex[1]);
			}
		}
		return sbCookies.toString();
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
			return "";
		}
	}

	/**
	 * Comparator for cookies
	 */
	private static class WebkitCookieComparator implements Comparator<String[]> {
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
