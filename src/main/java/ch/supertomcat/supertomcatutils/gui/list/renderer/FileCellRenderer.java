package ch.supertomcat.supertomcatutils.gui.list.renderer;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

/**
 * Renderer for File
 */
public class FileCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Full Path Display
	 */
	private boolean fullPathDisplay = false;

	/**
	 * Constructor
	 * 
	 * @param fullPathDisplay Full Path Display
	 */
	public FileCellRenderer(boolean fullPathDisplay) {
		this.fullPathDisplay = fullPathDisplay;
		setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof Path path) {
			if (fullPathDisplay) {
				setText(path.toAbsolutePath().toString());
			} else {
				setText(path.getFileName().toString());
			}
			setIcon(FileSystemView.getFileSystemView().getSystemIcon(path.toFile()));
		} else if (value instanceof File file) {
			if (fullPathDisplay) {
				setText(file.getAbsolutePath());
			} else {
				setText(file.getName());
			}
			setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
		}
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		return this;
	}
}
