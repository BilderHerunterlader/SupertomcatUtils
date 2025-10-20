package ch.supertomcat.supertomcatutils.http.cookies.ie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
			Path folder = Paths.get(System.getProperty("user.home"), "Cookies");
			if (Files.exists(folder) && Files.isDirectory(folder)) {
				Predicate<Path> filter = x -> {
					String cookieDomain = COOKIE_DOMAIN_PATTERN.matcher(x.getFileName().toString()).replaceAll("$1");

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
				};

				List<BrowserCookie> allCookies = new ArrayList<>();

				try (Stream<Path> stream = Files.list(folder)) {
					stream.filter(Files::isRegularFile).filter(filter).forEach(file -> {
						List<BrowserCookie> cookies = getCookiesFromIEFile(file, domain);
						allCookies.addAll(cookies);
					});
				} catch (IOException e) {
					logger.error("Could not read cookie files from: {}", folder, e);
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
	private static List<BrowserCookie> getCookiesFromIEFile(Path f, String domain) {
		try (InputStream in = Files.newInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(in, System.getProperty("native.encoding")))) {
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
			logger.error("Could not read cookies from IE: {}", f, e);
			return new ArrayList<>();
		}
	}
}
