package net.niconomicon.tile.source.app;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.niconomicon.tile.source.app.filter.FileDropHandler;
import net.niconomicon.tile.source.app.filter.ImageFileFilter;
import net.niconomicon.tile.source.app.sharing.TilesetSharingPanel;
import net.niconomicon.tile.source.app.tiling.Inhibitor;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.tiling.TilingStatusReporter;
import net.niconomicon.tile.source.app.viewer.TilingPreview;

/**
 * @author niko
 * 
 */
public class TileCreatorPanel extends JLayeredPane implements TilingStatusReporter {

	public static final int TILE_SIZE = 192;
	public static final String TILE_TYPE = "png";

	public static final String USER_HOME = "user.home";
	protected JComboBox tileSize;

	protected JButton browseInput;

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

	JLabel tilingStatus;
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
		this.setPreferredSize(new Dimension(300, 90));
		// content.moveToFront(input);

	}

	public JPanel initInputPanel() {

		JPanel input = new JPanel(new GridBagLayout());

		GridBagConstraints c;
		int y = 0;
		int x = 0;

		JLabel l = new JLabel("Drag and drop image or ");
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.anchor = c.LINE_END;
		input.add(l, c);

		x++;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.anchor = c.LINE_START;
		input.add(browseInput, c);

		FileDropHandler handler = new FileDropHandler(this);

		l.setTransferHandler(handler);
		browseInput.setTransferHandler(handler);
		input.setTransferHandler(handler);

		return input;
	}

	public JPanel initStatusPanel() {
		JPanel status = new JPanel(new GridBagLayout());
		GridBagConstraints c;
		int y = 0;
		int x = 0;
		tilingStatus = new JLabel();

		c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = 3;
		c.weightx = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		status.add(tilingStatus, c);

		x = 4;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(inhibitor);
		status.add(cancel, c);

		y++;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		status.add(progressIndicator, c);

		return status;
	}

	public void resetTilingStatus() {
		tilingStatus.setText("");
		progressIndicator.setEnabled(false);
	}

	public void setTilingStatus(String text, double percent) {
		progressIndicator.setEnabled(true);
		tilingStatus.setText(text);
		progressIndicator.setIndeterminate(true);
		/*if (percent > 0.99 || percent < 0.1) {
			progressIndicator.setIndeterminate(true);
		} else {
			progressIndicator.setIndeterminate(false);
			progressIndicator.setValue((int) (percent * 100));
		}*/
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
						setTilingStatus(
								"Opening " + currentSourcePath
										.substring(currentSourcePath.lastIndexOf(File.separator) + 1) + " ...",
								0.009);
						long start = System.currentTimeMillis();
						Communicator comm = new Communicator(preview);
						creator.calculateTiles(temp.getAbsolutePath(), currentSourcePath, TILE_SIZE, TILE_TYPE,
								TileCreatorPanel.this, TileCreatorApp.ThreadCount, true, inhibitor);
						long end = System.currentTimeMillis();
						System.out
								.println("creation time : " + (end - start) + " ms. == " + ((end - start) / 1000) + "s " + ((end - start) / 1000 / 60) + "min");
						setTilingStatus("Finishing to write the temporary file ...", 0.9999);
						creator.finalizeFile();
						progressIndicator.setEnabled(false);
						if (inhibitor.hasRunInhibitionBeenRequested()) {
							setTilingStatus("Finishing to write the temporary file ...", 0.9999);
							temp.delete();
						} else {
							setTilingStatus("Adding it to the list of shareable TileSets ...", 0.9999);
							sharingPanel.addTileSetToShare(temp.getAbsolutePath(), Ref.fileSansDot(currentSourcePath));
						}
					} catch (Exception ex) {
						if (ex instanceof IIOException) {
							JOptionPane
									.showConfirmDialog(
											TileCreatorPanel.this,
											"<html><body>Could not open the image. <br/>Reason : <i>" + ex.getMessage() + "</i><br/>Possible workaround: <br/>Try saving the image as a PNG or a BMP in another program and then transform that file instead.</body></html>",
											"Error opening the image", JOptionPane.DEFAULT_OPTION,
											JOptionPane.ERROR_MESSAGE);
						} else {
							JOptionPane
									.showConfirmDialog(
											TileCreatorPanel.this,
											"<html><body>Error creating the tile set  : <i>" + ex.getMessage() + "</i></body></html>",
											"Error creating the tile set", JOptionPane.DEFAULT_OPTION,
											JOptionPane.ERROR_MESSAGE);
						}
						ex.printStackTrace();
					}
					resetTilingStatus();
					TileCreatorPanel.this.remove(status);
					TileCreatorPanel.this.add(input, new Integer(0));
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
			int returnVal = sourceChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("You chose to open this file: " + sourceChooser.getSelectedFile().getName());
				try {
					setImageFileToTile(sourceChooser.getSelectedFile());
				} catch (Exception e) {
					// from.setText("Sorry, cannot open the file");
					e.printStackTrace();
				}
			}
		}
	}

	public void setImageFileToTile(File imageFile) throws IOException {
		// from.setText(imageFile.getCanonicalPath());
		// from.setToolTipText("Image Tile set is going to be created from " + imageFile.getCanonicalPath());
		sourceChooser.setSelectedFile(imageFile);
		preTile(imageFile.getCanonicalPath());

	}
}
