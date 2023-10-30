package ch.supertomcat.supertomcatutils.gui.combobox.renderer;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * Renderer for Filename ComboBox
 */
public class FilenameComboBoxRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof Path) {
			setText(((Path)value).getFileName().toString());
		} else if (value instanceof File) {
			setText(((File)value).getName());
		}
		return comp;
	}
}
