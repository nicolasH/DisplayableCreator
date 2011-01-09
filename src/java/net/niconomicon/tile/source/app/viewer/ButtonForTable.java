/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.niconomicon.tile.source.app.SaveDialog;

/**
 * @author Nicolas Hoibian
 * 
 */
public class ButtonForTable extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

	ImageTileSetViewer viewer = null;
	SaveDialog saveDialog = null;

	String lastValue = null;
	JButton ren;
	String defaultText;

	int lastRow;
	JTable lastTable;

	public ButtonForTable(SaveDialog save, String text) {
		saveDialog = save;
		defaultText = text;
		ren = new JButton(text);
	}

	public ButtonForTable(ImageTileSetViewer viewer, String text) {
		defaultText = text;
		ren = new JButton(text);
		this.viewer = viewer;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		lastRow = row;
		lastTable = table;

		JButton b = ren;
		if (value == null) {
			b.setText(defaultText);
		} else {
			b.setText(value.toString());
		}
		return b;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		lastRow = row;
		lastTable = table;

		lastValue = value.toString();
		JButton b = new JButton();
		b.addActionListener(this);
		b.setText(value.toString());
		return b;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String fileLocation = "";
		if (null != viewer) {
			fileLocation = (String) lastTable.getValueAt(lastRow, -1);
			viewer.setTileSet(fileLocation);
			return;
		}
		if (null != saveDialog) {

			fileLocation = (String) lastTable.getValueAt(lastRow, -1);
			saveDialog.showDialog(lastTable, fileLocation);
			return;
		}
		System.out.println("Action performed. Presumably for file " + fileLocation);
		// setTileSet()
	}

}
