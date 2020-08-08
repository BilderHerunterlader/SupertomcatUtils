package ch.supertomcat.supertomcatutils.gui.dialog.about;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.layout.SpringUtilities;

/**
 * About Dialog Program Panel
 */
public class AboutDialogProgramPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Program Main Panel
	 */
	protected JPanel pnlProgramMainInfo = new JPanel();

	/**
	 * Program Detail Panel
	 */
	protected JPanel pnlProgramDetailInfo = new JPanel(new SpringLayout());

	/**
	 * System Information Panel
	 */
	protected JPanel pnlSystemInfo = new JPanel(new SpringLayout());

	/**
	 * Java Information Panel
	 */
	protected JPanel pnlJavaInfo = new JPanel(new SpringLayout());

	/**
	 * Constructor
	 */
	public AboutDialogProgramPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		pnlProgramMainInfo.setLayout(new FlowLayout(FlowLayout.LEFT));

		SpringUtilities.makeCompactGrid(pnlProgramDetailInfo, 0, 0, 0, 0, 5, 5);

		pnlSystemInfo.setBorder(BorderFactory.createTitledBorder("System:"));

		pnlJavaInfo.setBorder(BorderFactory.createTitledBorder("Java:"));

		add(pnlProgramMainInfo);
		add(pnlProgramDetailInfo);
		add(pnlSystemInfo);
		add(pnlJavaInfo);

		fillSystemInformation();
		fillJavaInformation();
	}

	/**
	 * Fill Information
	 * 
	 * @param name Name
	 * @param version Version
	 */
	public void fillProgramInformation(String name, String version) {
		JLabel lblNameVersion = new JLabel(name + " (" + version + ")");
		pnlProgramMainInfo.add(lblNameVersion);
	}

	/**
	 * Add Program Folder Information
	 * 
	 * @param title Title
	 * @param folder Folder Path
	 */
	public void addProgramFolderInformation(String title, String folder) {
		addProgramFolderInformation(title, folder, null);
	}

	/**
	 * Add Program Folder Information. Folder will only be added if it is not the same as reference folder.
	 * 
	 * @param title Title
	 * @param folderPath Folder Path
	 * @param referenceFolder Reference Folder or null
	 */
	public void addProgramFolderInformation(String title, String folderPath, File referenceFolder) {
		File folder = new File(folderPath);
		if (referenceFolder != null && folder.equals(referenceFolder)) {
			return;
		}
		addInformation(pnlProgramDetailInfo, title, folder.getAbsolutePath(), InfoActionType.FOLDER);
		SpringUtilities.makeCompactGrid(pnlProgramDetailInfo, pnlProgramDetailInfo.getComponentCount() / 3, 3, 0, 0, 5, 5);
	}

	/**
	 * Add Program Contact Information
	 * 
	 * @param emailAddress E-Mail Address
	 */
	public void addProgramContactInformation(String emailAddress) {
		addInformation(pnlProgramDetailInfo, "E-Mail:", emailAddress, InfoActionType.EMAIL);
		SpringUtilities.makeCompactGrid(pnlProgramDetailInfo, pnlProgramDetailInfo.getComponentCount() / 3, 3, 0, 0, 5, 5);
	}

	/**
	 * Fill System Information
	 */
	protected void fillSystemInformation() {
		addInformation(pnlSystemInfo, "Operating System:", System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
		addInformation(pnlSystemInfo, "Processors:", Integer.toString(Runtime.getRuntime().availableProcessors()));
		SpringUtilities.makeCompactGrid(pnlSystemInfo, 2, 3, 0, 0, 5, 5);
	}

	/**
	 * Fill Java Information
	 */
	protected void fillJavaInformation() {
		addInformation(pnlJavaInfo, "Version:", System.getProperty("java.version"));
		addInformation(pnlJavaInfo, "Vendor:", System.getProperty("java.vendor"));
		addInformation(pnlJavaInfo, "VM Name:", System.getProperty("java.vm.name"));
		addInformation(pnlJavaInfo, "Directory:", System.getProperty("java.home"), InfoActionType.FOLDER);
		SpringUtilities.makeCompactGrid(pnlJavaInfo, 4, 3, 0, 0, 5, 5);
	}

	/**
	 * Add Information
	 * 
	 * @param pnl Panel
	 * @param title Title
	 * @param information Information
	 */
	protected void addInformation(JPanel pnl, String title, String information) {
		addInformation(pnl, title, information, null);
	}

	/**
	 * Add Information
	 * 
	 * @param pnl Panel
	 * @param title Title
	 * @param information Information
	 * @param infoActionType Action Type or null
	 */
	protected void addInformation(JPanel pnl, String title, String information, InfoActionType infoActionType) {
		pnl.add(new JLabel(title));
		JTextField txt = new JTextField(information);
		txt.setEditable(false);
		pnl.add(txt);
		if (infoActionType != null) {
			switch (infoActionType) {
				case FOLDER:
					pnl.add(createDirectoryButton(information));
					break;
				case EMAIL:
					pnl.add(createEMailButton(information));
					break;
				default:
					pnl.add(new JLabel());
					break;
			}
		} else {
			pnl.add(new JLabel());
		}
	}

	/**
	 * Create Directory Button
	 * 
	 * @param folder Folder Path
	 * @return Directory Button
	 */
	protected JButton createDirectoryButton(String folder) {
		JButton btnPath = new JButton(Icons.getTangoIcon("places/folder.png", 16));
		btnPath.addActionListener(e -> openDirectory(folder));
		return btnPath;
	}

	/**
	 * Create E-Mail Button
	 * 
	 * @param emailAddress E-Mail Address
	 * @return E-Mail Button
	 */
	protected JButton createEMailButton(String emailAddress) {
		JButton btnEMail = new JButton(Icons.getTangoIcon("actions/mail-message-new.png", 16));
		btnEMail.addActionListener(e -> openEMail(emailAddress));
		return btnEMail;
	}

	/**
	 * Open Directory
	 * 
	 * @param folder Folder Path
	 */
	protected void openDirectory(String folder) {
		openDirectory(new File(folder));
	}

	/**
	 * Open Directory
	 * 
	 * @param folder Folder
	 */
	protected void openDirectory(File folder) {
		try {
			Desktop.getDesktop().open(folder);
		} catch (IOException e) {
			logger.error("Could not open Directory: {}", folder.getAbsolutePath(), e);
		}
	}

	/**
	 * Open E-Mail
	 * 
	 * @param emailAddress E-Mail Address
	 */
	protected void openEMail(String emailAddress) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.MAIL)) {
				try {
					desktop.mail(new URI("mailto:" + emailAddress));
				} catch (URISyntaxException | IOException e) {
					logger.error("Could not open email: {}", emailAddress, e);
				}
			}
		} else {
			logger.error("Could not open email, because Desktop is not supported: {}", emailAddress);
		}
	}
}
