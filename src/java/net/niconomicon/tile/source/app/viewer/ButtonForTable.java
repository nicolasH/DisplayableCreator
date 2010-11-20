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

/**
 * @author Nicolas Hoibian
 * 
 */
public class ButtonForTable extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

	long lastRow = 0;
	JButton ren;

	public ButtonForTable() {
		ren = new JButton("view");
	}

	public Object getCellEditorValue() {
		return "view";
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		System.out.println("Asking for the cell renderer at (" + row + " " + column + ") value : " + value);

		JButton b = ren;// new JButton("view");
		// if (null != value) {
		// b.setText(value.toString());
		// } else {
		b.setText("view");
		// }
		return b;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		System.out.println("Asking for the cell editor at (" + row + " " + column + ") : value " + value);
		lastRow = row;
		JButton b = new JButton("view");
		b.addActionListener(this);
		b.setText(value.toString());
		return b;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		System.out.println("Action performed. Presumably at row " + lastRow);
//		setTileSet()
	}
}
