package ch.supertomcat.supertomcatutils.queue;

/**
 * Restriction Interface
 */
public interface Restriction {
	/**
	 * @return Key for restriction
	 */
	public String getRestrictionKey();

	/**
	 * @return Maximum connection count
	 */
	public int getMaxConnectionCount();

	/**
	 * @return True if is restricted, false otherwise
	 */
	public boolean isRestricted();
}
