package ch.supertomcat.supertomcatutils.gui.dialog.about;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.FileExplorerUtil;

/**
 * About Dialog
 */
public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Tabs
	 */
	protected JTabbedPane tabPane = new JTabbedPane();

	/**
	 * Program Panel
	 */
	protected AboutDialogProgramPanel pnlProgram = new AboutDialogProgramPanel();

	/**
	 * License Panel
	 */
	protected AboutDialogLicensePanel pnlLicense = new AboutDialogLicensePanel();

	/**
	 * Libraries
	 */
	protected AboutDialogLibrariesPanel pnlLibraries = new AboutDialogLibrariesPanel();

	/**
	 * Memory Display Panel
	 */
	protected AboutDialogMemoryPanel pnlMemory = new AboutDialogMemoryPanel();

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
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		configureComponents();

		fillApplicationInformation();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				pnlMemory.cancelUpdateTimer();
			}
		});

		addComponents();

		pack();
		setLocationRelativeTo(owner);
	}

	/**
	 * Configure Components
	 */
	protected void configureComponents() {
		configureTabs();

		pnlMain.add(tabPane, BorderLayout.CENTER);
		pnlMain.add(pnlMemory, BorderLayout.SOUTH);
	}

	/**
	 * Configure Tabs
	 */
	protected void configureTabs() {
		tabPane.addTab("Program", pnlProgram);
		tabPane.addTab("License", pnlLicense);
		tabPane.addTab("Libraries", pnlLibraries);
	}

	/**
	 * Add Components to Dialog
	 */
	protected void addComponents() {
		add(pnlMain, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	/**
	 * Fill Application Information
	 */
	protected void fillApplicationInformation() {
		fillProgramInformation();
		fillApplicationPathsInformation();
		fillApplicationLicenseInformation();
	}

	/**
	 * Fill Program Information
	 */
	protected void fillProgramInformation() {
		pnlProgram.fillProgramInformation(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_NAME), ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION));
	}

	/**
	 * Fill Application Paths Information
	 */
	protected void fillApplicationPathsInformation() {
		pnlProgram.addProgramFolderInformation("Program Path:", ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH));
		pnlProgram.addProgramFolderInformation("Profile Path:", ApplicationProperties.getProperty(ApplicationMain.PROFILE_PATH));
		Path profilePath = Paths.get(ApplicationProperties.getProperty(ApplicationMain.PROFILE_PATH));
		pnlProgram.addProgramFolderInformation("Logs Path:", ApplicationProperties.getProperty(ApplicationMain.LOGS_PATH), profilePath);
	}

	/**
	 * Fill Application License Information
	 */
	protected void fillApplicationLicenseInformation() {
		String licenseName = ApplicationProperties.getProperty(ApplicationMain.LICENSE_NAME);
		String licenseText = ApplicationProperties.getProperty(ApplicationMain.LICENSE_TEXT);
		if (licenseName != null && licenseText != null) {
			pnlLicense.fillLicenseInformation(licenseName, licenseText);
		}
	}

	/**
	 * Open URL
	 * 
	 * @param url URL
	 */
	protected void openURL(String url) {
		FileExplorerUtil.openURL(url);
	}
}
