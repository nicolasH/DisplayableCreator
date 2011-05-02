/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.SaveDialog;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;
import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;

/**
 * @author Nicolas Hoibian
 * 
 */
public class ButtonForTable extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

	DisplayableViewer viewer = null;
	SaveDialog saveDialog = null;

	// String lastValue = null;
	JButton ren;
	String defaultText;

	int lastRow;
	JTable lastTable;
	IconsLoader iconLoader;

	public ButtonForTable(String text) {
		defaultText = text;
		ren = new JButton();
	}

	public static final String text_save = "! Save this Displayable ! Otherwise it will be erased when you quit.";
	public static final String text_edit = "Edit Displayable : title, file name, location";
	public static final String text_view = "View Displayable";

	public ButtonForTable(SaveDialog save, String text) {
		this(text);
		saveDialog = save;
		iconLoader = IconsLoader.getIconsLoader();

	}

	public ButtonForTable(DisplayableViewer viewer, String text) {
		this(text);
		this.viewer = viewer;
		iconLoader = IconsLoader.getIconsLoader();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		String text;
		if (value == null) {
			text = defaultText;
		} else {
			text = value.toString();
		}
		ren.setToolTipText(text);
		ImageIcon ic = null;
		if (text.equals(text_save)) {
			ic = iconLoader.ic_save_16;
		}
		if (text.equals(text_edit)) {
			ic = iconLoader.ic_edit_16;
		}
		if (text.equals(text_view)) {
			ic = iconLoader.ic_zoom_16;
		}
		ren.setIcon(ic);

		return ren;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return null;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		lastRow = row;
		lastTable = table;

		JButton b = new JButton();

		String text;
		if (value == null) {
			text = defaultText;
		} else {
			text = value.toString();
		}
		ImageIcon ic = null;
		if (text.equals(text_save)) {
			ic = iconLoader.ic_save_16;
		}
		if (text.equals(text_edit)) {
			ic = iconLoader.ic_edit_16;
		}
		if (text.equals(text_view)) {
			ic = iconLoader.ic_zoom_16;
		}
		b.setIcon(ic);
		b.setToolTipText(text);
		b.addActionListener(this);
		return b;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String fileLocation = "";
		System.out.println("Action source : " + e.getActionCommand());
		if (null != viewer) {
			fileLocation = (String) lastTable.getValueAt(lastRow, -1);
			System.out.println("Showing viewer : last row : " + lastRow + " file : " + fileLocation);
			viewer.setDisplayable(fileLocation);
			return;
		}
		if (null != saveDialog) {
			fileLocation = (String) lastTable.getValueAt(lastRow, -1);
			System.out.println("Showing save Dialog : last row : " + lastRow + " file : " + fileLocation);
			ResultStruct newInfos = saveDialog.showDialog(lastTable, fileLocation);
			if (newInfos.newLocation != null) {
				lastTable.setValueAt(newInfos.newLocation, lastRow, -1);
			}
			if (newInfos.newTitle != null) {
				lastTable.setValueAt(newInfos.newTitle, lastRow, CheckBoxTable.colTitle);
			}
			if (null != newInfos.newLocation && !Ref.isInTmpLocation(newInfos.newLocation)) {
				this.fireEditingStopped();
			}

			return;
		}
		((CheckBoxTable.CustomTableModel) lastTable.getModel()).removeDisplayable(new int[] { lastRow });

		System.out.println("Action performed. Presumably for file " + fileLocation);
	}

}
