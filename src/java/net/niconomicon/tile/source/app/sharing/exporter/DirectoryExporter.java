/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.exporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.sharing.server.jetty.JettyImageServerServlet;

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

		Map<String, String> allFiles = Ref.generateIndexFromFileNames(fullPaths);
		FileChannel source = null;
		FileChannel destination = null;
		String query = "";
		String field = "";
		if (!directory.endsWith(File.separator)) {
			directory += File.separator;
		}
		for (String key : allFiles.keySet()) {
			String file = allFiles.get(key);
			// System.out.println(key +"->"+file);
			String destPath = directory + key;
			// Displayables
			if (key.endsWith(".disp")) {
				try {
					// System.out.println("(Over)writing " + destPath);
					source = new FileInputStream(file).getChannel();
					destination = new FileOutputStream(destPath).getChannel();
					destination.transferFrom(source, 0, source.size());
				} finally {
					if (source != null) {
						source.close();
					}
					if (destination != null) {
						destination.close();
					}
				}
			}
			if (key.endsWith(Ref.ext_mini) || key.endsWith(Ref.ext_thumb)) {
				try {
					if (key.endsWith(Ref.ext_mini)) {
						query = "select " + Ref.infos_miniature + " from infos";
						field = Ref.infos_miniature;
					}
					if (key.endsWith(Ref.ext_thumb)) {
						query = "select " + Ref.infos_thumb + " from infos";
						field = Ref.infos_thumb;
					}
					Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
					connection.setReadOnly(true);
					Statement statement = connection.createStatement();
					statement.setQueryTimeout(5); // set timeout to 30 sec.

					ResultSet rs = statement.executeQuery(query);
					while (rs.next()) {
						FileOutputStream oStream = new FileOutputStream(destPath);
						oStream.write(rs.getBytes(field));
						oStream.close();
						break;
					}
					if (connection != null) connection.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				// either a displayable or html, json or css
				if (key.endsWith(".html") || key.endsWith(".json") || key.endsWith(".css")) {
					FileOutputStream oStream = new FileOutputStream(destPath);
					oStream.write(file.getBytes());
					oStream.close();
					oStream.close();
				}
			}
		}
	}

	public static void main(String[] args) {
		List<String> fullPaths = new ArrayList<String>();
		String base = "/Users/niko/TileSources/displayables/";
		String[] disps = new String[] { "tpg-plan-centre-9-decembre-12-4-1.disp", "tpg-plan-peripherique-9-decembre-12-2.disp",
				"tpg-plan-schematique-9-decembre-12-3.disp" };
		for (String d : disps) {
			fullPaths.add(base + d);
		}
		String directory = "/Users/niko/TESTING_DISP/";
		try {
			DirectoryExporter.copyFiles(directory, fullPaths);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
}
