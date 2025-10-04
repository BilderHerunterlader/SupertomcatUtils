package ch.supertomcat.supertomcatutils.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This class provides methods for working with URL's
 */
public final class HTTPUtil {
	private static final String[] URL_SCHEMES = new String[] { "http", "https" };

	/**
	 * Pattern to trim spaces from the start of a string
	 */
	private static final Pattern LEFT_TRIM_PATTERN = Pattern.compile("^[\\s]+");

	/**
	 * Pattern to trim spaces from the end of a string
	 */
	private static final Pattern RIGHT_TRIM_PATTERN = Pattern.compile("[\\s]+$");

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(HTTPUtil.class);

	/**
	 * Constructor
	 */
	private HTTPUtil() {
	}

	/**
	 * Checks if the given String is an URL
	 * 
	 * @param url URL
	 * @return True if the given String is an URL, false otherwise
	 */
	public static boolean isURL(String url) {
		UrlValidator urlValidator = new UrlValidator(URL_SCHEMES, UrlValidator.ALLOW_2_SLASHES + UrlValidator.ALLOW_LOCAL_URLS);
		return urlValidator.isValid(url);
	}

	/**
	 * Trims an url
	 * 
	 * @param url URL
	 * @return Trimmed URL
	 */
	public static String trimURL(String url) {
		/*
		 * We check first if the url is a local file on the harddisk, because
		 * the url could be such a file and in this case, we can't trim.
		 */
		if (!url.isEmpty() && HTTPUtil.isURL(url)) {
			url = LEFT_TRIM_PATTERN.matcher(url).replaceAll("");
			url = RIGHT_TRIM_PATTERN.matcher(url).replaceAll("");
		}
		return url;
	}

	/**
	 * Parse URI
	 * 
	 * @param uri URI
	 * @return URI
	 */
	public static URI parseURI(String uri) {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(uri).build();
		return uriComponents.toUri();
	}

	/**
	 * Parse URL
	 * 
	 * @param url URL
	 * @return URL
	 * @throws MalformedURLException
	 */
	public static URL parseURL(String url) throws MalformedURLException {
		return parseURI(url).toURL();
	}

	/**
	 * Encodes the given URL. If the method fails to encode the URL the given URL is returned.
	 * 
	 * If the checkForPercentCharacter is true and a percent character is found in the URL, then the URL will not be encoded and just returned.
	 * 
	 * @param url Decoded URL
	 * @param checkForPercentCharacter Flag if URL should be checked for percent character to prevent double encoding
	 * @return Encoded URL
	 */
	public static String encodeURL(String url, boolean checkForPercentCharacter) {
		String encodedURL = url;
		// If checkForPercentCharacter flag is true and a percent character is found, we don't encode
		if (checkForPercentCharacter && url.contains("%")) {
			return encodedURL;
		}
		try {
			URL parsedURL = parseURL(url);
			// Use this URI constructor with mutliple parts of the URL, because the normal constructor or toURI will not do any encoding
			URI uri = new URI(parsedURL.getProtocol(), parsedURL.getUserInfo(), parsedURL.getHost(), parsedURL.getPort(), parsedURL.getPath(), parsedURL.getQuery(), parsedURL.getRef());
			encodedURL = uri.toASCIIString();
		} catch (MalformedURLException | URISyntaxException e) {
			logger.error("Could not encode URL, because it is malformed: {}", url, e);
		}

		logger.debug("URL: {}, EncodedURL: {}", url, encodedURL);
		return encodedURL;
	}

	/**
	 * Encodes the given URL. If the method fails to encode the URL the given URL is returned. Only unencoded/decoded URLs should be passed to this method,
	 * because encoded URLs would be encoded again!
	 * 
	 * @param url Decoded URL
	 * @return Encoded URL
	 */
	public static String encodeURL(String url) {
		return encodeURL(url, true);
	}

	/**
	 * Decodes the given URL. If the method fails to decode the URL the given URL is returned.
	 * 
	 * @param url Encoded URL
	 * @return Decoded URL
	 */
	public static String decodeURL(String url) {
		try {
			return decodeURL(parseURL(url));
		} catch (MalformedURLException | URISyntaxException e) {
			logger.error("Could not decode URL, because it is malformed: {}", url, e);
			return url;
		}
	}

	/**
	 * Decodes the given URL. If the method fails to decode the URL the given URL is returned.
	 * 
	 * @param url Encoded URL
	 * @return Decoded URL
	 * @throws URISyntaxException
	 */
	public static String decodeURL(URL url) throws URISyntaxException {
		// Use toURI, so that the URL is not encoded again
		URI uri = url.toURI();

		/*
		 * Unfortunately there seems to be no way to get the full decoded URL from the URI, so we have to get the decoded parts and put them together to a
		 * full URL here.
		 */

		StringBuilder sb = new StringBuilder();

		String scheme = uri.getScheme();
		if (scheme != null) {
			sb.append(scheme);
			sb.append(':');
		}
		if (uri.isOpaque()) {
			sb.append(uri.getSchemeSpecificPart());
		} else {
			String host = uri.getHost();
			String authority = uri.getAuthority();
			if (host != null) {
				sb.append("//");
				String userInfo = uri.getUserInfo();
				if (userInfo != null) {
					sb.append(userInfo);
					sb.append('@');
				}

				boolean bracketsNeeded = (host.indexOf(':') >= 0) && !host.startsWith("[") && !host.endsWith("]");
				if (bracketsNeeded) {
					sb.append('[');
				}
				sb.append(host);
				if (bracketsNeeded) {
					sb.append(']');
				}
				int port = uri.getPort();
				if (port != -1) {
					sb.append(':');
					sb.append(port);
				}
			} else if (authority != null) {
				sb.append("//");
				sb.append(authority);
			}
			String path = uri.getPath();
			if (path != null) {
				sb.append(path);
			}
			String query = uri.getQuery();
			if (query != null) {
				sb.append('?');
				sb.append(query);
			}
		}
		String fragment = uri.getFragment();
		if (fragment != null) {
			sb.append('#');
			sb.append(fragment);
		}
		String decodedURL = sb.toString();

		logger.debug("URL: {}, DecodedURL: {}", url, decodedURL);
		return decodedURL;
	}

	/**
	 * Returns the domain of a URL
	 * If the domain could not be found an empty
	 * String is returned
	 * 
	 * @param url URL
	 * @return Domain
	 */
	public static String getDomainFromURL(String url) {
		try {
			URL parsedURL = parseURL(url);
			return parsedURL.getHost();
		} catch (MalformedURLException e) {
			logger.debug("Could not get domain from URL: {}", url, e);
			return "";
		}
	}

	/**
	 * Returns the domain and path part of a URL
	 * If the url could not be splitted, null is returned
	 * 
	 * @param url URL
	 * @return String-Array
	 */
	public static String[] getDomainAndPathFromURL(String url) {
		String schemeSearch = "://";
		int posDomain = url.indexOf(schemeSearch);
		if (posDomain > -1) {
			int posPath = url.indexOf('/', posDomain + schemeSearch.length());
			if (posPath > -1) {
				return new String[] { url.substring(0, posPath + 1), url.substring(posPath + 1) };
			}
		}
		return null;
	}

	/**
	 * Returns only the name of a TLD
	 * 
	 * @param domain Domain
	 * @return TLD-Name
	 */
	public static String getTLDName(String domain) {
		int posTldDot = domain.lastIndexOf('.');
		if (posTldDot > 0) {
			int posSubDomainDot = domain.lastIndexOf('.', posTldDot - 1);
			if (posSubDomainDot > -1) {
				return domain.substring(posSubDomainDot + 1);
			}
		}
		return domain;
	}

	/**
	 * Returns the filename from the url if possible or otherwise the defaultValue
	 * 
	 * @param url URL
	 * @param defaultValue DefaultValue
	 * @return Filename
	 */
	public static String getFilenameFromURL(String url, String defaultValue) {
		try {
			URL parsedURL = parseURL(url);
			String path = parsedURL.getPath();

			int pos = path.lastIndexOf("/");
			if (pos > -1) {
				return path.substring(pos + 1);
			} else {
				return defaultValue;
			}
		} catch (MalformedURLException e) {
			logger.debug("Could not get filename from URL: {}", url, e);
			return defaultValue;
		}
	}

	/**
	 * @param url URL
	 * @return Base-URL
	 */
	public static String getBaseURL(String url) {
		int posPath = url.lastIndexOf("/");
		if (posPath > 0) {
			return url.substring(0, posPath + 1);
		}
		return url;
	}

	/**
	 * @param referrer
	 * @param urlToConvert URL
	 * @return Converted URL
	 */
	public static String convertURLFromRelativeToAbsolute(String referrer, String urlToConvert) {
		String baseURL = getBaseURL(referrer);
		if (!baseURL.endsWith("/")) {
			return baseURL + "/" + urlToConvert;
		} else {
			return baseURL + urlToConvert;
		}
	}
}
