package ch.supertomcat.supertomcatutils.gui.table.renderer;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Filename Color Row Renderer
 */
public class FilenameColorRowRenderer extends DefaultStringColorRowRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public FilenameColorRowRenderer() {
	}

	@Override
	public void prepareValueText(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Path) {
			super.prepareValueText(label, table, ((Path)value).getFileName(), isSelected, hasFocus, row, column);
		} else if (value instanceof File) {
			super.prepareValueText(label, table, ((File)value).getName(), isSelected, hasFocus, row, column);
		} else {
			super.prepareValueText(label, table, value, isSelected, hasFocus, row, column);
		}
	}
}
