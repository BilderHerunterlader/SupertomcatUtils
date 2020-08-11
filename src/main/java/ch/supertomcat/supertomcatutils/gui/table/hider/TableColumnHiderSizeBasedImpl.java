package ch.supertomcat.supertomcatutils.gui.table.hider;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * Implementation of {@link TableColumnHider}, which uses size (min, max) to hide the column
 */
public class TableColumnHiderSizeBasedImpl implements TableColumnHider {
	/**
	 * Restore Data
	 */
	private Map<Object, TableColumnRestoreData> columnsRestoreData = new HashMap<>();

	/**
	 * Table
	 */
	private final JTable table;

	/**
	 * Constructor
	 * 
	 * @param table
	 */
	public TableColumnHiderSizeBasedImpl(JTable table) {
		this.table = table;
	}

	@Override
	public void hideColumn(Object identifier) {
		TableColumn tableColumn = table.getColumn(identifier);

		TableColumnRestoreData tableColumnRestoreData = columnsRestoreData.get(identifier);
		if (tableColumnRestoreData == null) {
			tableColumnRestoreData = new TableColumnRestoreData();
			columnsRestoreData.put(identifier, tableColumnRestoreData);
		}

		if (!tableColumnRestoreData.isHidden()) {
			tableColumnRestoreData.setMinWidth(tableColumn.getMinWidth());
			tableColumnRestoreData.setMaxWidth(tableColumn.getMaxWidth());
			tableColumnRestoreData.setWidth(tableColumn.getWidth());
			tableColumnRestoreData.setPreferredWidth(tableColumn.getPreferredWidth());
		}

		tableColumnRestoreData.setHidden(true);

		// Set Min Width first
		tableColumn.setMinWidth(0);
		// Set Max Width second
		tableColumn.setMaxWidth(0);
		// Now width and preferred width can be set
		tableColumn.setPreferredWidth(0);
		tableColumn.setWidth(0);
	}

	@Override
	public void showColumn(Object identifier) {
		TableColumnRestoreData tableColumnRestoreData = columnsRestoreData.get(identifier);
		if (tableColumnRestoreData == null || !tableColumnRestoreData.isHidden()) {
			return;
		}

		tableColumnRestoreData.setHidden(false);

		TableColumn tableColumn = table.getColumn(identifier);
		// Set Max Width first
		tableColumn.setMaxWidth(tableColumnRestoreData.getMaxWidth());
		// Set Min Width second
		tableColumn.setMinWidth(tableColumnRestoreData.getMinWidth());
		// Now width and preferred width can be set
		tableColumn.setPreferredWidth(tableColumnRestoreData.getPreferredWidth());
		tableColumn.setWidth(tableColumnRestoreData.getWidth());
	}

	@Override
	public boolean isVisible(Object identifier) {
		TableColumnRestoreData tableColumnRestoreData = columnsRestoreData.get(identifier);
		if (tableColumnRestoreData == null) {
			return true;
		}
		return !tableColumnRestoreData.isHidden();
	}

	/**
	 * Class to store width, min with and max with for restoring
	 */
	private static class TableColumnRestoreData {
		/**
		 * Width
		 */
		private int width;

		/**
		 * Minimum Width
		 */
		private int minWidth;

		/**
		 * Maximum Width
		 */
		private int maxWidth;

		/**
		 * PreferredWidth
		 */
		private int preferredWidth;

		/**
		 * Hidden
		 */
		private boolean hidden = false;

		/**
		 * Constructor
		 */
		public TableColumnRestoreData() {
		}

		/**
		 * Returns the width
		 * 
		 * @return width
		 */
		public int getWidth() {
			return width;
		}

		/**
		 * Sets the width
		 * 
		 * @param width width
		 */
		public void setWidth(int width) {
			this.width = width;
		}

		/**
		 * Returns the minWidth
		 * 
		 * @return minWidth
		 */
		public int getMinWidth() {
			return minWidth;
		}

		/**
		 * Sets the minWidth
		 * 
		 * @param minWidth minWidth
		 */
		public void setMinWidth(int minWidth) {
			this.minWidth = minWidth;
		}

		/**
		 * Returns the maxWidth
		 * 
		 * @return maxWidth
		 */
		public int getMaxWidth() {
			return maxWidth;
		}

		/**
		 * Sets the maxWidth
		 * 
		 * @param maxWidth maxWidth
		 */
		public void setMaxWidth(int maxWidth) {
			this.maxWidth = maxWidth;
		}

		/**
		 * Returns the preferredWidth
		 * 
		 * @return preferredWidth
		 */
		public int getPreferredWidth() {
			return preferredWidth;
		}

		/**
		 * Sets the preferredWidth
		 * 
		 * @param preferredWidth preferredWidth
		 */
		public void setPreferredWidth(int preferredWidth) {
			this.preferredWidth = preferredWidth;
		}

		/**
		 * Returns the hidden
		 * 
		 * @return hidden
		 */
		public boolean isHidden() {
			return hidden;
		}

		/**
		 * Sets the hidden
		 * 
		 * @param hidden hidden
		 */
		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}
	}
}
