package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.niconomicon.tile.source.app.filter.ImageFileFilter;
import net.niconomicon.tile.source.app.sharing.TilesetSharingPanel;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.TilingPreview;

/**
 * @author niko
 * 
 */
public class TileCreatorPanel extends JPanel {

	public static final int TILE_SIZE = 192;
	public static final String TILE_TYPE = "png";

	public static final String USER_HOME = "user.home";
	protected JComboBox tileSize;

	protected JTextField where;
	protected JTextField outputFileName;
	protected JTextField from;

	protected JButton browseInput;
	protected JButton browseOutput;

	protected JLabel imageProperty;

	JProgressBar progressIndicator;

	protected String place;
	protected String name;

	JFileChooser sourceChooser;

	ImageFileFilter imageFilter;
	FileFilter archiveFilter;

	TilesetSharingPanel sharingPanel;
	TilingPreview preview;

	JTextField source;

	SQliteTileCreatorMultithreaded creator;

	File temp;

	// protected TileArchiveCreator tileCreator;

	public TileCreatorPanel() {

		creator = new SQliteTileCreatorMultithreaded();
		JPanel content = new JPanel(new BorderLayout());
		JPanel option = new JPanel(new GridBagLayout());

		imageFilter = new ImageFileFilter();

		sourceChooser = new JFileChooser();
		sourceChooser.setAcceptAllFileFilterUsed(false);
		sourceChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		sourceChooser.setFileFilter(imageFilter);
		sourceChooser.setDialogTitle("Open Supported Images");
		sourceChooser.setCurrentDirectory(new File(System.getProperty(USER_HOME)));

		preview = new TilingPreview();

		from = new JTextField("", 20);
		from.setEditable(false);
		browseInput = new JButton("Choose image");
		browseInput.addActionListener(new InputActionListener());

		// tileSize = new JComboBox(new String[] { "64", "128", "192", "256" });
		// tileSize.setSelectedIndex(2);
		tileSize = new JComboBox(new String[] { "" + TILE_SIZE });
		tileSize.setSelectedIndex(0);

		// could also load from the user's preferences
		source = new JTextField("", 20);

		// could also load from the user's preferences

		// load from file name
		outputFileName = new JTextField("", 10);

		where = new JTextField("", 20);
		where.setEditable(false);

		progressIndicator = new JProgressBar(0, 100);
		// Replacing the FormLayout by a GridBagLayout
		GridBagConstraints c;
		int y = 0;
		int x = 0;

		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x;
		c.anchor = c.LINE_END;
		option.add(new JLabel("Source image :"), c);

		// c = new GridBagConstraints();
		// c.gridy = y++;
		// c.gridx = x;
		// c.anchor = c.LINE_END;
		// option.add(new JLabel("Current action :"), c);

		y = 0;
		x = 1;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.anchor = c.LINE_START;
		option.add(from, c);

		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x + 1;
		c.anchor = c.LINE_START;
		option.add(browseInput, c);

		y++;
		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		option.add(progressIndicator, c);

		content.add(option, BorderLayout.CENTER);
		// content.add(new JLabel("Image goes here"), BorderLayout.CENTER);

		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.NORTH);
	}

	protected String currentSourcePath;

	public void preTile(String sourcePath) {
		try {
			// Create temp file.
			temp = File.createTempFile(Ref.fileSansDot(sourcePath) + "_", Ref.ext_db);
			// Delete temp file when program exits.
			temp.deleteOnExit();// Apparently survives if renamed before exit.
			currentSourcePath = sourcePath;

			Thread t = new Thread() {
				public void run() {
					try {
						progressIndicator.setValue(0);
						progressIndicator.setEnabled(true);
						progressIndicator.setStringPainted(true);
						progressIndicator.setString("Opening file ...");
						progressIndicator.setValue(1);
						long start = System.currentTimeMillis();
						Communicator comm = new Communicator(preview);
						creator.calculateTiles(temp.getAbsolutePath(), currentSourcePath, TILE_SIZE, TILE_TYPE, progressIndicator, 8);
						long end = System.currentTimeMillis();
						System.out.println("creation time : " + (end - start) + " ms. == " + ((end - start) / 1000) + "s " + ((end - start) / 1000 / 60) + "min");
						// progressIndicator.setIndeterminate(false);
						creator.finalizeFile();
						progressIndicator.setValue(100);
						progressIndicator.setString("Done");
						sharingPanel.addTileSetToShare(temp.getAbsolutePath(), Ref.fileSansDot(currentSourcePath));
						progressIndicator.setEnabled(false);
						from.setText("");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setSharingService(TilesetSharingPanel sharingPanel) {
		this.sharingPanel = sharingPanel;
	}

	private class InputActionListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			String s = " some file";
			int returnVal = sourceChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("You chose to open this file: " + sourceChooser.getSelectedFile().getName());
				s = sourceChooser.getSelectedFile().getName();
				try {
					from.setText(sourceChooser.getSelectedFile().getCanonicalPath());
					from.setToolTipText("Image Tile set is going to be created from " + sourceChooser.getSelectedFile().getCanonicalPath());
					String fileName = sourceChooser.getSelectedFile().getName();
					String fileSansDot = fileName.substring(0, fileName.lastIndexOf("."));
					// title.setText(fileSansDot);
					if (null == where.getText() || 0 == where.getText().length()) {
						where.setText(sourceChooser.getSelectedFile().getParent());
					}
					outputFileName.setText(fileSansDot + Ref.ext_db);
					preTile(sourceChooser.getSelectedFile().getAbsolutePath());
				} catch (Exception e) {
					from.setText("Sorry, cannot open the file");
					e.printStackTrace();
				}
			}
		}
	}

	private class OutputActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {}
	}

	// /**
	// * @param args
	// */
	// public static void main(String[] args) {
	// TileCreatorPanel f = new TileCreatorPanel();
	// }

}
