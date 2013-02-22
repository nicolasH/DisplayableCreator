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
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DirectoryExporter {

	public static void showDialog(JComponent parent, List<String> fullPaths) {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		boolean notDecided = true;
		long weight = 0;
		int res0, res1, res2;
		for (String file : fullPaths) {
			File f = new File(file);
			if (f.exists()) {
				weight += f.length();
			} else {
				weight += file.length();
			}
		}
		weight = weight / 100000;// 100kb
		double w = weight / 10.0;
		// String text =
		// "Select a directory and it will copy the shared displayables into it for easy uploading to a web site. \n"
		// +
		// "The directory will contain an HTML web page describing each displayables as well as the miniature and thumbnail"
		// +
		// " of each displayable. It will also contain a JSON file providing the same descriptions in a Displayator-friendly way. \n"
		// +
		// "You can point an iPhone web browser at the HTML page. That page contains a link that, when tapped, will launch the Displayator "
		// +
		// "and make it open the list of Displayables availables in that page.\n"
		// + "\n"
		// +
		// "If you upload this directory to a web host, you can share your displayables on the Internet.\n"
		// + "\n" + "The size of the directory will be " + w + " MB.\n"
		// + "\n Are you sure you want to continue ?";
		String text = "Select a directory and the shared displayables will be copied into it, as well as an html index "
				+ "and a json representation of the list (+thumbnails and miniatures). You can then upload this directory to a web site for easy"
				+ " internet sharing of your displayables." + "\n" + "The size of the directory will be " + w + " MB.";

		JTextArea area = new JTextArea(text);
		area.setColumns(40);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBackground(chooser.getBackground());
		area.setEditable(false);
		String[] options = new String[] { "Create " + w + " MB directory", " Nevermind !" };

		while (notDecided) {
			res0 = JOptionPane.showOptionDialog(parent, area, "Export " + fullPaths.size() + " files in a new, " + w + " MB directory",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (res0 != JOptionPane.YES_OPTION) { return; }
			res1 = chooser.showSaveDialog(parent);
			if (res1 != JFileChooser.APPROVE_OPTION) { return; }
			File f = chooser.getSelectedFile();
			if (f.exists()) {
				String message = "Are you sure you want to replace this file by a directory ?";
				if (f.isDirectory()) {
					message = "Continuing might overwrite existing files in this directory.";
				}
				res2 = JOptionPane.showConfirmDialog(parent, message, "Overwrite ?", JOptionPane.YES_NO_OPTION);
				if (res2 == JOptionPane.NO_OPTION) {
					continue;
				}
			}
			try {
				copyFiles(chooser.getSelectedFile().getAbsolutePath(), fullPaths);
				notDecided = false;
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(parent, ex, "Error while trying to export the files into "
						+ chooser.getSelectedFile().getAbsolutePath(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static void copyFiles(String directory, List<String> fullPaths) throws IOException {
		File destFile = new File(directory);
		if (!destFile.exists() || !destFile.isDirectory()) {
			destFile.mkdir();
		}

		FileChannel source = null;
		FileChannel destination = null;
		for (String fpath : fullPaths) {
			File file = new File(fpath);
			String fname = Ref.fileSansPath(fpath);
			String destPath = destFile.getAbsolutePath() + File.separator + fname;

			try {
				destination = new FileOutputStream(destPath).getChannel();
				if (!file.exists()) {
					System.out.println("Creating " + destPath);
					destination.write(ByteBuffer.wrap(fpath.getBytes()));
				} else {
					System.out.println("Overwriting " + destPath);
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
