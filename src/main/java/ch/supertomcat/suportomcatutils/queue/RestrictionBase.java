package ch.supertomcat.suportomcatutils.queue;

/**
 * Base class for Restriction
 */
public abstract class RestrictionBase implements Restriction {
	/**
	 * Restriction Key
	 */
	protected final String restrictionKey;

	/**
	 * Max Connection Count
	 */
	protected int maxConnectionCount;

	/**
	 * Constructor
	 * 
	 * @param restrictionKey Restriction Key
	 * @param maxConnectionCount Max Connection Count
	 */
	public RestrictionBase(String restrictionKey, int maxConnectionCount) {
		this.restrictionKey = restrictionKey;
		this.maxConnectionCount = maxConnectionCount;
	}

	@Override
	public String getRestrictionKey() {
		return restrictionKey;
	}

	@Override
	public int getMaxConnectionCount() {
		return maxConnectionCount;
	}

	/**
	 * Sets the maxConnectionCount
	 * 
	 * @param maxConnectionCount maxConnectionCount
	 */
	protected void setMaxConnectionCount(int maxConnectionCount) {
		this.maxConnectionCount = maxConnectionCount;
	}

	@Override
	public boolean isRestricted() {
		return maxConnectionCount > 0;
	}
}
