package ch.supertomcat.supertomcatutils.application.libraries;

/**
 * Information about a library
 */
public class LibraryInfo implements Comparable<LibraryInfo> {
	/**
	 * Name
	 */
	private final String name;

	/**
	 * Version
	 */
	private final String version;

	/**
	 * License
	 */
	private final String license;

	/**
	 * Constructor
	 * 
	 * @param name Name
	 * @param version Version
	 * @param license License
	 */
	public LibraryInfo(String name, String version, String license) {
		this.name = name;
		this.version = version;
		this.license = license;
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
	 * Returns the version
	 * 
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the license
	 * 
	 * @return license
	 */
	public String getLicense() {
		return license;
	}

	@Override
	public int compareTo(LibraryInfo o) {
		int result = name.compareToIgnoreCase(o.name);
		if (result != 0) {
			return result;
		}
		return version.compareToIgnoreCase(o.version);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LibraryInfo other = (LibraryInfo)obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}
}
