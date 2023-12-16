package ch.supertomcat.supertomcatutils.http.cookies.ie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.http.HTTPUtil;
import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;

/**
 * Class for reading cookies of IE
 */
public final class IECookies {
	private static final Pattern COOKIE_DOMAIN_PATTERN = Pattern.compile("^.*?@(.*?)\\[[0-9]+\\]\\.txt$");

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(IECookies.class);

	/**
	 * Constructor
	 */
	private IECookies() {
	}

	/**
	 * Returns cookies from IE
	 * 
	 * @param domain Domain
	 * @param hosts Hosts
	 * @return Cookies
	 */
	public static List<BrowserCookie> getCookiesFromIE(final String domain, final String[] hosts) {
		String os = System.getProperty("os.name").toLowerCase();
		if (!os.contains("windows")) {
			return new ArrayList<>();
		}

		try {
			File folder = new File(System.getProperty("user.home"), "Cookies");
			if (folder.exists() && folder.isDirectory()) {
				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if (pathname.isFile()) {
							String cookieDomain = COOKIE_DOMAIN_PATTERN.matcher(pathname.getName()).replaceAll("$1");

							boolean matchedDomain = cookieDomain.equals(HTTPUtil.getTLDName(domain));
							if (!matchedDomain) {
								for (int i = 0; i < hosts.length; i++) {
									matchedDomain = cookieDomain.equals(HTTPUtil.getTLDName(hosts[i].substring(1)));
									if (matchedDomain) {
										break;
									}
								}
							}

							return matchedDomain;
						}
						return false;
					}
				};

				List<BrowserCookie> allCookies = new ArrayList<>();

				File[] files = folder.listFiles(filter);
				if (files != null) {
					for (File file : files) {
						List<BrowserCookie> cookies = getCookiesFromIEFile(file, domain);
						allCookies.addAll(cookies);
					}
				}

				return allCookies;
			}
			return new ArrayList<>();
		} catch (NullPointerException e) {
			logger.error("Could not read cookies from IE", e);
			return new ArrayList<>();
		}
	}

	/**
	 * Returns the cookies from a IE-Cookie-File
	 * 
	 * @param f Cookie-File
	 * @param domain Domain
	 * @return Cookies
	 */
	private static List<BrowserCookie> getCookiesFromIEFile(File f, String domain) {
		try (FileInputStream in = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
			List<BrowserCookie> cookies = new ArrayList<>();

			BrowserCookie cookie = new BrowserCookie();
			cookie.setDomain("");
			int i = 0;
			String row = null;
			while ((row = br.readLine()) != null) {
				if (row.equals("*")) {
					i = 0;
					if ((domain + "/").matches("^.*" + cookie.getDomain() + "$")) {
						cookies.add(cookie);
					}
					cookie = new BrowserCookie();
					cookie.setDomain("");
					continue;
				}

				if ((i > -1) && (i < 8)) {
					switch (i) {
						case 0:
							// Name
							cookie.setName(row);
							break;
						case 1:
							// Content
							cookie.setValue(row);
							break;
						case 2:
							// Domain
							cookie.setDomain(row);
							break;
						case 3:
							// Nothing to do, because unknown field
							break;
						case 4:
							// Gueltigkeit
							// TODO How is the format?
							cookie.setExpiryDate(null);
							break;
						case 5:
							// Gueltigkeit
							// TODO How is the format?
							cookie.setExpiryDate(null);
							break;
						case 6:
							// Erstellungszeit
							// TODO How is the format?
							cookie.setCreationDate(null);
							break;
						case 7:
							// Erstellungszeit
							// TODO How is the format?
							cookie.setCreationDate(null);
							break;
						default:
							logger.error("Unknown field position: {}", i);
							break;
					}
				}
				i++;
			}

			return cookies;
		} catch (IOException e) {
			logger.error("Could not read cookies from IE: {}", f.getAbsolutePath(), e);
			return new ArrayList<>();
		}
	}
}
