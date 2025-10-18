package ch.supertomcat.supertomcatutils.gui.table.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.util.Objects;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * TableCellRenderer for alternate coloring.
 * Some Tables in BH are using a extended class of this class.
 * The renderer returns a JProgressBar.
 */
public class DefaultProgressBarColorRowRenderer extends JProgressBar implements TableCellRenderer {
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
	public DefaultProgressBarColorRowRenderer() {
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
	public DefaultProgressBarColorRowRenderer(Color backgroundColor, Color alternateBackgroundColor, Color foregroundColor, Color alternateForegroundColor) {
		this.backgroundColor = backgroundColor;
		this.alternateBackgroundColor = alternateBackgroundColor;
		this.foregroundColor = foregroundColor;
		this.alternateForegroundColor = alternateForegroundColor;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		prepareForegroundColor(this, table, value, isSelected, hasFocus, row, column);
		prepareBackgroundColor(this, table, value, isSelected, hasFocus, row, column);
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
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public boolean isOpaque() {
		Color back = getBackground();
		Component p = getParent();
		if (p != null) {
			p = p.getParent();
		}

		// p should now be the JTable.
		boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
		return !colorMatch && super.isOpaque();
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public void invalidate() {
		// Nothing to do
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public void validate() {
		// Nothing to do
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public void revalidate() {
		// Nothing to do
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		// Nothing to do
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public void repaint(Rectangle r) {
		// Nothing to do
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public void repaint() {
		// Nothing to do
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		switch (propertyName) {
			case "orientation", "stringPainted", "string", "borderPainted", "indeterminate":
				super.firePropertyChange(propertyName, oldValue, newValue);
				break;
			case "font", "foreground":
				if (oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null) {
					super.firePropertyChange(propertyName, oldValue, newValue);
				}
				break;
			case String str when isScaleChanged(str, oldValue, newValue):
				if (getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null) {
					super.firePropertyChange(propertyName, oldValue, newValue);
				}
				break;
			default:
				// Nothing to do
				break;
		}
	}

	/**
	 * Overridden for performance reasons. See documentation of {@link DefaultTableCellRenderer} for explanation
	 */
	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		// Nothing to do
	}

	/**
	 * Check if scale used by GraphicsConfiguration was changed
	 * 
	 * @param name Property Name
	 * @param oldValue Old Value
	 * @param newValue New Value
	 * @return True if scale was changed, false otherwise
	 */
	private static boolean isScaleChanged(final String name, final Object oldValue, final Object newValue) {
		if (oldValue == newValue || !"graphicsConfiguration".equals(name)) {
			return false;
		}
		var newGC = (GraphicsConfiguration)oldValue;
		var oldGC = (GraphicsConfiguration)newValue;
		var newTx = newGC != null ? newGC.getDefaultTransform() : null;
		var oldTx = oldGC != null ? oldGC.getDefaultTransform() : null;
		return !Objects.equals(newTx, oldTx);
	}
}
