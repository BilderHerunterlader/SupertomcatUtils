package ch.supertomcat.supertomcatutils.gui.progress;

/**
 * Listener for ProgressObserver
 */
public interface IProgressObserver {
	/**
	 * Update the progressbar by adding 1 to the value
	 */
	public void progressIncreased();

	/**
	 * Update the progressbar with a new value
	 * 
	 * @param val Value
	 */
	public void progressChanged(int val);

	/**
	 * Update the progressbar with new min and max values
	 * 
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 */
	public void progressChanged(int min, int max, int val);

	/**
	 * Update the text of the progressbar
	 * 
	 * @param text Progressbar-Text
	 */
	public void progressChanged(String text);

	/**
	 * Update the visbility of the progressbar
	 * 
	 * @param visible Visibility
	 */
	public void progressChanged(boolean visible);

	/**
	 * Sets the progressbar to indeterminate mode or not
	 * 
	 * @param indeterminate Indeterminate
	 */
	public void progressModeChanged(boolean indeterminate);

	/**
	 * Indicates completion of the progress and provides the result
	 */
	public void progressCompleted();
}
