/**
 * 
 */
package net.niconomicon.tile.source.app.filter;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.niconomicon.tile.source.app.DisplayableCreatorInputPanel;

/**
 * @author Nicolas Hoibian
 * 
 */
public class FileDropHandler extends TransferHandler {

	DisplayableCreatorInputPanel panel;
	ImageFileFilter filter;

	public FileDropHandler(DisplayableCreatorInputPanel p) {
		this.panel = p;
		filter = new ImageFileFilter();
	}

	/**
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
	 */
	public boolean canImport(JComponent arg0, DataFlavor[] arg1) {
		for (int i = 0; i < arg1.length; i++) {
			DataFlavor flavor = arg1[i];
			if (flavor.equals(DataFlavor.javaFileListFlavor)) {
				System.out.println("canImport: JavaFileList FLAVOR: " + flavor);
				return true;
			}
			if (flavor.equals(DataFlavor.stringFlavor)) {
				System.out.println("canImport: String FLAVOR: " + flavor);
				return true;
			}
			System.err.println("canImport: Rejected Flavor: " + flavor);
		}
		// Didn't find any that match, so:
		return false;
	}

	/**
	 * Do the actual import.
	 * 
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
	 */
	public boolean importData(JComponent comp, Transferable t) {
		DataFlavor[] flavors = t.getTransferDataFlavors();
		System.out.println("Trying to import:" + t);
		System.out.println("... which has " + flavors.length + " flavors.");
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor flavor = flavors[i];
			try {
				if (flavor.equals(DataFlavor.javaFileListFlavor)) {
					System.out.println("importData: FileListFlavor");

					List l = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator iter = l.iterator();
					while (iter.hasNext()) {
						File file = (File) iter.next();
						System.out.println("GOT FILE: " + file.getCanonicalPath());
						if (filter.accept(file)) {
							panel.setImageFileToTile(file);
							return true;
						}
						System.out.println("Rejected the file");
						// Now do something with the file...
					}
					// return true;
				}
			} catch (IOException ex) {
				System.err.println("IOError getting data: " + ex);
			} catch (UnsupportedFlavorException e) {
				System.err.println("Unsupported Flavor: " + e);
			}
		}
		// If you get here, I didn't like the flavor.
		Toolkit.getDefaultToolkit().beep();
		return false;
	}
}
