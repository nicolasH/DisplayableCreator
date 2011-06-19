/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.SaveDialog;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableCheckBoxTable extends JPanel {

	private final String[] columnsTitles = new String[] { "Share", "Title", "Edit", "View" };

	CustomTableModel model;
	boolean sharingDefaut = true;

	public static final int colCheckBox = 0;
	public static final int colTitle = 1;
	public static final int colEdit = 2;

	public static final int colView = 3;

	public static final int colWidthShare = 40;
	public static final int colWidthEdit = 40;
	public static final int colWidthView = 40;

	// public static final int colRemove = 4;

	JTable table;

	public DisplayableCheckBoxTable(DisplayableViewer viewer) {
		super(new BorderLayout());
		SaveDialog saveDialog = new SaveDialog();
		table = new JTable();
		model = new CustomTableModel();

		table.setModel(model);
		table.getColumnModel().getColumn(colCheckBox).setPreferredWidth(colWidthShare);
		table.getColumnModel().getColumn(colCheckBox).setMinWidth(colWidthShare);
		table.getColumnModel().getColumn(colCheckBox).setMaxWidth(colWidthShare);

		table.getColumnModel().getColumn(colTitle).setPreferredWidth(200);

		table.getColumnModel().getColumn(colEdit).setPreferredWidth(colWidthEdit);
		table.getColumnModel().getColumn(colEdit).setMinWidth(colWidthEdit);
		table.getColumnModel().getColumn(colEdit).setMaxWidth(colWidthEdit);

		table.getColumnModel().getColumn(colView).setPreferredWidth(colWidthView);
		table.getColumnModel().getColumn(colView).setMinWidth(colWidthView);
		table.getColumnModel().getColumn(colView).setMaxWidth(colWidthView);

		table.setMinimumSize(new Dimension(330, 100));
		ColoredCellRenderer a = new ColoredCellRenderer();
		table.getColumnModel().getColumn(colTitle).setCellRenderer(a);

		ButtonForTable b = new ButtonForTable(viewer, "view");
		table.getColumnModel().getColumn(colView).setCellEditor(b);
		table.getColumnModel().getColumn(colView).setCellRenderer(b);

		ButtonForTable b1 = new ButtonForTable(saveDialog, "edit");
		table.getColumnModel().getColumn(colEdit).setCellEditor(b1);
		table.getColumnModel().getColumn(colEdit).setCellRenderer(b1);

		// ButtonForTable b2 = new ButtonForTable("remove");
		// this.getColumnModel().getColumn(colRemove).setCellEditor(b2);
		// this.getColumnModel().getColumn(colRemove).setCellRenderer(b2);
		// Add the scroll pane to this panel.
		// this.add(scrollPane,BorderLayout.CENTER);
		JButton removeButton = new JButton("Remove selected Displayable(s)");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int item = table.getSelectedRow();
				if (item < 0) {
					JOptionPane.showMessageDialog(DisplayableCheckBoxTable.this, "Please select a displayable", "No Displayable selected",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				((CustomTableModel) table.getModel()).removeDisplayable(table.getSelectedRows());
			}
		});
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		this.add(removeButton, BorderLayout.SOUTH);
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

	class CustomTableModel extends DefaultTableModel {

		List<DisplayableInfos> backstore;
		Set<String> knownPaths;

		public CustomTableModel() {
			super();
			setColumnIdentifiers(columnsTitles);
			backstore = new ArrayList<DisplayableInfos>();
			knownPaths = new HashSet<String>();

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
					return ButtonForTable.text_view;
				case colEdit:
					return Ref.isInTmpLocation(i.location) ? ButtonForTable.text_save : ButtonForTable.text_edit;
				}
			}
			return null;
		}

		public String getTooltipAt(int row) {
			if (null != backstore && row < backstore.size()) {
				DisplayableInfos i = backstore.get(row);
				return i.tooltip();
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
			if (column == colView) { return; }
			if (column == colEdit) { return; }

			if (column == -1 && aValue != null && row < backstore.size()) {
				String oldL = backstore.get(row).location;

				if (knownPaths.remove(oldL)) {
					backstore.get(row).location = (String) aValue;
					knownPaths.add(backstore.get(row).location);
				}
				// visible data did not change unless the it was a temporary file.
				if (!(Ref.isInTmpLocation(oldL) && Ref.isInTmpLocation(backstore.get(row).location))) { return; }
			}
			// visible data did change.
			if (column == colTitle && aValue != null && row < backstore.size()) {
				backstore.get(row).title = (String) aValue;
			}
			if (column == colCheckBox) {
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
				if (!knownPaths.contains(info.location)) {
					knownPaths.add(info.location);
					backstore.add(info);
				}
			}
			Collections.sort(backstore);
			fireTableDataChanged();
		}

		public void addDisplayable(DisplayableInfos infos) {
			backstore.add(infos);
			knownPaths.add(infos.location);
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
					knownPaths.remove(info.location);
					knownPaths.add(newlocation);
					info.location = newlocation;
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
			if (c instanceof JComponent) {
				((JComponent) c).setToolTipText(model.getTooltipAt(row));
			}
			return c;
		}
	}

}
