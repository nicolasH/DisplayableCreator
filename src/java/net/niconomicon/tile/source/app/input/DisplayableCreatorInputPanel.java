package net.niconomicon.tile.source.app.input;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.imageio.IIOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;

import net.niconomicon.tile.source.app.DisplayableCreatorApp;
import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.filter.FileDropHandler;
import net.niconomicon.tile.source.app.filter.ImageFileFilter;
import net.niconomicon.tile.source.app.tiling.SQLiteDisplayableCreatorMoreParallel;
import net.niconomicon.tile.source.app.tiling.TilingStatusReporter;
import net.niconomicon.tile.source.app.viewer.TilingPreview;
import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableCreatorInputPanel extends JPanel implements TilingStatusReporter {

	public static final int TILE_SIZE = 256;
	public static final String TILE_TYPE = "png";

	public static final String USER_HOME = "user.home";
	protected JComboBox tileSize;

	protected String place;
	protected String name;

	ImageFileFilter imageFilter;
	FileFilter archiveFilter;

	TilingPreview preview;

	SQLiteDisplayableCreatorMoreParallel creator;

	File temp;
	// protected String currentSourcePath;
	QueueListItem currentItem;

	JPanel input;
	JPanel status;

	QueueListView queueListView;
	JFrame queueFrame;

	Thread tilerThread;

	String tilingDone = "isItDone?";

	public DisplayableCreatorInputPanel(QueueListView queueListView) {
		super(new BorderLayout());
		creator = new SQLiteDisplayableCreatorMoreParallel();

		imageFilter = new ImageFileFilter();

		this.queueListView = queueListView;

		queueFrame = new JFrame("List");
		JScrollPane sp = new JScrollPane(queueListView);
		sp.setMinimumSize(new Dimension(QueueListItem.minWidth, 0));
		queueFrame.setContentPane(sp);
		queueFrame.setTitle("Image queue & Displayable(s) list");

		Dimension dim = new Dimension(QueueListItem.minWidth, QueueListItem.minHeight * 4);
		queueFrame.setPreferredSize(dim);
		queueFrame.setSize(dim);
		queueFrame.setMinimumSize(new Dimension(dim.width, QueueListItem.minHeight));
		queueFrame.pack();
		queueFrame.setLocation(400, 100);

		// this.add(sp, BorderLayout.CENTER);
		initInputPanel();
		this.setPreferredSize(new Dimension(300, 200));
		tilerThread = new Thread(new Tiler());
		tilerThread.start();

	}

	public void initInputPanel() {

		JLabel dragndropText = new JLabel("<html><body>Drop images here</body></html>",SwingConstants.CENTER);		
//		JLabel dragndropText = new JLabel("Drag & drop images here",SwingConstants.CENTER);		
//		dragndropText.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		dragndropText.setForeground(Color.DARK_GRAY);
		dragndropText.setFont(dragndropText.getFont().deriveFont(Font.ITALIC,24f));
		dragndropText.setAlignmentX(CENTER_ALIGNMENT);
		dragndropText.setAlignmentY(CENTER_ALIGNMENT);
		
		this.add(dragndropText, BorderLayout.CENTER);
		// JLabel image = new JLabel();
		// this.add(image, BorderLayout.NORTH);
		JPanel bottom = new JPanel();
		JButton showList = new JButton(IconsLoader.getIconsLoader().ic_list_24);
		showList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				queueFrame.setVisible(true);
			}
		});
		
		JButton showPrefs = new JButton(IconsLoader.getIconsLoader().ic_settings_24);
		JToggleButton sharing = new JToggleButton(IconsLoader.getIconsLoader().ic_sharingOff_24);
		sharing.setBackground(Color.orange);
		bottom.add(showList);
		bottom.add(showPrefs);
		bottom.add(sharing);
		
		this.add(bottom, BorderLayout.SOUTH);
		FileDropHandler handler = new FileDropHandler(new DragAndDropManager(this));

		dragndropText.setTransferHandler(handler);
		this.setTransferHandler(handler);
		dragndropText.setMinimumSize(new Dimension(150, 50));
		dragndropText.setPreferredSize(new Dimension(150, 50));
		dragndropText.setMaximumSize(new Dimension(150, 50));

	}

	public void resetTilingStatus() {
		// progressIndicator.setEnabled(false);
	}

	public void setTilingStatus(String text, double percent) {
		// progressIndicator.setEnabled(true);
		// progressIndicator.setIndeterminate(true);
	}

	// this will open and tile the image in a separate thread
	public void preTile(QueueListItem item) {
		try {
			Thread t = new Thread(new TileImageRunnable(item));
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public class TileImageRunnable implements Runnable {

		QueueListItem it;

		public TileImageRunnable(QueueListItem it) {
			this.it = it;
		}

		public void run() {
			synchronized (tilingDone) {
				try {
					// Create temp file.

					String cName = Ref.fileSansDot(it.getFullPath());
					temp = File.createTempFile(cName + "_", Ref.ext_db);
					// Delete temp file when program exits.
					temp.deleteOnExit();// Apparently survives if renamed before
										// exit.
					String currentSourcePath = it.getFullPath();
					it.arrangeStatusPanel();
					long start = System.currentTimeMillis();

					creator.calculateTiles(temp.getAbsolutePath(), currentSourcePath, TILE_SIZE, TILE_TYPE, DisplayableCreatorInputPanel.this,
							DisplayableCreatorApp.ThreadCount, true, it);
					long end = System.currentTimeMillis();
					System.out.println("creation time : " + (end - start) + " ms. == " + ((end - start) / 1000) + "s " + ((end - start) / 1000 / 60)
							+ "min");

					creator.finalizeFile();
					if (it.hasRunInhibitionBeenRequested()) {
						setTilingStatus("Cancelling and leaning up ...", 0.9999);
						temp.delete();
					} else {
						System.err.println("Should inform someone that there is a new displayable in town.");
						it.arrangeDisplayablePanel(temp);
						synchronized (queueListView.getDisplayablesLock()) {
							queueListView.getDisplayablesLock().notifyAll();
						}
						queueFrame.setVisible(true);
						queueFrame.requestFocus();
					}
				} catch (Exception ex) {
					if (ex instanceof IIOException) {
						JOptionPane
								.showConfirmDialog(
										DisplayableCreatorInputPanel.this,
										"<html><body>Could not open the image. <br/>Reason : <i>"
												+ ex.getMessage()
												+ "</i><br/>Possible workaround: <br/>Try saving the image as a PNG or a BMP in another program and then transform that file instead.</body></html>",
										"Error opening the image", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showConfirmDialog(DisplayableCreatorInputPanel.this,
								"<html><body>Error creating the Displayable : <i>" + ex.getMessage() + "</i></body></html>",
								"Error creating the Displayable", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					}
					ex.printStackTrace();
				}
				tilingDone.notifyAll();
			}
		}

	}

	public void addFile(File image) {
		QueueListItem it = new QueueListItem(image, queueListView);
		queueListView.addItem(it);
		queueFrame.setVisible(true);
		queueFrame.requestFocus();
	}

	private class Tiler implements Runnable {

		public void run() {
			while (true) {
				QueueListItem item = queueListView.getNextItem();
				System.out.println("Got item!" + item);
				if (item != null) {
					try {
						preTile(item);
						synchronized (tilingDone) {
							tilingDone.wait();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					synchronized (queueListView.queue) {
						try {
							queueListView.queue.wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
