package ch.supertomcat.supertomcatutils.http.cookies;

import java.util.Map;

/**
 * Strategy for No Cookies
 */
public class NoCookieStrategy implements CookieStrategy {
	@Override
	public String getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options) {
		return "";
	}
}
