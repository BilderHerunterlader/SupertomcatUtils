package ch.supertomcat.supertomcatutils.gui.dialog.about;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ch.supertomcat.supertomcatutils.application.libraries.LibraryInfo;
import ch.supertomcat.supertomcatutils.application.libraries.LibraryInfoUtil;

/**
 * About Dialog Libraries Panel
 */
public class AboutDialogLibrariesPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Libraries TextPane
	 */
	protected JTextPane txtAboutLibs = new JTextPane();

	/**
	 * Constructor
	 */
	public AboutDialogLibrariesPanel() {
		setLayout(new BorderLayout());

		txtAboutLibs.setEditable(false);
		FontMetrics fontMetrics = txtAboutLibs.getFontMetrics(txtAboutLibs.getFont());
		int fontHeight = fontMetrics.getLeading() + fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
		txtAboutLibs.setPreferredSize(new Dimension(100 * fontMetrics.charWidth('A'), 25 * fontHeight));

		add(new JScrollPane(txtAboutLibs), BorderLayout.CENTER);

		prepareLibInformation();
	}

	/**
	 * Prepare Lib Information
	 */
	protected void prepareLibInformation() {
		StringBuilder sbLibInformation = new StringBuilder();
		List<Point> boldPositions = new ArrayList<>();
		fillLibInformation(sbLibInformation, boldPositions);
		txtAboutLibs.setText(sbLibInformation.toString());
		for (Point p : boldPositions) {
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setBold(sas, true);
			txtAboutLibs.getStyledDocument().setCharacterAttributes(p.x, p.y, sas, false);
		}
		txtAboutLibs.setCaretPosition(0);
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
}
