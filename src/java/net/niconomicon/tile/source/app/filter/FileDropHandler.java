/**
 * 
 */
package net.niconomicon.tile.source.app.filter;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.niconomicon.tile.source.app.input.DragAndDropManager;

/**
 * @author Nicolas Hoibian
 * 
 */
public class FileDropHandler extends TransferHandler {

	// DisplayableCreatorInputPanel panel;
	DragAndDropManager manager;
	ImageFileFilter filter_image;
	DisplayableFilter filter_disp;

	public FileDropHandler(DragAndDropManager manager) {
		this.manager = manager;
		filter_image = new ImageFileFilter();
		filter_disp = new DisplayableFilter();
	}

	/**
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
	 *      java.awt.datatransfer.DataFlavor[])
	 */
	public boolean canImport(JComponent arg0, DataFlavor[] arg1) {
		for (int i = 0; i < arg1.length; i++) {
			DataFlavor flavor = arg1[i];
			if (flavor.equals(DataFlavor.javaFileListFlavor)) {
//				System.out.println("canImport: JavaFileList FLAVOR: " + flavor);
				return true;
			}
			if (flavor.equals(DataFlavor.stringFlavor)) {
//				System.out.println("canImport: String FLAVOR: " + flavor);
				return true;
			}
//			System.err.println("canImport: Rejected Flavor: " + flavor);
		}
		// Didn't find any that match, so:
		return false;
	}

	/**
	 * Do the actual import.
	 * 
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
	 *      java.awt.datatransfer.Transferable)
	 */
	public boolean importData(JComponent comp, Transferable t) {
		DataFlavor[] flavors = t.getTransferDataFlavors();
		System.out.println("Trying to import:" + t);
		System.out.println("... which has " + flavors.length + " flavors." + flavors);
		boolean returnVal = false;
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor flavor = flavors[i];
			if (flavor.equals(DataFlavor.javaFileListFlavor)) {
				try {
					System.out.println("importData: FileListFlavor" + t.getTransferData(DataFlavor.javaFileListFlavor));
					List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					for (File file : fileList) {
						System.out.println("importing file:" + file);
						returnVal = addFile(file);
						System.out.println("Return val for accepting the file:" + returnVal);
						// Now do something with the file...
					}
				} catch (IOException ex) {
					System.err.println("IOError getting data: " + ex);
				} catch (UnsupportedFlavorException e) {
					System.err.println("Unsupported Flavor: " + e);
				}
			}
		}
		manager.wakeProcessors();
		// If you get here, I didn't like the flavor.
		// Toolkit.getDefaultToolkit().beep();
		return returnVal;
	}

	public boolean addFile(File file) throws IOException {
		boolean returnVal = false;
		if (file.isDirectory()) {
			for (String fName : file.list()) {
				File fileToCheck = new File(fName);
				returnVal = returnVal || addFile(fileToCheck);
				System.out.println((returnVal ? "Found Files in " : "Rejected") + file.getCanonicalPath());
			}
			return returnVal;
		}
		if (filter_image.accept(file) || filter_disp.accept(file)) {
			System.out.println("GOT something: " + file.getCanonicalPath());
			manager.addFile(file);
			return true;
		}
		return false;
	}
}
