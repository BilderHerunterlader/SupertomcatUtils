package ch.supertomcat.supertomcatutils.gui.dialog.about;

import java.awt.GridLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

/**
 * About Dialog Libraries Panel
 */
public class AboutDialogMemoryPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Label
	 */
	protected JLabel lblMaxTotalMemory = new JLabel("");

	/**
	 * ProgressBar
	 */
	protected JProgressBar pgMemUsed = new JProgressBar();

	/**
	 * ProgressBar
	 */
	protected JProgressBar pgMemTotal = new JProgressBar();

	/**
	 * Timer
	 */
	protected Timer timerMemory = null;

	/**
	 * Constructor
	 */
	public AboutDialogMemoryPanel() {
		setBorder(BorderFactory.createTitledBorder(Localization.getString("MemoryUsage")));
		setLayout(new GridLayout(3, 1));
		add(lblMaxTotalMemory);
		add(pgMemUsed);
		add(pgMemTotal);

		pgMemUsed.setStringPainted(true);
		pgMemTotal.setStringPainted(true);

		configureMemoryDisplayTimer();

		updateMemory();
	}

	/**
	 * Configure Memory Display Timer
	 */
	protected void configureMemoryDisplayTimer() {
		timerMemory = new Timer(true);
		timerMemory.scheduleAtFixedRate(new MemoryUpdateTimerTask(), 0, 2000);
	}

	/**
	 * Cancel Memory Timer
	 */
	public void cancelUpdateTimer() {
		if (timerMemory != null) {
			timerMemory.cancel();
			timerMemory = null;
		}
	}

	/**
	 * Update memory display
	 */
	protected synchronized void updateMemory() {
		long max = Runtime.getRuntime().maxMemory();
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - free;

		int mode = UnitFormatUtil.AUTO_CHANGE_SIZE;

		lblMaxTotalMemory.setText(Localization.getString("MaximumAvailableMemory") + ": " + UnitFormatUtil.getSizeString(max, mode));

		int val = (int)((used * 100.0d) / max);
		pgMemUsed.setMinimum(0);
		pgMemUsed.setMaximum(100);
		pgMemUsed.setValue(val);
		pgMemUsed.setString(Localization.getString("Used") + ": " + UnitFormatUtil.getSizeString(used, mode) + " / " + UnitFormatUtil.getSizeString(max, mode) + " (" + val + " %)");

		val = (int)((total * 100.0d) / max);
		pgMemTotal.setMinimum(0);
		pgMemTotal.setMaximum(100);
		pgMemTotal.setValue(val);
		pgMemTotal.setString(Localization.getString("CurrentlyAllocated") + ": " + UnitFormatUtil.getSizeString(total, mode) + " / " + UnitFormatUtil.getSizeString(max, mode) + " (" + val + " %)");
	}

	/**
	 * Timer Task for updating memory display
	 */
	protected class MemoryUpdateTimerTask extends TimerTask {
		@Override
		public void run() {
			updateMemory();
		}
	}
}
