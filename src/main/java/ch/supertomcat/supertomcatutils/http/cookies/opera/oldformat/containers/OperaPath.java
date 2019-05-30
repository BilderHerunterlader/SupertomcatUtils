package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers;

import java.util.ArrayList;
import java.util.List;

/**
 * OperaDomain
 */
public class OperaPath {
	/**
	 * Name of path part (e.g. bar)
	 */
	private String name;

	/**
	 * Fully Qualified Name (e.g. /foo/bar)
	 */
	private String fullyQualifiedName;

	/**
	 * Cookies
	 */
	private List<OperaCookie> cookies = new ArrayList<>();

	/**
	 * Sub Paths
	 */
	private List<OperaPath> subPaths = new ArrayList<>();

	/**
	 * Constructor
	 */
	public OperaPath() {
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the fullyQualifiedName
	 * 
	 * @return fullyQualifiedName
	 */
	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	/**
	 * Sets the fullyQualifiedName
	 * 
	 * @param fullyQualifiedName fullyQualifiedName
	 */
	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
	}

	/**
	 * Add Sub Path
	 * 
	 * @param subPath Sub Path
	 */
	public void addSubPath(OperaPath subPath) {
		subPaths.add(subPath);
	}

	/**
	 * Add Cookie
	 * 
	 * @param cookie Cookie
	 */
	public void addCookie(OperaCookie cookie) {
		cookies.add(cookie);
	}

	/**
	 * Returns the subPaths
	 * 
	 * @return subPaths
	 */
	public List<OperaPath> getSubPaths() {
		return subPaths;
	}

	/**
	 * Returns the cookies
	 * 
	 * @return cookies
	 */
	public List<OperaCookie> getCookies() {
		return cookies;
	}

	/**
	 * @param recursive Recursive
	 * @return the cookies
	 */
	public List<OperaCookie> getCookies(boolean recursive) {
		if (recursive) {
			List<OperaCookie> allCookies = new ArrayList<>();
			allCookies.addAll(cookies);
			for (OperaPath subPath : subPaths) {
				allCookies.addAll(subPath.getCookies(true));
			}
			return allCookies;
		} else {
			return getCookies();
		}
	}

	@Override
	public String toString() {
		return fullyQualifiedName;
	}
}
