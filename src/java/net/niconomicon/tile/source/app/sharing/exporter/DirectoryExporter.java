/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.exporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DirectoryExporter {

	public static void showDialog(JComponent parent, Map<String, String> nameToFiles) {
		JFileChooser chooser = new JFileChooser();
		boolean notDecided = true;
		double weight = 0.0f;
		int res0, res1, res2;
		while (notDecided) {
			res0 = JOptionPane
					.showConfirmDialog(
							parent,
							"This will copy the selected displayable and their thumbnail, miniatures, the webpage and web feed to a new directory for easy uploading to a web site. Its size will be " + weight + " MB. Are you sure you want to continue ?");
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
				copyFiles(chooser.getSelectedFile().getAbsolutePath(), nameToFiles);
				notDecided = false;
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(parent, ex, "Error while trying to write the zip file", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static void copyFiles(String directory, Map<String, String> nameToFiles) throws IOException {
		File destFile = new File(directory);
		if (!destFile.exists() || !destFile.isDirectory()) {
			destFile.mkdir();
		}

		FileChannel source = null;
		FileChannel destination = null;
		for (Entry<String, String> entry : nameToFiles.entrySet()) {
			String name = entry.getKey();
			String fpath = entry.getValue();

			File file = new File(fpath);
			String destPath = destFile.getAbsolutePath() + File.separator + Ref.fileSansPath(name);

			try {
				destination = new FileOutputStream(destPath).getChannel();
				if (!file.exists()) {
					System.out.println("Creating " + name);
					destination.write(ByteBuffer.wrap(fpath.getBytes()));
				} else {
					System.out.println("Writing " + name);
					source = new FileInputStream(file).getChannel();
					destination.transferFrom(source, 0, source.size());
				}
			} finally {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}
		}
	}
}
