package net.niconomicon.tile.source.app.input;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;

public class ImageQueueList extends JTable {

	LinkedList<File> imagesToTransform;
	CustomTableModel model;

	public static final int colTitle = 0;
	public static final int colRemove = 1;

	public static final int colWidthRemove = 40;

	JTable table;

	private static final Border border = BorderFactory
			.createLoweredBevelBorder();

	public ImageQueueList() {
		super();

		imagesToTransform = new LinkedList<File>();

		model = new CustomTableModel(imagesToTransform);

		this.setModel(model);

		this.getColumnModel().getColumn(colTitle).setPreferredWidth(200);
		this.getColumnModel().getColumn(colRemove)
				.setPreferredWidth(colWidthRemove);
		this.getColumnModel().getColumn(colRemove).setMinWidth(colWidthRemove);
		this.getColumnModel().getColumn(colRemove).setMaxWidth(colWidthRemove);

		RemoveButton b = new RemoveButton("Cancel");
		this.getColumnModel().getColumn(colRemove).setCellEditor(b);
		this.getColumnModel().getColumn(colRemove).setCellRenderer(b);
		this.setTableHeader(null);
		// this.setSize(300, 50);
		// this.setMaximumSize(new Dimension(300, 50));
	}

	/**
	 * 
	 * @param f
	 *            the image that should be removed because you don't want it to
	 *            become a displayable.
	 * 
	 */
	public void removeImage(File f) {
		synchronized (imagesToTransform) {
			imagesToTransform.remove(f);
		}
		if (imagesToTransform.size() == 0) {
			this.setBorder(null);
		}
		model.fireTableDataChanged();

	}

	public void removeImageAtIndex(int index) {
		synchronized (imagesToTransform) {
			imagesToTransform.remove(index);
		}
		model.fireTableDataChanged();
	}

	/**
	 * @return The image that should be transformed into a displayable
	 */
	public File removeImage() {
		File f = null;
		synchronized (imagesToTransform) {
			f = imagesToTransform.poll();
		}
		if (imagesToTransform.size() == 0) {
			this.setBorder(null);
		}
		model.fireTableDataChanged();
		return f;
	}

	public void addImage(File f) {
		synchronized (imagesToTransform) {
			imagesToTransform.add(f);
			imagesToTransform.notifyAll();
		}
		if (imagesToTransform.size() > 0 && this.getBorder() == null) {
			this.setBorder(border);
		}
		model.fireTableDataChanged();
	}

	class CustomTableModel extends DefaultTableModel {

		List<File> backstore;

		public CustomTableModel(List<File> coll) {
			super();
			backstore = coll;
		}

		public int getColumnCount() {
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int row, int column) {
			if (null != backstore && column < 2 && row < backstore.size()) {
				File f = backstore.get(row);

				switch (column) {
				case 0:
					return f.getName();
				case 1:
					return "Cancel";
				}
			}
			return null;
		}

		public String getTooltipAt(int row) {
			if (null != backstore && row < backstore.size()) {
				File f = backstore.get(row);
				return "Do not create a displayable from "
						+ f.getAbsolutePath();
			}
			return null;
		}

		public int getRowCount() {
			if (null == backstore) {
				return 0;
			}
			return backstore.size();
		}

		public void removeFiles(int[] rows) {
			Arrays.sort(rows);
			for (int i = rows.length - 1; i >= 0; i--) {
				int k = rows[i];
				File infos = backstore.remove(k);
			}
			fireTableDataChanged();
		}

		public boolean isCellEditable(int row, int column) {
			return null != backstore && row < backstore.size();
		}

		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

	}

	class RemoveButton extends AbstractCellEditor implements TableCellRenderer,
			TableCellEditor, ActionListener {

		JButton ren;
		String defaultText;

		int lastRow;
		JTable lastTable;
		IconsLoader iconLoader;

		public RemoveButton(String text) {
			defaultText = text;
			ren = new JButton();
			iconLoader = IconsLoader.getIconsLoader();
		}

		public static final String tooltip_text_remove = "Click to not create a displayable from this image";

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			ren.setText("-");
			ren.setToolTipText(tooltip_text_remove);

			return ren;
		}

		public Object getCellEditorValue() {
			return null;
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			lastRow = row;
			System.out.println("last row : " + lastRow);
			lastTable = table;

			JButton b = new JButton("-");
			b.setToolTipText(tooltip_text_remove);
			b.addActionListener(this);
			return b;
		}

		public void actionPerformed(ActionEvent e) {
			removeImageAtIndex(lastRow);
		}

	}
}