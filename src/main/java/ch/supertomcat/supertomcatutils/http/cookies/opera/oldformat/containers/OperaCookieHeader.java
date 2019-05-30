package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers;

/**
 * OperaCookieHeader
 */
public class OperaCookieHeader {
	/**
	 * Anzahl Bytes benoetigt fuer alle Member
	 */
	public static final int HEADER_BYTE_LENGTH = 4 + 4 + 2 + 2;

	/**
	 * 4 Byte
	 */
	private long fileVersion = 0;

	/**
	 * 4 Byte
	 */
	private long appVersion = 0;

	/**
	 * 2 Byte
	 */
	private int tagSize = 0;

	/**
	 * 2 Byte
	 */
	private int recordSize = 0;

	/**
	 * @return the fileVersion
	 */
	public long getFileVersion() {
		return fileVersion;
	}

	/**
	 * @param fileVersion the fileVersion to set
	 */
	public void setFileVersion(long fileVersion) {
		this.fileVersion = fileVersion;
	}

	/**
	 * @return the appVersion
	 */
	public long getAppVersion() {
		return appVersion;
	}

	/**
	 * @param appVersion the appVersion to set
	 */
	public void setAppVersion(long appVersion) {
		this.appVersion = appVersion;
	}

	/**
	 * @return the tagSize
	 */
	public int getTagSize() {
		return tagSize;
	}

	/**
	 * @param tagSize the tagSize to set
	 */
	public void setTagSize(int tagSize) {
		this.tagSize = tagSize;
	}

	/**
	 * @return the recordSize
	 */
	public int getRecordSize() {
		return recordSize;
	}

	/**
	 * @param recordSize the recordSize to set
	 */
	public void setRecordSize(int recordSize) {
		this.recordSize = recordSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)(appVersion ^ (appVersion >>> 32));
		result = prime * result + (int)(fileVersion ^ (fileVersion >>> 32));
		result = prime * result + recordSize;
		result = prime * result + tagSize;
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
		OperaCookieHeader other = (OperaCookieHeader)obj;
		if (appVersion != other.appVersion) {
			return false;
		}
		if (fileVersion != other.fileVersion) {
			return false;
		}
		if (recordSize != other.recordSize) {
			return false;
		}
		if (tagSize != other.tagSize) {
			return false;
		}
		return true;
	}
}
