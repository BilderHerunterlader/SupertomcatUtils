package ch.supertomcat.supertomcatutils.gui.copyandpaste;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import ch.supertomcat.supertomcatutils.gui.Icons;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Class which adds a context menu to text components
 */
public final class JTextComponentCopyAndPaste {
	private static final CopyAndPasteMouseAdapter mouseListener = new CopyAndPasteMouseAdapter();

	private static final ImageIcon COPY_ICON = Icons.getTangoSVGIcon("actions/edit-copy.svg", 16);
	private static final ImageIcon PASTE_ICON = Icons.getTangoSVGIcon("actions/edit-paste.svg", 16);
	private static final ImageIcon DELETE_ICON = Icons.getTangoSVGIcon("actions/edit-delete.svg", 16);

	private JTextComponentCopyAndPaste() {
	}

	/**
	 * @param txtComp Text Component
	 */
	public static synchronized void addCopyAndPasteMouseListener(JTextComponent txtComp) {
		MouseListener[] listeners = txtComp.getMouseListeners();
		for (MouseListener listener : listeners) {
			if (listener == mouseListener) {
				// Listener is already added
				return;
			}
		}
		txtComp.addMouseListener(mouseListener);
	}

	/**
	 * @param txtComp Text Component
	 */
	public static synchronized void removeCopyAndPasteMouseListener(JTextComponent txtComp) {
		txtComp.removeMouseListener(mouseListener);
	}

	/**
	 * Mouse Adapter for displaying the context menu
	 */
	private static class CopyAndPasteMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		private void showPopupMenu(MouseEvent e) {
			if (!(e.getSource() instanceof JTextComponent)) {
				return;
			}
			JTextComponent txtField = (JTextComponent)e.getSource();
			if (!txtField.isEnabled()) {
				return;
			}

			boolean editable = txtField.isEditable();

			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem menuItemCopy = new JMenuItem(Localization.getString("Copy"));
			JMenuItem menuItemPaste = new JMenuItem(Localization.getString("Paste"));
			JMenuItem menuItemDelete = new JMenuItem(Localization.getString("Delete"));
			menuItemCopy.setAction(new CopyAction(txtField));
			menuItemPaste.setAction(new PasteAction(txtField));
			menuItemPaste.setEnabled(editable);
			menuItemDelete.setAction(new DeleteAction(txtField));
			menuItemDelete.setEnabled(editable);
			// Set icons after the actions, because otherwise they are not shown for some reason
			menuItemCopy.setIcon(COPY_ICON);
			menuItemPaste.setIcon(PASTE_ICON);
			menuItemDelete.setIcon(DELETE_ICON);
			popupMenu.add(menuItemCopy);
			popupMenu.add(menuItemPaste);
			popupMenu.add(menuItemDelete);

			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
