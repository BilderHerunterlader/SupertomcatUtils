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
		return name.compareToIgnoreCase(o.name);
	}
}
