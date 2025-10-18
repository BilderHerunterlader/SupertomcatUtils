package ch.supertomcat.supertomcatutils.gui.table.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * TableCellRenderer for alternate coloring.
 * The renderer returns a JLabel. Used {@link DefaultTableCellRenderer} as base instead of JLabel directly, because it contains overridden methods of JLabel for
 * performance improvements.
 */
public class DefaultStringColorRowRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Background Color
	 */
	protected final Color backgroundColor;

	/**
	 * Alternate Background Color
	 */
	protected final Color alternateBackgroundColor;

	/**
	 * Foreground Color
	 */
	protected final Color foregroundColor;

	/**
	 * Alternate Foreground Color
	 */
	protected final Color alternateForegroundColor;

	/**
	 * Constructor
	 */
	public DefaultStringColorRowRenderer() {
		this(Color.WHITE, Color.decode("#F0F8FF"), null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param backgroundColor Background Color or null for default
	 * @param alternateBackgroundColor Alternate Background Color or null for default
	 * @param foregroundColor Foreground Color or null for default
	 * @param alternateForegroundColor Alternate Foreground Color or null for default
	 */
	public DefaultStringColorRowRenderer(Color backgroundColor, Color alternateBackgroundColor, Color foregroundColor, Color alternateForegroundColor) {
		this.backgroundColor = backgroundColor;
		this.alternateBackgroundColor = alternateBackgroundColor;
		this.foregroundColor = foregroundColor;
		this.alternateForegroundColor = alternateForegroundColor;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		prepareForegroundColor(this, table, value, isSelected, hasFocus, row, column);
		prepareBackgroundColor(this, table, value, isSelected, hasFocus, row, column);
		prepareValueText(this, table, value, isSelected, hasFocus, row, column);
		this.setOpaque(true);
		return this;
	}

	/**
	 * Sets the Background-Color of the component.
	 * The rows are getting alternate background-colors.
	 * Which color a cell in a row gets is determent by the index of the row.
	 * If the index is even then the cell will be white if not the cell will have a
	 * different color.
	 * 
	 * @param comp The Component
	 * @param table The Table
	 * @param value The Value
	 * @param isSelected Is the cell selected
	 * @param hasFocus Has the cell the focus
	 * @param row Index of the row
	 * @param column Index of the Column
	 */
	public void prepareBackgroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color c;
		if (isSelected) {
			c = table.getSelectionBackground();
		} else {
			if ((row % 2) != 0) {
				c = alternateBackgroundColor != null ? alternateBackgroundColor : table.getBackground();
			} else {
				c = backgroundColor != null ? backgroundColor : table.getBackground();
			}
		}
		comp.setBackground(c);
	}

	/**
	 * Sets the Foreground-Color of the component
	 * 
	 * @param comp The Component
	 * @param table The Table
	 * @param value The Value
	 * @param isSelected Is the cell selected
	 * @param hasFocus Has the cell the focus
	 * @param row Index of the row
	 * @param column Index of the Column
	 */
	public void prepareForegroundColor(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color c;
		if (isSelected) {
			c = table.getSelectionForeground();
		} else {
			if ((row % 2) != 0) {
				c = alternateForegroundColor != null ? alternateForegroundColor : table.getForeground();
			} else {
				c = foregroundColor != null ? foregroundColor : table.getForeground();
			}
		}
		comp.setForeground(c);
	}

	/**
	 * Sets a text to the JLabel
	 * 
	 * @param label The JLabel
	 * @param table The Table
	 * @param value The Value
	 * @param isSelected Is the cell selected
	 * @param hasFocus Has the cell the focus
	 * @param row Index of the row
	 * @param column Index of the Column
	 */
	public void prepareValueText(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String strValue = String.valueOf(value);
		label.setText(strValue);
		label.setToolTipText(strValue);
		label.setIcon(null);
	}
}
