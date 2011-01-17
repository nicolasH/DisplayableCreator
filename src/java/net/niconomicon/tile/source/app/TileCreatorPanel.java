package net.niconomicon.tile.source.app;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.niconomicon.tile.source.app.filter.ImageFileFilter;
import net.niconomicon.tile.source.app.sharing.TilesetSharingPanel;
import net.niconomicon.tile.source.app.tiling.Inhibitor;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.TilingPreview;

/**
 * @author niko
 * 
 */
public class TileCreatorPanel extends JLayeredPane {

	public static final int TILE_SIZE = 192;
	public static final String TILE_TYPE = "png";

	public static final String USER_HOME = "user.home";
	protected JComboBox tileSize;

	protected JTextField from;

	protected JButton browseInput;

	protected JLabel imageProperty;

	JProgressBar progressIndicator;

	protected String place;
	protected String name;

	JFileChooser sourceChooser;

	ImageFileFilter imageFilter;
	FileFilter archiveFilter;

	TilesetSharingPanel sharingPanel;
	TilingPreview preview;

	SQliteTileCreatorMultithreaded creator;

	File temp;

	protected String currentSourcePath;

	JPanel input;
	JPanel status;

	// JLayeredPane content;
	Inhibitor inhibitor;

	public TileCreatorPanel() {
		super();
		creator = new SQliteTileCreatorMultithreaded();
		// content = new JLayeredPane();// new BorderLayout());

		imageFilter = new ImageFileFilter();

		sourceChooser = new JFileChooser();
		sourceChooser.setAcceptAllFileFilterUsed(false);
		sourceChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		sourceChooser.setFileFilter(imageFilter);
		sourceChooser.setDialogTitle("Open Supported Images");
		sourceChooser.setCurrentDirectory(new File(System.getProperty(USER_HOME)));

		// preview = new TilingPreview();

		from = new JTextField("", 20);
		from.setEditable(false);
		browseInput = new JButton("Choose image");
		browseInput.addActionListener(new InputActionListener());

		// tileSize = new JComboBox(new String[] { "64", "128", "192", "256" });
		// tileSize.setSelectedIndex(2);
		// tileSize = new JComboBox(new String[] { "" + TILE_SIZE });
		// tileSize.setSelectedIndex(0);

		progressIndicator = new JProgressBar(0, 100);
		inhibitor = new Inhibitor(progressIndicator);
		// Replacing the FormLayout by a GridBagLayout

		status = initStatusPanel();
		input = initInputPanel();

		this.setLayout(new GridLayout(0, 1));
		this.add(input, new Integer(0));
		this.setPreferredSize(new Dimension(300, 60));
		// content.moveToFront(input);

	}

	public JPanel initInputPanel() {

		JPanel input = new JPanel(new GridBagLayout());

		GridBagConstraints c;
		int y = 0;
		int x = 0;

		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.anchor = c.LINE_END;
		input.add(new JLabel("Source image :"), c);

		x++;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.weightx = 3;
		c.anchor = c.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		input.add(from, c);

		x++;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.anchor = c.LINE_START;
		input.add(browseInput, c);

		return input;
	}

	public JPanel initStatusPanel() {
		JPanel status = new JPanel(new GridBagLayout());
		GridBagConstraints c;
		int y = 0;
		int x = 0;

		c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = 3;
		c.weightx = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		status.add(progressIndicator, c);

		x = 4;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(inhibitor);
		status.add(cancel, c);

		return status;

	}

	// this will open and tile the image in a separate thread
	public void preTile(String sourcePath) {
		try {
			// Create temp file.
			temp = File.createTempFile(Ref.fileSansDot(sourcePath) + "_", Ref.ext_db);
			// Delete temp file when program exits.
			temp.deleteOnExit();// Apparently survives if renamed before exit.
			currentSourcePath = sourcePath;

			this.remove(input);
			this.add(status, new Integer(0));

			Thread t = new Thread() {
				public void run() {
					try {
						progressIndicator.setIndeterminate(false);
						progressIndicator.setValue(0);
						progressIndicator.setEnabled(true);
						progressIndicator.setStringPainted(true);
						progressIndicator.setString("Opening file ...");
						progressIndicator.setValue(1);
						long start = System.currentTimeMillis();
						Communicator comm = new Communicator(preview);
						creator.calculateTiles(temp.getAbsolutePath(), currentSourcePath, TILE_SIZE, TILE_TYPE, progressIndicator, TileCreatorApp.ThreadCount, inhibitor);
						long end = System.currentTimeMillis();
						System.out.println("creation time : " + (end - start) + " ms. == " + ((end - start) / 1000) + "s " + ((end - start) / 1000 / 60) + "min");
						creator.finalizeFile();
						progressIndicator.setEnabled(false);
						from.setText("");
						if (inhibitor.hasRunInhibitionBeenRequested()) {
							progressIndicator.setValue(0);
							progressIndicator.setString("Cancelled");
							temp.delete();
						} else {
							sharingPanel.addTileSetToShare(temp.getAbsolutePath(), Ref.fileSansDot(currentSourcePath));
							progressIndicator.setValue(100);
							progressIndicator.setString("Done");
						}
						TileCreatorPanel.this.remove(status);
						TileCreatorPanel.this.add(input, new Integer(0));
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
			String s = "Some file";
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
					preTile(sourceChooser.getSelectedFile().getAbsolutePath());
				} catch (Exception e) {
					from.setText("Sorry, cannot open the file");
					e.printStackTrace();
				}
			}
		}
	}

}
