package ch.supertomcat.supertomcatutils.gui.table.renderer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * TableCellRenderer for alternate coloring.
 * The renderer returns a JLabel.
 */
public class DefaultNumberColorRowRenderer extends DefaultStringColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public void prepareValueText(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Number) {
			super.prepareValueText(label, table, value, isSelected, hasFocus, row, column);
		}
	}
}
