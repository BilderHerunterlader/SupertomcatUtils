package ch.supertomcat.supertomcatutils.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.libraries.LibraryInfo;
import ch.supertomcat.supertomcatutils.application.libraries.LibraryInfoUtil;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

/**
 * About Dialog
 */
public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger for this Klasse
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Tabs
	 */
	protected JTabbedPane tabPane = new JTabbedPane();

	/**
	 * Label
	 */
	protected JTextArea lblAboutProgram = new JTextArea(25, 100);

	/**
	 * Label
	 */
	protected JTextPane lblAboutLibs = new JTextPane();

	/**
	 * Memory Display Panel
	 */
	protected JPanel pnlMemory = new JPanel();

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
	 * Main Panel
	 */
	protected JPanel pnlMain = new JPanel(new BorderLayout());

	/**
	 * Panel
	 */
	protected JPanel pnlButtons = new JPanel();

	/**
	 * Constructor
	 * 
	 * @param owner Owner
	 * @param title Title
	 * @param icon Icon or null
	 */
	public AboutDialog(Window owner, String title, Image icon) {
		setTitle(title);
		setIconImage(icon);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		configureComponents();

		prepareApplicationInformation();

		prepareLibInformation();

		updateMemory();

		configureMemoryDisplayTimer();

		addComponents();

		pack();
		setLocationRelativeTo(owner);
	}

	/**
	 * Configure Components
	 */
	protected void configureComponents() {
		lblAboutProgram.setEditable(false);
		lblAboutProgram.setFont(UIManager.getFont("Label.font"));

		lblAboutLibs.setEditable(false);
		FontMetrics fontMetrics = lblAboutLibs.getFontMetrics(lblAboutLibs.getFont());
		int fontHeight = fontMetrics.getLeading() + fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
		lblAboutLibs.setPreferredSize(new Dimension(100 * fontMetrics.charWidth('A'), 25 * fontHeight));

		configureTabs();

		pnlMemory.setBorder(BorderFactory.createTitledBorder(Localization.getString("MemoryUsage")));
		pnlMemory.setLayout(new GridLayout(3, 1));
		pnlMemory.add(lblMaxTotalMemory);
		pnlMemory.add(pgMemUsed);
		pnlMemory.add(pgMemTotal);

		pgMemUsed.setStringPainted(true);
		pgMemTotal.setStringPainted(true);

		pnlMain.add(tabPane, BorderLayout.CENTER);
		pnlMain.add(pnlMemory, BorderLayout.SOUTH);
	}

	/**
	 * Configure Tabs
	 */
	protected void configureTabs() {
		tabPane.addTab("Program", new JScrollPane(lblAboutProgram));
		tabPane.addTab("Libraries", new JScrollPane(lblAboutLibs));
	}

	/**
	 * Add Components to Dialog
	 */
	protected void addComponents() {
		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	/**
	 * Configure Memory Display Timer
	 */
	protected void configureMemoryDisplayTimer() {
		timerMemory = new Timer(true);
		timerMemory.scheduleAtFixedRate(new MemoryUpdateTimerTask(), 0, 2000);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (timerMemory != null) {
					timerMemory.cancel();
					timerMemory = null;
				}
			}
		});
	}

	/**
	 * Prepare Application Information
	 */
	protected void prepareApplicationInformation() {
		StringBuilder sbApplicationInfo = new StringBuilder();
		fillApplicationInformation(sbApplicationInfo);
		lblAboutProgram.setText(sbApplicationInfo.toString());
		lblAboutProgram.setCaretPosition(0);
	}

	/**
	 * Fill Application Information
	 * 
	 * @param sbApplicationInfo StringBuilder
	 */
	protected void fillApplicationInformation(StringBuilder sbApplicationInfo) {
		fillApplicationVersionInformation(sbApplicationInfo);
		fillApplicationPathsInformation(sbApplicationInfo);
		sbApplicationInfo.append("\n");
		fillApplicationSystemInformation(sbApplicationInfo);
		sbApplicationInfo.append("\n");
		fillApplicationLicenseInformation(sbApplicationInfo);
		fillApplicationAdditionalInformation(sbApplicationInfo);
	}

	/**
	 * Fill Application Version Information
	 * 
	 * @param sbApplicationInfo StringBuilder
	 */
	protected void fillApplicationVersionInformation(StringBuilder sbApplicationInfo) {
		sbApplicationInfo.append(ApplicationProperties.getProperty("ApplicationName"));
		sbApplicationInfo.append(" (");
		sbApplicationInfo.append(ApplicationProperties.getProperty("ApplicationVersion"));
		sbApplicationInfo.append(")\n");
	}

	/**
	 * Fill Application Paths Information
	 * 
	 * @param sbApplicationInfo StringBuilder
	 */
	protected void fillApplicationPathsInformation(StringBuilder sbApplicationInfo) {
		sbApplicationInfo.append("Program Path: ");
		sbApplicationInfo.append(new File(ApplicationProperties.getProperty("ApplicationPath")).getAbsolutePath());
		File profilePath = new File(ApplicationProperties.getProperty("ProfilePath"));
		sbApplicationInfo.append("\nProfile Path: ");
		sbApplicationInfo.append(profilePath.getAbsolutePath());

		File logsPath = new File(ApplicationProperties.getProperty("LogsPath"));
		if (!logsPath.equals(profilePath)) {
			sbApplicationInfo.append("\nLogs Path: ");
			sbApplicationInfo.append(logsPath.getAbsolutePath());
		}
		sbApplicationInfo.append("\n");
	}

	/**
	 * Fill Application License Information
	 * 
	 * @param sbApplicationInfo StringBuilder
	 */
	protected void fillApplicationSystemInformation(StringBuilder sbApplicationInfo) {
		sbApplicationInfo.append("Operating System:\n");
		sbApplicationInfo.append(System.getProperty("os.name"));
		sbApplicationInfo.append(" ");
		sbApplicationInfo.append(System.getProperty("os.version"));
		sbApplicationInfo.append(" ");
		sbApplicationInfo.append(System.getProperty("os.arch"));
		sbApplicationInfo.append("\nProcessors: ");
		sbApplicationInfo.append(Runtime.getRuntime().availableProcessors());
		sbApplicationInfo.append("\n\nJava:\nVersion: ");
		sbApplicationInfo.append(System.getProperty("java.version"));
		sbApplicationInfo.append("\n");
		sbApplicationInfo.append(System.getProperty("java.vendor"));
		sbApplicationInfo.append("\n");
		sbApplicationInfo.append(System.getProperty("java.vm.name"));
		sbApplicationInfo.append("\n");
		sbApplicationInfo.append(System.getProperty("java.home"));
		sbApplicationInfo.append("\n");
	}

	/**
	 * Fill Application License Information
	 * 
	 * @param sbApplicationInfo StringBuilder
	 */
	protected void fillApplicationLicenseInformation(StringBuilder sbApplicationInfo) {
		String licenseName = ApplicationProperties.getProperty("LicenseName");
		if (licenseName != null && !licenseName.isEmpty()) {
			sbApplicationInfo.append("License:\n");
			sbApplicationInfo.append(licenseName);
			sbApplicationInfo.append("\n");
		}
		String licenseText = ApplicationProperties.getProperty("LicenseText");
		if (licenseText != null && !licenseText.isEmpty()) {
			sbApplicationInfo.append(licenseText);
			sbApplicationInfo.append("\n");
		}
	}

	/**
	 * Fill Application Additional Information
	 * 
	 * @param sbApplicationInfo StringBuilder
	 */
	protected void fillApplicationAdditionalInformation(StringBuilder sbApplicationInfo) {
		String additionalAboutText = ApplicationProperties.getProperty("AdditionalAboutText");
		if (additionalAboutText != null && !additionalAboutText.isEmpty()) {
			sbApplicationInfo.append("\n" + additionalAboutText + "\n");
		}
	}

	/**
	 * Prepare Lib Information
	 */
	protected void prepareLibInformation() {
		StringBuilder sbLibInformation = new StringBuilder();
		List<Point> boldPositions = new ArrayList<>();
		fillLibInformation(sbLibInformation, boldPositions);
		lblAboutLibs.setText(sbLibInformation.toString());
		for (Point p : boldPositions) {
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setBold(sas, true);
			lblAboutLibs.getStyledDocument().setCharacterAttributes(p.x, p.y, sas, false);
		}
		lblAboutLibs.setCaretPosition(0);
	}

	/**
	 * Fill Library Information
	 * 
	 * @param sbLibInformation StringBuilder
	 * @param boldPositions Bold Positions
	 */
	protected void fillLibInformation(StringBuilder sbLibInformation, List<Point> boldPositions) {
		for (LibraryInfo libInfo : LibraryInfoUtil.getLibraries()) {
			int startPos = sbLibInformation.length();
			int length = libInfo.getName().length();
			boldPositions.add(new Point(startPos, length));
			sbLibInformation.append(libInfo.getName());
			if (!libInfo.getVersion().isEmpty()) {
				sbLibInformation.append("\nVersion: ");
				sbLibInformation.append(libInfo.getVersion());
			}
			if (!libInfo.getLicense().isEmpty()) {
				sbLibInformation.append("\nLicense: ");
				sbLibInformation.append(libInfo.getLicense());
			}
			sbLibInformation.append("\n\n");
		}
	}

	/**
	 * Update memory display
	 */
	protected void updateMemory() {
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
			AboutDialog.this.updateMemory();
		}
	}
}
