package ch.supertomcat.supertomcatutils.gui.progress;

import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * Window which displays a progress bar
 */
public class ProgressWindow extends JFrame implements IProgressObserver {
	private static final long serialVersionUID = 1L;

	private JProgressBar pg = new JProgressBar();

	/**
	 * Constructor
	 * 
	 * @param title Window Title
	 * @param parent Parent Window or null
	 */
	public ProgressWindow(String title, Window parent) {
		super(title);
		pg.setStringPainted(true);
		pg.setVisible(true);
		this.add(pg);

		pack();
		setLocationRelativeTo(parent);

		this.setVisible(true);
	}

	@Override
	public void progressIncreased() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				pg.setValue(pg.getValue() + 1);
			}
		};
		if (EventQueue.isDispatchThread()) {
			task.run();
		} else {
			EventQueue.invokeLater(task);
		}
	}

	@Override
	public void progressChanged(final int val) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				pg.setValue(val);
			}
		};
		if (EventQueue.isDispatchThread()) {
			task.run();
		} else {
			EventQueue.invokeLater(task);
		}
	}

	@Override
	public void progressChanged(final int min, final int max, final int val) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				pg.setMinimum(min);
				pg.setMaximum(max);
				pg.setValue(val);
			}
		};
		if (EventQueue.isDispatchThread()) {
			task.run();
		} else {
			EventQueue.invokeLater(task);
		}
	}

	@Override
	public void progressChanged(final String text) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				pg.setString(text);
			}
		};
		if (EventQueue.isDispatchThread()) {
			task.run();
		} else {
			EventQueue.invokeLater(task);
		}
	}

	@Override
	public void progressChanged(final boolean visible) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				pg.setVisible(visible);
			}
		};
		if (EventQueue.isDispatchThread()) {
			task.run();
		} else {
			EventQueue.invokeLater(task);
		}
	}

	@Override
	public void progressModeChanged(final boolean indeterminate) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				pg.setIndeterminate(indeterminate);
			}
		};
		if (EventQueue.isDispatchThread()) {
			task.run();
		} else {
			EventQueue.invokeLater(task);
		}
	}

	@Override
	public void progressCompleted() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				ProgressWindow.this.dispose();
			}
		};
		if (EventQueue.isDispatchThread()) {
			task.run();
		} else {
			EventQueue.invokeLater(task);
		}
	}
}
