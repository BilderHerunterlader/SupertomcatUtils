package ch.supertomcat.supertomcatutils.http.cookies;

import java.time.Instant;

/**
 * Browser Cookie
 */
public class BrowserCookie {
	/**
	 * Name
	 */
	private String name;

	/**
	 * Value
	 */
	private String value;

	/**
	 * Domain
	 */
	private String domain;

	/**
	 * Path
	 */
	private String path;

	/**
	 * Secure
	 */
	private boolean secure;

	/**
	 * HTTP Only
	 */
	private boolean httpOnly;

	/**
	 * Creation Date
	 */
	private Instant creationDate;

	/**
	 * Expiry Date
	 */
	private Instant expiryDate;

	/**
	 * Constructor
	 */
	public BrowserCookie() {
	}

	/**
	 * Constructor
	 * 
	 * @param name Name
	 * @param value Value
	 */
	public BrowserCookie(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Constructor
	 * 
	 * @param name Name
	 * @param value Value
	 * @param domain Domain
	 * @param path Path
	 * @param secure Secure
	 * @param httpOnly HTTP Only
	 * @param creationDate Creation Date
	 * @param expiryDate Expiry Date
	 */
	public BrowserCookie(String name, String value, String domain, String path, boolean secure, boolean httpOnly, Instant creationDate, Instant expiryDate) {
		this.name = name;
		this.value = value;
		this.domain = domain;
		this.path = path;
		this.secure = secure;
		this.httpOnly = httpOnly;
		this.creationDate = creationDate;
		this.expiryDate = expiryDate;
	}

	/**
	 * Returns the name
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name
	 * 
	 * @param name name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the value
	 * 
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value
	 * 
	 * @param value value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the domain
	 * 
	 * @return domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Sets the domain
	 * 
	 * @param domain domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Returns the path
	 * 
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path
	 * 
	 * @param path path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the secure
	 * 
	 * @return secure
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * Sets the secure
	 * 
	 * @param secure secure
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * Returns the httpOnly
	 * 
	 * @return httpOnly
	 */
	public boolean isHttpOnly() {
		return httpOnly;
	}

	/**
	 * Sets the httpOnly
	 * 
	 * @param httpOnly httpOnly
	 */
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	/**
	 * Returns the creationDate
	 * 
	 * @return creationDate
	 */
	public Instant getCreationDate() {
		return creationDate;
	}

	/**
	 * Sets the creationDate
	 * 
	 * @param creationDate creationDate
	 */
	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Returns the expiryDate
	 * 
	 * @return expiryDate
	 */
	public Instant getExpiryDate() {
		return expiryDate;
	}

	/**
	 * Sets the expiryDate
	 * 
	 * @param expiryDate expiryDate
	 */
	public void setExpiryDate(Instant expiryDate) {
		this.expiryDate = expiryDate;
	}
}
