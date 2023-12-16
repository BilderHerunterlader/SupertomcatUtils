package ch.supertomcat.supertomcatutils.http.cookies;

import java.util.List;
import java.util.Map;

/**
 * Cookie Strategy Interface
 */
public interface CookieStrategy {
	/**
	 * Returns the cookies for the given URL
	 * 
	 * @param url URL
	 * @param domain Domain
	 * @param hosts Hosts
	 * @param paths Paths
	 * @param options Options
	 * @return Cookies
	 */
	public List<BrowserCookie> getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options);
}
