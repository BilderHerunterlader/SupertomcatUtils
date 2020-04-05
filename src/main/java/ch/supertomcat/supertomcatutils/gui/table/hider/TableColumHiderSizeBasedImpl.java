package ch.supertomcat.supertomcatutils.gui.table.hider;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * Implementation of {@link TableColumnHider}, which uses size (min, max) to hide the column
 */
public class TableColumHiderSizeBasedImpl implements TableColumnHider {
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
	public TableColumHiderSizeBasedImpl(JTable table) {
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
		}

		tableColumnRestoreData.setHidden(true);

		tableColumn.setMinWidth(0);
		tableColumn.setMaxWidth(0);
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
		tableColumn.setMinWidth(tableColumnRestoreData.getMinWidth());
		tableColumn.setMaxWidth(tableColumnRestoreData.getMaxWidth());
		tableColumn.setWidth(tableColumnRestoreData.getWidth());
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
