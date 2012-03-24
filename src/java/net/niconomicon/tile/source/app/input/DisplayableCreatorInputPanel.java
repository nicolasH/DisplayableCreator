package net.niconomicon.tile.source.app.input;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.imageio.IIOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import net.niconomicon.tile.source.app.DisplayableCreatorApp;
import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.filter.FileDropHandler;
import net.niconomicon.tile.source.app.filter.ImageFileFilter;
import net.niconomicon.tile.source.app.sharing.DisplayableSharingPanel;
import net.niconomicon.tile.source.app.tiling.Inhibitor;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.tiling.TilingStatusReporter;
import net.niconomicon.tile.source.app.viewer.TilingPreview;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableCreatorInputPanel extends JPanel implements
		TilingStatusReporter {

	public static final int TILE_SIZE = 192;
	public static final String TILE_TYPE = "png";

	public static final String USER_HOME = "user.home";
	protected JComboBox tileSize;

	JProgressBar progressIndicator;

	protected String place;
	protected String name;

	ImageFileFilter imageFilter;
	FileFilter archiveFilter;

	DisplayableSharingPanel sharingPanel;
	TilingPreview preview;

	SQliteTileCreatorMultithreaded creator;

	File temp;

	protected String currentSourcePath;

	JPanel input;
	JPanel status;

	JLabel tilingStatus;
	Inhibitor inhibitor;

	ImageQueueList queuedImages;
	Thread tilerThread;

	String tilingDone = "isItDone?";
	JScrollPane sp;

	public DisplayableCreatorInputPanel(DisplayableSharingPanel sharingP) {
		super(new GridLayout(0, 1));
		this.sharingPanel = sharingP;
		creator = new SQliteTileCreatorMultithreaded();

		imageFilter = new ImageFileFilter();

		progressIndicator = new JProgressBar(0, 100);
		inhibitor = new Inhibitor(progressIndicator);

		status = initStatusPanel();
		input = initInputPanel();
		queuedImages = new ImageQueueList();
		sp = new JScrollPane(queuedImages);
		sp.setMaximumSize(new Dimension(600, 100));

		sp.setVisible(false);

		this.add(sp);
		this.add(input);
		this.setPreferredSize(new Dimension(300, 200));
		tilerThread = new Thread(new Tiler());
		tilerThread.start();
	}

	public JPanel initInputPanel() {

		JPanel input = new JPanel(new BorderLayout());

		JLabel l = new JLabel("Drag and drop images or displayables here");
		input.add(l, BorderLayout.CENTER);

		FileDropHandler handler = new FileDropHandler(new DragAndDropManager(
				sharingPanel, this));

		l.setTransferHandler(handler);
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
	}

	// this will open and tile the image in a separate thread
	public void preTile(String sourcePath) {
		try {
			// Create temp file.
			temp = File.createTempFile(Ref.fileSansDot(sourcePath) + "_",
					Ref.ext_db);
			// Delete temp file when program exits.
			temp.deleteOnExit();// Apparently survives if renamed before exit.
			currentSourcePath = sourcePath;

			switchPanelsToProgress();

			Thread t = new Thread() {
				public void run() {
					synchronized (tilingDone) {
						try {
							String cName = Ref.fileSansDot(currentSourcePath);
							setTilingStatus("Opening " + cName + " ...", 0.001);
							long start = System.currentTimeMillis();
							// Communicator comm = new Communicator(preview);
							creator.calculateTiles(temp.getAbsolutePath(),
									currentSourcePath, TILE_SIZE, TILE_TYPE,
									DisplayableCreatorInputPanel.this,
									DisplayableCreatorApp.ThreadCount, true,
									inhibitor);
							long end = System.currentTimeMillis();
							System.out.println("creation time : "
									+ (end - start) + " ms. == "
									+ ((end - start) / 1000) + "s "
									+ ((end - start) / 1000 / 60) + "min");
							setTilingStatus(
									"Finishing to write the temporary file ...",
									0.9999);
							creator.finalizeFile();
							progressIndicator.setEnabled(false);
							if (inhibitor.hasRunInhibitionBeenRequested()) {
								setTilingStatus(
										"Cancelling and leaning up ...", 0.9999);
								temp.delete();
							} else {
								setTilingStatus(
										"Adding it to the list of shareable Displayables ...",
										0.9999);
								sharingPanel.addDisplayableToShare(
										temp.getAbsolutePath(),
										Ref.fileSansDot(currentSourcePath));
							}
						} catch (Exception ex) {
							if (ex instanceof IIOException) {
								JOptionPane
										.showConfirmDialog(
												DisplayableCreatorInputPanel.this,
												"<html><body>Could not open the image. <br/>Reason : <i>"
														+ ex.getMessage()
														+ "</i><br/>Possible workaround: <br/>Try saving the image as a PNG or a BMP in another program and then transform that file instead.</body></html>",
												"Error opening the image",
												JOptionPane.DEFAULT_OPTION,
												JOptionPane.ERROR_MESSAGE);
							} else {
								JOptionPane.showConfirmDialog(
										DisplayableCreatorInputPanel.this,
										"<html><body>Error creating the Displayable : <i>"
												+ ex.getMessage()
												+ "</i></body></html>",
										"Error creating the Displayable",
										JOptionPane.DEFAULT_OPTION,
										JOptionPane.ERROR_MESSAGE);
							}
							ex.printStackTrace();
						}
						resetTilingStatus();
						DisplayableCreatorInputPanel.this.switchPanelsToInput();
						tilingDone.notifyAll();
					}
				}
			};
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void queueImageForDisplayableCreation(File image) {
		System.out.println("Adding image:" + image.getAbsolutePath());
		queuedImages.addImage(image);
		if (queuedImages.imagesToTransform.size() > 0) {
			System.out.println("Making it visible");
			sp.setVisible(true);
			this.revalidate();
			this.getLayout().layoutContainer(this);
		}
	}

	private void switchPanelsToProgress() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DisplayableCreatorInputPanel.this.remove(input);
				DisplayableCreatorInputPanel.this.add(status);
			}
		});
	}

	private void switchPanelsToInput() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DisplayableCreatorInputPanel.this.remove(status);
				DisplayableCreatorInputPanel.this.add(input);
			}
		});
	}

	private class Tiler implements Runnable {

		public void run() {
			while (true) {
				File item = queuedImages.removeImage();
				if (queuedImages.imagesToTransform.size() == 0) {
					sp.setVisible(false);
					System.out.println("Making it IN-visible");
					DisplayableCreatorInputPanel.this.revalidate();
					DisplayableCreatorInputPanel.this.getLayout()
							.layoutContainer(DisplayableCreatorInputPanel.this);

				}
				if (item != null) {
					try {
						preTile(item.getCanonicalPath());
						synchronized (tilingDone) {
							tilingDone.wait();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					synchronized (queuedImages.imagesToTransform) {
						try {
							queuedImages.imagesToTransform.wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
