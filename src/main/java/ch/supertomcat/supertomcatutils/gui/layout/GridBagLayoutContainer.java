package ch.supertomcat.supertomcatutils.gui.layout;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Klasse die das Verwenden vom GridBagLayout vereinfacht
 */
public class GridBagLayoutContainer extends GridBagLayout {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Abstaende
	 */
	private Insets insets;
	
	/**
	 * Kompenten welcher der LayoutManager zugewiesen wird
	 */
	private Component owner;
	
	/**
	 * Konstruktor
	 * @param owner Owner
	 */
	public GridBagLayoutContainer(Component owner) {
		this(5, 5, 5, 5, owner);
	}
	
	/**
	 * Konstruktor
	 * @param inseta Inset
	 * @param insetb Inset
	 * @param insetc Inset
	 * @param insetd Inset
	 * @param owner Owner
	 */
	public GridBagLayoutContainer(int inseta, int insetb, int insetc, int insetd, Component owner) {
		this.insets = new Insets(inseta, insetb, insetc, insetd);
		this.owner = owner;
		if (this.owner instanceof JFrame) {
			((JFrame)this.owner).getContentPane().setLayout(this);
		} else if (this.owner instanceof JDialog) {
			((JDialog)this.owner).getContentPane().setLayout(this);
		} else if (this.owner instanceof JComponent) {
			((JComponent)this.owner).setLayout(this);
		}
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
	 * @param c Component
	 * @param x X-Position
	 * @param y Y-Position
	 */
	public void add(Component c, int x, int y) {
		if (c == null) return;
		this.add(c, getGBC(x, y));
	}
	
	/**
	 * @param c Component
	 * @param x X-Position
	 * @param y Y-Position
	 * @param w Width
	 * @param h Height
	 */
	public void add(Component c, int x, int y, int w, int h) {
		if (c == null) return;
		this.add(c, getGBC(x, y, w, h));
	}
	
	/**
	 * @param c Component
	 * @param x X-Position
	 * @param y Y-Position
	 * @param w Width
	 * @param h Height
	 * @param weightx X-Weight
	 * @param weighty Y-Weight
	 */
	public void add(Component c, int x, int y, int w, int h, double weightx, double weighty) {
		if (c == null) return;
		this.add(c, getGBC(x, y, w, h, weightx, weighty));
	}
	
	/**
	 * @param c Component
	 * @param gbc Constraints
	 */
	public void add(Component c, GridBagConstraints gbc) {
		if (c == null) return;
		if (gbc == null) return;
		if (this.owner == null) return;
		this.setConstraints(c, gbc);
		if (this.owner instanceof JFrame) {
			((JFrame)this.owner).getContentPane().add(c);
		} else if (this.owner instanceof JDialog) {
			((JDialog)this.owner).getContentPane().add(c);
		} else if (this.owner instanceof JComponent) {
			((JComponent)this.owner).add(c);
		}
	}
}
