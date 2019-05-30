package ch.supertomcat.supertomcatutils.gui.layout;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Klasse die das Verwenden vom GridBagLayout vereinfacht
 */
public class GridBagLayoutUtil {
	/**
	 * Abstaende
	 */
	private Insets insets;
	
	/**
	 * Konstruktor
	 */
	public GridBagLayoutUtil() {
		this(5, 5, 5, 5);
	}
	
	/**
	 * Konstruktor
	 * @param inseta Inset
	 * @param insetb Inset
	 * @param insetc Inset
	 * @param insetd Inset
	 */
	public GridBagLayoutUtil(int inseta, int insetb, int insetc, int insetd) {
		this.insets = new Insets(inseta, insetb, insetc, insetd);
	}
	
	/**
	 * Constraints erstellen
	 * @param x X-Position
	 * @param y Y-Position
	 * @return Constraints
	 */
	public GridBagConstraints getGBC(int x, int y) {
		return getGBC(x, y, 1, 1);
	}
	
	/**
	 * Constraints erstellen
	 * @param x X-Position
	 * @param y Y-Position
	 * @param w Width
	 * @param h Height
	 * @return Constraints
	 */
	public GridBagConstraints getGBC(int x, int y, int w, int h) {
		return getGBC(x, y, w, h, 0.0d, 0.0d);
	}
	
	/**
	 * Constraints erstellen
	 * @param x X-Position
	 * @param y Y-Position
	 * @param w Width
	 * @param h Height
	 * @param weightx X-Weight
	 * @param weighty Y-Weight
	 * @return Constraints
	 */
	public GridBagConstraints getGBC(int x, int y, int w, int h, double weightx, double weighty) {
		return getGBC(x, y, w, h, weightx, weighty, GridBagConstraints.BOTH);
	}
	
	/**
	 * Constraints erstellen
	 * @param x X-Position
	 * @param y Y-Position
	 * @param w Width
	 * @param h Height
	 * @param weightx X-Weight
	 * @param weighty Y-Weight
	 * @param fill Fill
	 * @return Constraints
	 */
	public GridBagConstraints getGBC(int x, int y, int w, int h, double weightx, double weighty, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = fill;
		gbc.insets = this.insets;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		return gbc;
	}
	
	/**
	 * Fuegt eine Komponente einem Frame hinzu
	 * @param gbl Layout
	 * @param gbc Constraints
	 * @param c Komponente
	 * @param frm Frame
	 */
	public static void addItemToFrame(GridBagLayout gbl, GridBagConstraints gbc, Component c, JFrame frm) {
		gbl.setConstraints(c, gbc);
		frm.getContentPane().add(c);
	}
	
	/**
	 * Fuegt eine Komponente einem Dialog hinzu
	 * @param gbl Layout
	 * @param gbc Constraints
	 * @param c Komponente
	 * @param dlg Dialog
	 */
	public static void addItemToDialog(GridBagLayout gbl, GridBagConstraints gbc, Component c, JDialog dlg) {
		gbl.setConstraints(c, gbc);
		dlg.getContentPane().add(c);
	}
	
	/**
	 * Fuegt eine Komponente einem Panel hinzu
	 * @param gbl Layout
	 * @param gbc Constraints
	 * @param c Komponente
	 * @param pnl Panel
	 */
	public static void addItemToPanel(GridBagLayout gbl, GridBagConstraints gbc, Component c, JPanel pnl) {
		gbl.setConstraints(c, gbc);
		pnl.add(c);
	}
}
