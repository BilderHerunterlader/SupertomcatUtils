package ch.supertomcat.supertomcatutils.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Utility class for tables
 */
public final class TableUtil {
	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory.getLogger(TableUtil.class);

	/**
	 * Constructor
	 */
	private TableUtil() {
	}

	/**
	 * Parses a colwidth setting and returns an array of integers.
	 * If the parsing fails completly null is returned.
	 * If only some values could not be parsed, then they are set
	 * to 0.
	 * Format ColumnName=###|ColumnName=###|...
	 * 
	 * @param setting ColWidth-Setting
	 * @return Map with column names as keys and int values
	 */
	public static Map<String, Integer> parseColWidthsSetting(String setting) {
		Map<String, Integer> map = new HashMap<>();

		String[] columns = setting.split("\\|");
		if (columns == null || columns.length == 0) {
			return null;
		}

		for (String col : columns) {
			String[] parts = col.split("=");
			if (parts == null || parts.length != 2) {
				logger.error("Could not parse column setting '{}' in setting String: {}", col, setting);
				continue;
			}

			String columnName = parts[0];
			String columnWidth = parts[1];
			try {
				int width = Integer.parseInt(columnWidth);
				map.put(columnName, width);
			} catch (NumberFormatException nfe) {
				logger.error("Could not parse column width '{}' for column name '{}' in setting String: {}", columnWidth, columnName, setting, nfe);
			}
		}

		return map;
	}

	/**
	 * 
	 * @param map Map
	 * @return Serialized column widths
	 */
	public static String serializeColWidthSetting(Map<String, Integer> map) {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("|");
			}
			sb.append(entry.getKey() + "=" + entry.getValue());
		}

		return sb.toString();
	}

	/**
	 * @param table Table
	 * @return Serialized column widths
	 */
	public static String serializeColWidthSetting(JTable table) {
		StringBuilder sb = new StringBuilder();

		Enumeration<TableColumn> en = table.getColumnModel().getColumns();
		TableColumn col;
		while (en.hasMoreElements()) {
			col = en.nextElement();
			if (sb.length() > 0) {
				sb.append("|");
			}
			sb.append(col.getIdentifier() + "=" + col.getWidth());
		}

		return sb.toString();
	}

	/**
	 * Sets column widths on a jtable
	 * Format ColumnName=###|ColumnName=###|...
	 * 
	 * @param table Table
	 * @param setting Setting
	 */
	public static void applyColWidths(JTable table, String setting) {
		applyColWidths(table, parseColWidthsSetting(setting));
	}

	/**
	 * Sets column widths on a jtable
	 * 
	 * @param table Table
	 * @param map Map
	 */
	public static void applyColWidths(JTable table, Map<String, Integer> map) {
		TableColumn col;
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			String columnName = entry.getKey();
			try {
				col = table.getColumn(columnName);
				col.setPreferredWidth(entry.getValue());
				col.setWidth(entry.getValue());
			} catch (IllegalArgumentException e) {
				logger.error("Could not apply column widths for column: {}", columnName, e);
			}
		}
	}

	/**
	 * Changes headervalues of the columns to internationalized version
	 * 
	 * @param table Table
	 */
	public static void internationalizeColumns(JTable table) {
		Enumeration<TableColumn> en = table.getColumnModel().getColumns();
		while (en.hasMoreElements()) {
			TableColumn col = en.nextElement();
			col.setIdentifier(col.getIdentifier());
			col.setHeaderValue(Localization.getString(col.getIdentifier().toString()));
		}
	}

	/**
	 * @param table Table
	 * @param setting Setting
	 */
	public static void applyTableSortOrder(JTable table, String setting) {
		// We can use the parseColWidthsSetting-Method, because the sortOrders are saved in the same form
		applyTableSortOrder(table, parseColWidthsSetting(setting));
	}

	/**
	 * @param table Table
	 * @param map Map
	 */
	public static void applyTableSortOrder(JTable table, Map<String, Integer> map) {
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			String columnName = entry.getKey();
			int sortOrder = entry.getValue();
			if (sortOrder < 0 || sortOrder > 2) {
				logger.error("Could not apply sort order for column '{}', because value is invalid: {}", columnName, sortOrder);
				continue;
			}

			int columnIndex = table.getColumnModel().getColumnIndex(columnName);

			if (sortOrder == 1) {
				sortKeys.add(new RowSorter.SortKey(columnIndex, SortOrder.ASCENDING));
			} else if (sortOrder == 2) {
				sortKeys.add(new RowSorter.SortKey(columnIndex, SortOrder.DESCENDING));
			}
		}

		if (!sortKeys.isEmpty()) {
			RowSorter<? extends TableModel> sorter = table.getRowSorter();
			sorter.setSortKeys(sortKeys);
			if (sorter instanceof DefaultRowSorter) {
				((DefaultRowSorter<?, ?>)sorter).sort();
			}
		}
	}

	/**
	 * @param table Table
	 * @return Serialized SortOrders
	 */
	public static String serializeTableSortOrderSetting(JTable table) {
		StringBuilder sb = new StringBuilder();

		List<? extends SortKey> sortKeys = table.getRowSorter().getSortKeys();
		for (SortKey sortKey : sortKeys) {
			int columnIndex = sortKey.getColumn();
			TableColumn col = table.getColumnModel().getColumn(columnIndex);

			SortOrder so = sortKey.getSortOrder();

			if (sb.length() > 0) {
				sb.append("|");
			}

			/*
			 * 0 = unsorted
			 * 1 = ascending
			 * 2 = descending
			 */
			int sortOrder = 0;

			if (so == SortOrder.ASCENDING) {
				sortOrder = 1;
			} else if (so == SortOrder.DESCENDING) {
				sortOrder = 2;
			}
			sb.append(col.getIdentifier() + "=" + sortOrder);
		}

		return sb.toString();
	}

	/**
	 * Sets the PreferredScrollableViewportSize based on font, leaving the height unchanged
	 * 
	 * @param table Table
	 * @param characterCount Count of characters to display
	 */
	public static void setPreferredScrollableViewportWidth(JTable table, int characterCount) {
		FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
		int charWidth = fontMetrics.charWidth('A');
		int preferredTableWidth = characterCount * charWidth;
		table.setPreferredScrollableViewportSize(new Dimension(preferredTableWidth, table.getPreferredScrollableViewportSize().height));
	}

	/**
	 * Sets the visible row count by setting the PreferredScrollableViewportSize based on row height, leaving the width unchanged
	 * 
	 * @param table Table
	 * @param rowCount Count of rows to display
	 */
	public static void setVisibleRowCount(JTable table, int rowCount) {
		table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredScrollableViewportSize().width, rowCount * table.getRowHeight()));
	}

	/**
	 * Calculate row height
	 * 
	 * @param table Table
	 * @param containsButtons True if rows contains buttons, false otherwise
	 * @param includeTableFont Include table font into calculation
	 * @return Row Height
	 */
	public static int calculateRowHeight(JTable table, boolean containsButtons, boolean includeTableFont) {
		List<Component> dummyComponents = new ArrayList<>();
		if (containsButtons) {
			dummyComponents.add(createDummyPanel(new JButton("Einstellungen")));
		}
		return calculateRowHeight(table, includeTableFont, dummyComponents);
	}

	/**
	 * Calculate row height
	 * 
	 * @param table Table
	 * @param includeTableFont Include table font into calculation
	 * @param dummyComponents Dummy Components, which can be provided to use them in the calculation. The preferred height of the components will be evaluated.
	 * @return Row Height
	 */
	public static int calculateRowHeight(JTable table, boolean includeTableFont, Component... dummyComponents) {
		return calculateRowHeight(table, includeTableFont, Arrays.asList(dummyComponents));
	}

	/**
	 * Calculate row height
	 * 
	 * @param table Table
	 * @param includeTableFont Include table font into calculation
	 * @param dummyComponents Dummy Components, which can be provided to use them in the calculation. The preferred height of the components will be evaluated.
	 * @return Row Height
	 */
	public static int calculateRowHeight(JTable table, boolean includeTableFont, List<Component> dummyComponents) {
		int renderedComponentRowHeight = 0;

		for (Component comp : dummyComponents) {
			renderedComponentRowHeight = Integer.max(renderedComponentRowHeight, comp.getPreferredSize().height);
		}

		int fontRowHeight = 0;
		if (includeTableFont) {
			FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
			fontRowHeight = fontMetrics.getLeading() + fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
		}

		int tableDefualtRowHeight = table.getRowHeight();

		return max(tableDefualtRowHeight, fontRowHeight, renderedComponentRowHeight);
	}

	/**
	 * Creates a panel with the given component added
	 * 
	 * @param dummyComp Dummy Component
	 * @return Panel with dummy component added
	 */
	private static JPanel createDummyPanel(Component dummyComp) {
		JPanel pnl = new JPanel();
		pnl.add(dummyComp);
		return pnl;
	}

	/**
	 * Returns the highest value
	 * 
	 * @param values Values
	 * @return Highest value
	 */
	private static int max(int... values) {
		int maxValue = 0;
		for (int value : values) {
			maxValue = Integer.max(value, maxValue);
		}
		return maxValue;
	}

	/**
	 * Calculate column header width
	 * 
	 * @param table Table
	 * @param column Column
	 * 
	 * @return Column Header Width
	 */
	public static int calculateColumnHeaderWidth(JTable table, TableColumn column) {
		return calculateColumnHeaderWidth(table, column, 0);
	}

	/**
	 * Calculate column header width
	 * 
	 * @param table Table
	 * @param column Column
	 * @param additionalChars Number of additional characters, which should be added to calculate width
	 * @return Column Header Width
	 */
	public static int calculateColumnHeaderWidth(JTable table, TableColumn column, int additionalChars) {
		String headerValue = String.valueOf(column.getHeaderValue());
		FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
		if (additionalChars <= 0) {
			return fontMetrics.stringWidth(headerValue);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(headerValue);
			for (int i = 0; i < additionalChars; i++) {
				sb.append("_");
			}
			return fontMetrics.stringWidth(sb.toString());
		}
	}

	/**
	 * Converts array of view index rows to model index rows
	 * 
	 * @param table Table
	 * @param viewRows View Index Rows
	 * @param sort True if converted rows should be sorted, false otherwise
	 * @return Converted array (model index rows)
	 */
	public static int[] convertRowIndexToModel(JTable table, int[] viewRows, boolean sort) {
		int[] convertedRows = new int[viewRows.length];
		for (int i = 0; i < viewRows.length; i++) {
			convertedRows[i] = table.convertRowIndexToModel(viewRows[i]);
		}
		if (sort) {
			Arrays.sort(convertedRows);
		}
		return convertedRows;
	}

	/**
	 * Converts array of model index rows to view index rows
	 * 
	 * @param table Table
	 * @param modelRows Model Index Rows
	 * @param sort True if converted rows should be sorted, false otherwise
	 * @return Converted array (view index rows)
	 */
	public static int[] convertRowIndexToView(JTable table, int[] modelRows, boolean sort) {
		int[] convertedRows = new int[modelRows.length];
		for (int i = 0; i < modelRows.length; i++) {
			convertedRows[i] = table.convertRowIndexToView(modelRows[i]);
		}
		if (sort) {
			Arrays.sort(convertedRows);
		}
		return convertedRows;
	}

	/**
	 * Converts array of view index columns to model index columns
	 * 
	 * @param table Table
	 * @param viewColumns View Index Columns
	 * @param sort True if converted columns should be sorted, false otherwise
	 * @return Converted array (model index columns)
	 */
	public static int[] convertColumnIndexToModel(JTable table, int[] viewColumns, boolean sort) {
		int[] convertedColumns = new int[viewColumns.length];
		for (int i = 0; i < viewColumns.length; i++) {
			convertedColumns[i] = table.convertColumnIndexToModel(viewColumns[i]);
		}
		if (sort) {
			Arrays.sort(convertedColumns);
		}
		return convertedColumns;
	}

	/**
	 * Converts array of model index columns to view index columns
	 * 
	 * @param table Table
	 * @param modelColumns Model Index Columns
	 * @param sort True if converted columns should be sorted, false otherwise
	 * @return Converted array (view index columns)
	 */
	public static int[] convertColumnIndexToView(JTable table, int[] modelColumns, boolean sort) {
		int[] convertedColumns = new int[modelColumns.length];
		for (int i = 0; i < modelColumns.length; i++) {
			convertedColumns[i] = table.convertColumnIndexToView(modelColumns[i]);
		}
		if (sort) {
			Arrays.sort(convertedColumns);
		}
		return convertedColumns;
	}
}
