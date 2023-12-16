package ch.supertomcat.supertomcatutils.http.cookies.ie;

import java.util.List;
import java.util.Map;

import ch.supertomcat.supertomcatutils.http.cookies.BrowserCookie;
import ch.supertomcat.supertomcatutils.http.cookies.CookieStrategy;

/**
 * Strategy for IE Cookies
 */
public class IECookieStrategy implements CookieStrategy {
	@Override
	public List<BrowserCookie> getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options) {
		return IECookies.getCookiesFromIE(domain, hosts);
	}
}
