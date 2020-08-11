package ch.supertomcat.supertomcatutils.gui.table.hider;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Class which adds a column selector to the table header
 */
public class TableHeaderColumnSelector {
	/**
	 * Table Column Hider
	 */
	protected final TableColumnHider tableColumnHider;

	/**
	 * Table
	 */
	protected final JTable table;

	/**
	 * PopupMenuTableHeader
	 */
	private JPopupMenu popupMenuTableHeader = new JPopupMenu();

	/**
	 * Menu Items
	 */
	private final Map<String, JMenuItem> menuItems = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param tableColumnHider Table Column Hider
	 * @param table Table
	 */
	public TableHeaderColumnSelector(TableColumnHider tableColumnHider, JTable table) {
		this.tableColumnHider = tableColumnHider;
		this.table = table;

		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showPopupMenu(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}
		});
	}

	/**
	 * Show Popup Menu
	 * 
	 * @param e Mouse Event
	 */
	protected void showPopupMenu(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popupMenuTableHeader.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * Add Column
	 * 
	 * @param identifier Identifier
	 */
	public void addColumn(Object identifier) {
		boolean visible = tableColumnHider.isVisible(identifier);
		JMenuItem menuItem = new JCheckBoxMenuItem(getLocalizedColumnName(identifier), visible);
		menuItem.addActionListener(e -> tableColumnHider.setVisible(identifier, menuItem.isSelected()));
		popupMenuTableHeader.add(menuItem);
		menuItems.put(String.valueOf(identifier), menuItem);
	}

	/**
	 * Returns the localized column name
	 * 
	 * @param identifier Identifier
	 * @return Localized column name
	 */
	public String getLocalizedColumnName(Object identifier) {
		return Localization.getString(String.valueOf(identifier));
	}

	/**
	 * Remove Column
	 * 
	 * @param identifier Identifier
	 */
	public void removeColumn(Object identifier) {
		JMenuItem menuItem = menuItems.remove(String.valueOf(identifier));
		if (menuItem != null) {
			popupMenuTableHeader.remove(menuItem);
		}
	}
}
