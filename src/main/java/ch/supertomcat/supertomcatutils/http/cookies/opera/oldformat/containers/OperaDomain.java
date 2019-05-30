package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers;

import java.util.ArrayList;
import java.util.List;

/**
 * OperaDomain
 */
public class OperaDomain {
	/**
	 * Name (e.g. bar)
	 */
	private String name;

	/**
	 * Fully Qualified Name (e.g. ch.foo.bar)
	 */
	private String fullyQualifiedName;

	/**
	 * int8
	 */
	private byte accept;

	/**
	 * int8
	 */
	private byte notMatch;

	/**
	 * int8
	 */
	private byte thirdParty;

	/**
	 * Cookies
	 */
	private List<OperaCookie> cookies = new ArrayList<>();

	/**
	 * Paths
	 */
	private List<OperaPath> paths = new ArrayList<>();

	/**
	 * Sub Domains
	 */
	private List<OperaDomain> subDomains = new ArrayList<>();

	/**
	 * Constructor
	 */
	public OperaDomain() {
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
	 * @return the accept
	 */
	public byte getAccept() {
		return accept;
	}

	/**
	 * @param accept the accept to set
	 */
	public void setAccept(byte accept) {
		this.accept = accept;
	}

	/**
	 * @return the notMatch
	 */
	public byte getNotMatch() {
		return notMatch;
	}

	/**
	 * @param notMatch the notMatch to set
	 */
	public void setNotMatch(byte notMatch) {
		this.notMatch = notMatch;
	}

	/**
	 * @return the thirdParty
	 */
	public byte getThirdParty() {
		return thirdParty;
	}

	/**
	 * @param thirdParty the thirdParty to set
	 */
	public void setThirdParty(byte thirdParty) {
		this.thirdParty = thirdParty;
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
	 * Add Path
	 * 
	 * @param path Path
	 */
	public void addPath(OperaPath path) {
		paths.add(path);
	}

	/**
	 * Add Sub Domain
	 * 
	 * @param subDomain Sub Domain
	 */
	public void addSubDomain(OperaDomain subDomain) {
		subDomains.add(subDomain);
	}

	/**
	 * Returns the paths
	 * 
	 * @return paths
	 */
	public List<OperaPath> getPaths() {
		return paths;
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
			for (OperaPath path : paths) {
				allCookies.addAll(path.getCookies(true));
			}
			for (OperaDomain subDomain : subDomains) {
				allCookies.addAll(subDomain.getCookies(true));
			}
			return allCookies;
		} else {
			return getCookies();
		}
	}

	/**
	 * Returns the subDomains
	 * 
	 * @return subDomains
	 */
	public List<OperaDomain> getSubDomains() {
		return subDomains;
	}

	@Override
	public String toString() {
		return fullyQualifiedName;
	}
}
