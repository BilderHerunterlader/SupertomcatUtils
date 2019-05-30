package ch.supertomcat.supertomcatutils.gui.progress;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ProgressObserver which can be used always when an object must be informed, that a progress has changed
 */
public class ProgressObserver {
	/**
	 * Listeners
	 */
	private List<IProgressObserver> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Update the progressbar by adding 1 to the value
	 */
	public void progressIncreased() {
		for (IProgressObserver listener : listeners) {
			listener.progressIncreased();
		}
	}

	/**
	 * Update the progressbar with a new value
	 * 
	 * @param val Value
	 */
	public void progressChanged(int val) {
		for (IProgressObserver listener : listeners) {
			listener.progressChanged(val);
		}
	}

	/**
	 * Update the progressbar with new min and max values
	 * 
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 */
	public void progressChanged(int min, int max, int val) {
		for (IProgressObserver listener : listeners) {
			listener.progressChanged(min, max, val);
		}
	}

	/**
	 * Update the text of the progressbar
	 * 
	 * @param text Progressbar-Text
	 */
	public void progressChanged(String text) {
		for (IProgressObserver listener : listeners) {
			listener.progressChanged(text);
		}
	}

	/**
	 * Update the visbility of the progressbar
	 * 
	 * @param visible Visibility
	 */
	public void progressChanged(boolean visible) {
		for (IProgressObserver listener : listeners) {
			listener.progressChanged(visible);
		}
	}

	/**
	 * Sets the progressbar to indeterminate mode or not
	 * 
	 * @param indeterminate Indeterminate
	 */
	public void progressModeChanged(boolean indeterminate) {
		for (IProgressObserver listener : listeners) {
			listener.progressModeChanged(indeterminate);
		}
	}

	/**
	 * Indicates completion of the progress and provides the result
	 */
	public void progressCompleted() {
		for (IProgressObserver listener : listeners) {
			listener.progressCompleted();
		}
	}

	/**
	 * Add listener
	 * 
	 * @param l Listener
	 */
	public void addProgressListener(IProgressObserver l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * Remove listener
	 * 
	 * @param l Listener
	 */
	public void removeProgressListener(IProgressObserver l) {
		listeners.remove(l);
	}
}
