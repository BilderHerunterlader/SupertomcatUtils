package ch.supertomcat.supertomcatutils.http.cookies;

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
	public String getCookies(String url, String domain, String[] hosts, String[] paths, Map<String, String> options);
}
