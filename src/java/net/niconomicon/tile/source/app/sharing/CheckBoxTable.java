/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.SaveDialog;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;

/**
 * @author niko
 * 
 */
public class CheckBoxTable extends JTable {

	private final String[] columnsTitles = new String[] { "Share", "Title", "Edit", "View" };

	CustomTableModel model;
	boolean sharingDefaut = true;

	public static final int colCheckBox = 0;
	public static final int colTitle = 1;
	public static final int colEdit = 2;

	 public static final int colView = 3;

	 public static final int colWidthShare = 40;
	 public static final int colWidthEdit = 50;
	 public static final int colWidthView = 40;
	// public static final int colRemove = 4;

	public CheckBoxTable(DisplayableViewer viewer) {
		super();
		SaveDialog saveDialog = new SaveDialog();

		model = new CustomTableModel();

		this.setModel(model);
		this.getColumnModel().getColumn(colCheckBox).setPreferredWidth(colWidthShare);
		this.getColumnModel().getColumn(colCheckBox).setMinWidth(colWidthShare);
		this.getColumnModel().getColumn(colCheckBox).setMaxWidth(colWidthShare);
		
		this.getColumnModel().getColumn(colTitle).setPreferredWidth(200);

		this.getColumnModel().getColumn(colEdit).setPreferredWidth(colWidthEdit);
		this.getColumnModel().getColumn(colEdit).setMinWidth(colWidthEdit);
		this.getColumnModel().getColumn(colEdit).setMaxWidth(colWidthEdit);
		
		this.getColumnModel().getColumn(colView).setPreferredWidth(colWidthView);
		this.getColumnModel().getColumn(colView).setMinWidth(colWidthView);
		this.getColumnModel().getColumn(colView).setMaxWidth(colWidthView);
		
		this.setMinimumSize(new Dimension(330, 100));
		ColoredCellRenderer a = new ColoredCellRenderer();
		this.getColumnModel().getColumn(colTitle).setCellRenderer(a);

		ButtonForTable b = new ButtonForTable(viewer, "view");
		this.getColumnModel().getColumn(colView).setCellEditor(b);
		this.getColumnModel().getColumn(colView).setCellRenderer(b);

		ButtonForTable b1 = new ButtonForTable(saveDialog, "edit");
		this.getColumnModel().getColumn(colEdit).setCellEditor(b1);
		this.getColumnModel().getColumn(colEdit).setCellRenderer(b1);

		// ButtonForTable b2 = new ButtonForTable("remove");
		// this.getColumnModel().getColumn(colRemove).setCellEditor(b2);
		// this.getColumnModel().getColumn(colRemove).setCellRenderer(b2);
		// Add the scroll pane to this panel.
		// this.add(scrollPane,BorderLayout.CENTER);

	}

	public void addData(Map<String, String> pathToTitle) {
		model.addData(pathToTitle);
	}

	public void addDisplayable(String location, String title) {
		DisplayableInfos i = new DisplayableInfos(location, title);
		model.addDisplayable(i);
	}

	public Collection<String> getSelectedTilesSetFiles() {
		return model.getSelectedItems();
	}

	public void updateLocation(String oldLocation, String newLocation) {
		model.updateDisplayableLocation(oldLocation, newLocation);
	}

	private class DisplayableInfos implements Comparable<DisplayableInfos> {
		String title;
		String location;
		boolean shouldShare;

		public DisplayableInfos(String path, String title) {
			this.title = title;
			this.location = path;
			this.shouldShare = true;
		}

		public int compareTo(DisplayableInfos o) {
			return title.compareTo(o.title);
		}
	}

	class CustomTableModel extends DefaultTableModel {

		List<DisplayableInfos> backstore;

		public CustomTableModel() {
			super();
			setColumnIdentifiers(columnsTitles);
			backstore = new ArrayList<CheckBoxTable.DisplayableInfos>();
		}

		public int getColumnCount() {
			return columnsTitles.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int row, int column) {
			if (null != backstore && column < columnsTitles.length && row < backstore.size()) {
				DisplayableInfos i = backstore.get(row);
				switch (column) {
				case -1:
					return i.location;
				case colCheckBox:
					return i.shouldShare;
				case colTitle:
					return i.title;
				case colView:
					return "view";
				case colEdit:
					return Ref.isInTmpLocation(i.location) ? "!save!" : "edit";
					// case colRemove:
					// return "remove";
				}
			}
			return null;
		}

		public boolean needsSaving(int row) {
			if (null != backstore && row < backstore.size()) {
				DisplayableInfos i = backstore.get(row);
				return Ref.isInTmpLocation(i.location);
			}
			return false;
		}

		public void setValueAt(Object aValue, int row, int column) {
			if (column == -1 && aValue != null && row < backstore.size()) {
				backstore.get(row).location = (String) aValue;
				try {
					backstore.get(row).title = SQliteTileCreatorMultithreaded.getTitle(backstore.get(row).location);
				} catch (Exception ex) {
					ex.printStackTrace();
				};
			}
			System.out.println("aValue:" + aValue);
			if (column == 0) {//
				backstore.get(row).shouldShare = ((Boolean) aValue).booleanValue();
			}
			Collections.sort(backstore);
			fireTableDataChanged();
		}

		public int getRowCount() {
			if (null == backstore) { return 0; }
			return backstore.size();
		}

		public void addData(Map<String, String> pathToTitle) {
			// backstore.clear();
			for (Entry<String, String> elem : pathToTitle.entrySet()) {
				DisplayableInfos info = new DisplayableInfos(elem.getKey(), elem.getValue());
				backstore.add(info);
			}
			Collections.sort(backstore);
			fireTableDataChanged();
		}

		public void addDisplayable(DisplayableInfos infos) {
			backstore.add(infos);
			Collections.sort(backstore);
			fireTableDataChanged();
		}

		public void removeDisplayable(int[] rows) {
			Arrays.sort(rows);
			for (int i = rows.length - 1; i >= 0; i--) {
				int k = rows[i];
				System.out.println("Removed the selection : " + backstore.remove(k));
			}
			// Collections.sort(backstore);
			fireTableDataChanged();
		}

		public void updateDisplayableLocation(String oldLocation, String newlocation) {
			DisplayableInfos i;
			for (DisplayableInfos info : backstore) {
				if (info.location.contentEquals(oldLocation)) {
					info.location = newlocation;;
				}
			}
			fireTableDataChanged();
		}

		public Collection<String> getSelectedItems() {
			List<String> l = new ArrayList<String>();
			for (DisplayableInfos info : backstore) {
				if (info.shouldShare) {
					l.add(info.location);
				}
			}
			return l;
		}

		public boolean isCellEditable(int row, int column) {
			return null != backstore && row < backstore.size() && column < columnsTitles.length && column != 1;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) { return Boolean.class; }
			if (columnIndex == 1) { return String.class; }
			if (columnIndex == 2) { return String.class; }
			if (columnIndex == 3) { return String.class; }
			return super.getColumnClass(columnIndex);
		}
	}

	public class ColoredCellRenderer extends DefaultTableCellRenderer {
		Color defaultForegroundColor;
		Color defaultForegroundColorSelected;
		Color defaultBackgroundColor;
		Color defaultBackgroundColorSelected;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (isSelected) {
				if (defaultBackgroundColorSelected == null) {
					defaultBackgroundColorSelected = c.getBackground();
				}
				if (defaultForegroundColorSelected == null) {
					defaultForegroundColorSelected = c.getForeground();
				}
			} else {
				if (defaultBackgroundColor == null) {
					defaultBackgroundColor = c.getBackground();
				}
				if (defaultForegroundColor == null) {
					defaultForegroundColor = c.getForeground();
				}
			}
			if (((CustomTableModel) table.getModel()).needsSaving(row)) {
				c.setBackground(Color.orange);
			} else {
				if (isSelected) {
					c.setBackground(defaultBackgroundColorSelected);
				} else {
					c.setBackground(defaultBackgroundColor);
				}
			}
			return c;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame f = new JFrame("test map list model");
		Map<String, String> mapLost = new HashMap<String, String>();
		mapLost.put("france.mdb", "Map of france");
		mapLost.put("faso.mdb", "Map of burkina Faso");
		mapLost.put("uk.mdb", "Map of United kingdom of england and northern ireland");
		CheckBoxTable list = new CheckBoxTable(null);
		f.setContentPane(list);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		while (true) {
			try {
				Thread.sleep(3000);
				list.addData(mapLost);
				for (int i = 0; i < list.model.getRowCount(); i++) {
					System.out
							.println("Value for i = " + i + " :: " + list.model.getValueAt(i, 0) + "::" + list.model.getValueAt(i, 1) + " :: " + list.model
									.getValueAt(i, 2));
				}
				break;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
