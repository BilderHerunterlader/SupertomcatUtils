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

/**
 * 
 *
 */
public class IECookies {
	private static final Pattern COOKIE_DOMAIN_PATTERN = Pattern.compile("^.*?@(.*?)\\[[0-9]+\\]\\.txt$");

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(IECookies.class);

	/**
	 * Returns cookies from IE
	 * 
	 * @param domain Domain
	 * @param hosts Hosts
	 * @return Cookies
	 */
	public static String getCookiesFromIE(final String domain, final String hosts[]) {
		String os = System.getProperty("os.name").toLowerCase();
		if (!os.contains("windows")) {
			return "";
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

				StringBuilder sbRetval = new StringBuilder();

				File[] files = folder.listFiles(filter);
				if (files != null) {
					for (File file : files) {
						String cookie = getCookiesFromIEFile(file, domain);
						if (!cookie.isEmpty() && sbRetval.length() > 0) {
							sbRetval.append("; ");
						}
						sbRetval.append(cookie);
					}
				}

				return sbRetval.toString();
			}
			return "";
		} catch (NullPointerException e) {
			logger.error("Could not read cookies from IE", e);
			return "";
		}
	}

	/**
	 * Returns the cookies from a IE-Cookie-File
	 * 
	 * @param f Cookie-File
	 * @param domain Domain
	 * @return Cookies
	 */
	private static String getCookiesFromIEFile(File f, String domain) {
		StringBuilder sbRetval = new StringBuilder();
		try (FileInputStream in = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
			List<String[]> v = new ArrayList<>();

			String cookie[] = { "", "", "", "", "", "", "", "" };
			int i = 0;
			String row = null;
			while ((row = br.readLine()) != null) {
				if (row.equals("*")) {
					i = 0;
					if ((domain + "/").matches("^.*" + cookie[2] + "$")) {
						v.add(cookie);
					}
					cookie = new String[8];
					cookie[0] = ""; // Name
					cookie[1] = ""; // Content
					cookie[2] = ""; // Domain
					cookie[3] = ""; // Unbekannt
					cookie[4] = ""; // Gueltigkeit
					cookie[5] = ""; // Gueltigkeit
					cookie[6] = ""; // Erstellungszeit
					cookie[7] = ""; // Erstellungszeit
					continue;
				}

				if ((i > -1) && (i < 8)) {
					cookie[i] = row;
				}
				i++;
			}

			for (String[] cookiex : v) {
				if (sbRetval.length() > 0) {
					sbRetval.append("; ");
				}
				sbRetval.append(cookiex[0] + "=" + cookiex[1]);
			}
			return sbRetval.toString();
		} catch (IOException e) {
			logger.error("Could not read cookies from IE: {}", f.getAbsolutePath(), e);
			return "";
		}
	}
}
