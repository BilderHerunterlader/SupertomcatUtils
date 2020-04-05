package ch.supertomcat.supertomcatutils.gui.table.hider;

/**
 * Class for hiding table columns in JTable
 */
public interface TableColumnHider {
	/**
	 * Hide Column
	 * 
	 * @param identifier Identifier
	 */
	public void hideColumn(Object identifier);

	/**
	 * Show Column
	 * 
	 * @param identifier Identifier
	 */
	public void showColumn(Object identifier);

	/**
	 * Show or hide column
	 * 
	 * @param identifier Identifier
	 * @param visible True if column should be shown, false if column should be hidden
	 */
	public default void setVisible(Object identifier, boolean visible) {
		if (visible) {
			showColumn(identifier);
		} else {
			hideColumn(identifier);
		}
	}
}
