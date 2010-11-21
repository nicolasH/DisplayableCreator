/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * @author Nicolas Hoibian
 * 
 */
public class ImageTileSetViewer extends JScrollPane {

	ImageTileSetPanel tileViewer;
	String tileSetLocation;
	JFrame viewerFrame;

	public static ImageTileSetViewer createInstance() {
		return new ImageTileSetViewer(new ImageTileSetPanel());
	}

	private ImageTileSetViewer(ImageTileSetPanel tileViewer) {
		super(tileViewer);
		this.tileViewer = tileViewer;
		init();
	}

	private void init() {
		viewerFrame = new JFrame();
		// JPanel c = new JPanel(new BorderLayout());
		// c.add(tileSetViewerPanel, BorderLayout.CENTER);
		this.setMinimumSize(new Dimension(400, 400));
		viewerFrame.setContentPane(this);
		viewerFrame.setMinimumSize(new Dimension(400, 400));
		viewerFrame.setLocation(400, 200);
		viewerFrame.setSize(500, 500);
		this.setPreferredSize(new Dimension(500, 500));
	}

	public void setTileSet(String tileSetLocation) {
		System.out.println("setting tile set");
		this.tileSetLocation = tileSetLocation;
		viewerFrame.pack();
		viewerFrame.setVisible(true);

		Thread t = new Thread(new TileSourceSetter(tileSetLocation));
		t.start();
	}

	private class TileSourceSetter implements Runnable {
		String tileSourceLocation;

		public TileSourceSetter(String location) {
			tileSetLocation = location;
		}

		public void run() {
			tileViewer.setTileSource(tileSetLocation);
			tileViewer.revalidate();
			revalidate();
			viewerFrame.pack();
			viewerFrame.setVisible(true);

		}
	}

}
