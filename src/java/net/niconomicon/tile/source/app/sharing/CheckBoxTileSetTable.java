/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.niconomicon.tile.source.app.viewer.ButtonForTable;
import net.niconomicon.tile.source.app.viewer.ImageTileSetViewer;

/**
 * @author niko
 * 
 */
public class CheckBoxTileSetTable extends JTable {

	CustomTableModel model;
	boolean sharingDefaut = true;

	public CheckBoxTileSetTable(ImageTileSetViewer viewer) {
		super();

		model = new CustomTableModel();
		this.setModel(model);
		this.getColumnModel().getColumn(0).setPreferredWidth(30);
		this.getColumnModel().getColumn(1).setPreferredWidth(200);
		this.setMinimumSize(new Dimension(330, 100));

		ButtonForTable b = new ButtonForTable(viewer, "view");
		this.getColumnModel().getColumn(2).setCellEditor(b);
		this.getColumnModel().getColumn(2).setCellRenderer(b);

		ButtonForTable b1 = new ButtonForTable(viewer, "edit");
		this.getColumnModel().getColumn(3).setCellEditor(b1);
		this.getColumnModel().getColumn(3).setCellRenderer(b1);
		// Add the scroll pane to this panel.
		// this.add(scrollPane,BorderLayout.CENTER);
	}

	public void setData(Map<String, String> pathToTitle) {
		model.setData(pathToTitle);
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
				case 0:
					return i.shouldShare;
				case 1:
					return i.title;
				case 2:
					return "view";
				case 3:
					return "edit";
				}
			}
			return null;
		}


		public void setValueAt(Object aValue, int row, int column) {
			System.out.println("aValue:" + aValue);
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
			fireTableDataChanged();
		}

		public void addTileSet(String title, String location) {
			TileSetInfos i = new TileSetInfos(location, title);
			backstore.add(i);
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

	public Collection<String> getSelectedTilesSetFiles() {
		return model.getSelectedItems();
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
