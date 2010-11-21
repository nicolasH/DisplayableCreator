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
public class CheckBoxMapTable extends JTable {

	CustomTableModel model;
	boolean sharingDefaut = true;

	public CheckBoxMapTable(ImageTileSetViewer viewer) {
		super();

		model = new CustomTableModel();
		this.setModel(model);
		this.getColumnModel().getColumn(0).setPreferredWidth(30);
		this.getColumnModel().getColumn(1).setPreferredWidth(200);
		this.setMinimumSize(new Dimension(230, 100));
		ButtonForTable b = new ButtonForTable(viewer);
		this.getColumnModel().getColumn(2).setCellEditor(b);
		this.getColumnModel().getColumn(2).setCellRenderer(b);
		// Add the scroll pane to this panel.
		// this.add(scrollPane,BorderLayout.CENTER);
	}

	public void setData(Map<String, String> names) {
		model.setData(names);
	}

	public String getFileLocation(int index) {
		return (String) ((Vector) model.getDataVector().elementAt(index)).elementAt(3);
	}

	private class CustomTableModel extends DefaultTableModel {

		public CustomTableModel() {
			super(new Object[0][3], new String[] { "shared", "map name", "view" });
		}

		public int getColumnCount() {
			return 3;
		}

		public void setData(Map<String, String> names) {
			// save the current state
			Vector<Vector<Object>> newVec = new Vector<Vector<Object>>();
			for (String path : names.keySet()) {// looking for existing line
				Vector<Object> line = new Vector<Object>();
				String name = names.get(path);
				for (Object o : this.dataVector) {
					Vector<Object> v = (Vector<Object>) o;
					if (((String) v.elementAt(2)).compareTo(path) == 0) {
						line.add(v.elementAt(0));
						line.add(name);
						line.add(path);
						break;
					}
				}
				if (line.size() == 0) {// new line
					line.add(new Boolean(sharingDefaut));
					line.add(name);
					line.add(path);
				}
				newVec.add(line);
			}
			this.dataVector = newVec;
			fireTableDataChanged();
		}

		public Collection<String> getSelectedItems() {
			List<String> l = new ArrayList<String>();
			for (Object o : this.dataVector) {
				Vector<Object> v = (Vector<Object>) o;
				if (((Boolean) v.elementAt(0)).booleanValue()) {
					l.add((String) v.elementAt(2));
				}
			}
			return l;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) { return Boolean.class; }
			if (columnIndex == 1) { return String.class; }
			if (columnIndex == 2) { return String.class; }
			return super.getColumnClass(columnIndex);
		}
	}

	public Collection<String> getSelectedMapFiles() {
		return model.getSelectedItems();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame f = new JFrame("test map list model");
		Map<String, String> mapLost = new HashMap<String, String>();
		mapLost.put("frane.mdb", "Map of france");
		mapLost.put("faso.mdb", "Map of burkina Faso");
		mapLost.put("uk.mdb", "Map of United kingdom of england and northern ireland");
		CheckBoxMapTable list = new CheckBoxMapTable(null);
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
