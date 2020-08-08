package ch.supertomcat.supertomcatutils.gui.dialog.about;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * About Dialog License Panel
 */
public class AboutDialogLicensePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * License Name Text Field
	 */
	protected JTextField txtLicenseName = new JTextField(100);

	/**
	 * License Text Text Area
	 */
	protected JTextArea txtLicenseText = new JTextArea(10, 100);

	/**
	 * License Additional Text Text Area
	 */
	protected JTextArea txtLicenseAdditionalText = new JTextArea(10, 100);

	/**
	 * Constructor
	 */
	public AboutDialogLicensePanel() {
		setLayout(new BorderLayout());

		txtLicenseName.setEditable(false);
		txtLicenseText.setEditable(false);
		txtLicenseText.setFont(UIManager.getFont("Label.font"));
		txtLicenseText.setLineWrap(true);
		txtLicenseAdditionalText.setEditable(false);
		txtLicenseAdditionalText.setFont(UIManager.getFont("Label.font"));
		txtLicenseAdditionalText.setLineWrap(true);

		add(txtLicenseName, BorderLayout.NORTH);
		add(new JScrollPane(txtLicenseText), BorderLayout.CENTER);
		add(new JScrollPane(txtLicenseAdditionalText), BorderLayout.SOUTH);
	}

	/**
	 * Fill License Information
	 * 
	 * @param licenseName License Name
	 * @param licenseText License Text
	 */
	public void fillLicenseInformation(String licenseName, String licenseText) {
		txtLicenseName.setText(licenseName);
		txtLicenseText.setText(licenseText);
	}

	/**
	 * Fill License Additional Information
	 * 
	 * @param additionalText Additional Text
	 */
	public void fillLicenseAdditionalInformation(String additionalText) {
		txtLicenseAdditionalText.setText(additionalText);
	}
}
