package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers;

/**
 * OperaCookie
 */
public class OperaCookie {
	private String domain;
	private String path;

	private String name;
	private String value;
	private String comment;
	private String commentURL;
	private String recvDomain;
	private String recvPath;
	private String portList;
	private int version;
	private boolean authenticate = false;
	private boolean server = false;
	private boolean secure = false;
	private boolean deleteProtected = false;
	private boolean thirdParty = false;
	private boolean password = false;
	private boolean prefixed = false;
	private long expires;
	private long lastUsed;

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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the commentURL
	 */
	public String getCommentURL() {
		return commentURL;
	}

	/**
	 * @param commentURL the commentURL to set
	 */
	public void setCommentURL(String commentURL) {
		this.commentURL = commentURL;
	}

	/**
	 * @return the recvDomain
	 */
	public String getRecvDomain() {
		return recvDomain;
	}

	/**
	 * @param recvDomain the recvDomain to set
	 */
	public void setRecvDomain(String recvDomain) {
		this.recvDomain = recvDomain;
	}

	/**
	 * @return the recvPath
	 */
	public String getRecvPath() {
		return recvPath;
	}

	/**
	 * @param recvPath the recvPath to set
	 */
	public void setRecvPath(String recvPath) {
		this.recvPath = recvPath;
	}

	/**
	 * @return the portList
	 */
	public String getPortList() {
		return portList;
	}

	/**
	 * @param portList the portList to set
	 */
	public void setPortList(String portList) {
		this.portList = portList;
	}

	/**
	 * Returns the version
	 * 
	 * @return version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Sets the version
	 * 
	 * @param version version
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Returns the authenticate
	 * 
	 * @return authenticate
	 */
	public boolean isAuthenticate() {
		return authenticate;
	}

	/**
	 * Sets the authenticate
	 * 
	 * @param authenticate authenticate
	 */
	public void setAuthenticate(boolean authenticate) {
		this.authenticate = authenticate;
	}

	/**
	 * @return the server
	 */
	public boolean isServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(boolean server) {
		this.server = server;
	}

	/**
	 * @return the secure
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * @param secure the secure to set
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * Returns the deleteProtected
	 * 
	 * @return deleteProtected
	 */
	public boolean isDeleteProtected() {
		return deleteProtected;
	}

	/**
	 * Sets the deleteProtected
	 * 
	 * @param deleteProtected deleteProtected
	 */
	public void setDeleteProtected(boolean deleteProtected) {
		this.deleteProtected = deleteProtected;
	}

	/**
	 * @return the thirdParty
	 */
	public boolean isThirdParty() {
		return thirdParty;
	}

	/**
	 * @param thirdParty the thirdParty to set
	 */
	public void setThirdParty(boolean thirdParty) {
		this.thirdParty = thirdParty;
	}

	/**
	 * @return the password
	 */
	public boolean isPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(boolean password) {
		this.password = password;
	}

	/**
	 * @return the prefixed
	 */
	public boolean isPrefixed() {
		return prefixed;
	}

	/**
	 * @param prefixed the prefixed to set
	 */
	public void setPrefixed(boolean prefixed) {
		this.prefixed = prefixed;
	}

	/**
	 * @return the expires
	 */
	public long getExpires() {
		return expires;
	}

	/**
	 * @param expires the expires to set
	 */
	public void setExpires(long expires) {
		this.expires = expires;
	}

	/**
	 * @return the lastUsed
	 */
	public long getLastUsed() {
		return lastUsed;
	}

	/**
	 * @param lastUsed the lastUsed to set
	 */
	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
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

	@Override
	public String toString() {
		return "OperaCookie [domain=" + domain + ", path=" + path + ", name=" + name + ", value=" + value + ", comment=" + comment + ", commentURL=" + commentURL + ", recvDomain=" + recvDomain
				+ ", recvPath=" + recvPath + ", portList=" + portList + ", version=" + version + ", authenticate=" + authenticate + ", server=" + server + ", secure=" + secure + ", deleteProtected="
				+ deleteProtected + ", thirdParty=" + thirdParty + ", password=" + password + ", prefixed=" + prefixed + ", expires=" + expires + ", lastUsed=" + lastUsed + "]";
	}
}
