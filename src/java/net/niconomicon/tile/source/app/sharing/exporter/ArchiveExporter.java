/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.exporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.niconomicon.tile.source.app.sharing.DisplayableCheckBoxTable;

/**
 * @author Nicolas Hoibian
 * 
 *         The aim of this class is to export the JSON feed, the html page, the thumbnails, the minatures and the .disp
 *         files into a single zip or tar file so that the user can unzip it into a web page.
 * 
 */
public class ArchiveExporter {
	DisplayableCheckBoxTable table;

	public static void showDialog(JComponent parent, Map<String, String> nameToFiles) {
		JFileChooser chooser = new JFileChooser();
		boolean notDecided = true;
		double weight = 0.0f;
		int res0, res1, res2;
		while (notDecided) {
			res0 = JOptionPane
					.showConfirmDialog(
							parent,
							"This will create a Zip file of all the selected displayable and their thumbnail, miniatures and the webpage and web feed. The archive will be of " + weight + " MB. Are you sure you want to continue ?");
			if (res0 == JOptionPane.NO_OPTION || res0 == JOptionPane.CANCEL_OPTION) { return; }
			res1 = chooser.showSaveDialog(parent);
			if (res1 == JFileChooser.CANCEL_OPTION) { return; }
			if (chooser.getSelectedFile().exists()) {
				res2 = JOptionPane.showConfirmDialog(parent, "Are you sure you want to overwrite this file?", "Overwrite file",
						JOptionPane.YES_NO_OPTION);
				if (res2 == JOptionPane.NO_OPTION) {
					continue;
				}
			}
			try {
				zipIt(nameToFiles, chooser.getSelectedFile().getAbsolutePath());
				notDecided = false;
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(parent, ex, "Error while trying to write the zip file", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static void zipIt(Map<String, String> nameToFiles, String destination) throws IOException {

		File zipFile = new File(destination);

		String path = zipFile.getPath();
		System.out.println("path : " + path);

		if (zipFile.exists()) {
			System.out.println("Erasing and recreating the file");
			zipFile.delete();
			zipFile.createNewFile();
		}
		System.out.println("Zipping to " + zipFile.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		int bytesRead;
		byte[] buffer = new byte[1024];
		CRC32 crc = new CRC32();
		for (Entry<String, String> emtry : nameToFiles.entrySet()) {
			String name = emtry.getKey();
			String fpath = emtry.getValue();

			File file = new File(fpath);
			if (!file.exists()) {
				System.err.println("Skipping: " + name);
				continue;
			}
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			crc.reset();
			while ((bytesRead = bis.read(buffer)) != -1) {
				crc.update(buffer, 0, bytesRead);
			}
			bis.close();
			// Reset to beginning of input stream
			bis = new BufferedInputStream(new FileInputStream(file));
			ZipEntry entry = new ZipEntry(name);
			entry.setMethod(ZipEntry.STORED);
			entry.setCompressedSize(file.length());
			entry.setSize(file.length());
			entry.setCrc(crc.getValue());
			zos.putNextEntry(entry);
			while ((bytesRead = bis.read(buffer)) != -1) {
				zos.write(buffer, 0, bytesRead);
			}
			bis.close();
		}
		zos.close();
	}
}
