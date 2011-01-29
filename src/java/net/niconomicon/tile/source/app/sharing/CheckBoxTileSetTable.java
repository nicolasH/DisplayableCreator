/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
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
import javax.swing.text.StyledEditorKit.ForegroundAction;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.SaveDialog;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.ImageTileSetViewerFrame;

/**
 * @author niko
 * 
 */
public class CheckBoxTileSetTable extends JTable {

	CustomTableModel model;
	boolean sharingDefaut = true;

	public CheckBoxTileSetTable(ImageTileSetViewerFrame viewer) {
		super();
		SaveDialog saveDialog = new SaveDialog();

		model = new CustomTableModel();
		this.setModel(model);
		this.getColumnModel().getColumn(0).setPreferredWidth(30);
		this.getColumnModel().getColumn(1).setPreferredWidth(200);
		this.setMinimumSize(new Dimension(330, 100));

		ColoredCellRenderer a = new ColoredCellRenderer();
		this.getColumnModel().getColumn(1).setCellRenderer(a);

		ButtonForTable b = new ButtonForTable(viewer, "view");
		this.getColumnModel().getColumn(2).setCellEditor(b);
		this.getColumnModel().getColumn(2).setCellRenderer(b);

		ButtonForTable b1 = new ButtonForTable(saveDialog, "edit");
		this.getColumnModel().getColumn(3).setCellEditor(b1);
		this.getColumnModel().getColumn(3).setCellRenderer(b1);
		// Add the scroll pane to this panel.
		// this.add(scrollPane,BorderLayout.CENTER);
	}

	public void setData(Map<String, String> pathToTitle) {
		model.setData(pathToTitle);
	}

	public void addTileSet(String location, String title) {
		TileSetInfos i = new TileSetInfos(location, title);
		model.addTileSet(i);
	}

	public Collection<String> getSelectedTilesSetFiles() {
		return model.getSelectedItems();
	}

	public void updateLocation(String oldLocation, String newLocation) {
		model.updateTileSetLocation(oldLocation, newLocation);
	}

	private class TileSetInfos implements Comparable<TileSetInfos> {
		String title;
		String location;
		boolean shouldShare;

		public TileSetInfos(String path, String title) {
			this.title = title;
			this.location = path;
			this.shouldShare = true;
		}

		public int compareTo(TileSetInfos o) {
			return title.compareTo(o.title);
		}
	}

	private class CustomTableModel extends DefaultTableModel {

		private final String[] columnsTitles = new String[] { "shared", "title", "view", "edit" };
		List<TileSetInfos> backstore;

		public CustomTableModel() {
			super();
			// new Object[0][3], new String[] { "shared", "map name", "view" });
			setColumnIdentifiers(columnsTitles);
			backstore = new ArrayList<CheckBoxTileSetTable.TileSetInfos>();
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
				TileSetInfos i = backstore.get(row);
				switch (column) {
				case -1:
					return i.location;
				case 0:
					return i.shouldShare;
				case 1:
					return i.title;
				case 2:
					return "view";
				case 3:
					return Ref.isInTmpLocation(i.location) ? "! save !" : "edit";
				}
			}
			return null;
		}

		public boolean needsSaving(int row) {
			if (null != backstore && row < backstore.size()) {
				TileSetInfos i = backstore.get(row);
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

		public void setData(Map<String, String> pathToTitle) {
			backstore.clear();
			for (Entry<String, String> elem : pathToTitle.entrySet()) {
				TileSetInfos info = new TileSetInfos(elem.getKey(), elem.getValue());
				backstore.add(info);
			}
			Collections.sort(backstore);
			fireTableDataChanged();
		}

		public void addTileSet(TileSetInfos infos) {
			backstore.add(infos);
			Collections.sort(backstore);
			fireTableDataChanged();
		}

		public void updateTileSetLocation(String oldLocation, String newlocation) {
			TileSetInfos i;
			for (TileSetInfos info : backstore) {
				if (info.location.contentEquals(oldLocation)) {
					info.location = newlocation;;
				}
			}
			fireTableDataChanged();
		}

		public Collection<String> getSelectedItems() {
			List<String> l = new ArrayList<String>();
			for (TileSetInfos info : backstore) {
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
		CheckBoxTileSetTable list = new CheckBoxTileSetTable(null);
		f.setContentPane(list);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		while (true) {
			try {
				Thread.sleep(3000);
				list.setData(mapLost);
				for (int i = 0; i < list.model.getRowCount(); i++) {
					System.out.println("Value for i = " + i + " :: " + list.model.getValueAt(i, 0) + "::" + list.model.getValueAt(i, 1) + " :: " + list.model.getValueAt(i, 2));
				}
				break;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
