/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.niconomicon.tile.source.app.sharing.MapSharingPanel;
import net.niconomicon.tile.source.app.viewer.ImageTileSetViewer;

/**
 * @author niko
 * 
 */
public class TileCreatorApp {

	TileCreatorPanel tileCreatorPanel;
	MapSharingPanel mapSharingPanel;
	ImageTileSetViewer tileSetViewerPanel;

	public TileCreatorApp() {
		init();
	}

	private void init() {
		JFrame f = new JFrame("Image Cutter App");
		tileCreatorPanel = new TileCreatorPanel();
		mapSharingPanel = new MapSharingPanel();
		tileSetViewerPanel = ImageTileSetViewer.createInstance();

		tileCreatorPanel.setBorder(BorderFactory.createTitledBorder("Create Tile Set"));
		mapSharingPanel.setBorder(BorderFactory.createTitledBorder("Share Tile Sets"));
		JPanel p = new JPanel(new BorderLayout());
		p.add(tileCreatorPanel, BorderLayout.NORTH);
		p.add(mapSharingPanel, BorderLayout.CENTER);
		tileCreatorPanel.setSharingService(mapSharingPanel);
		mapSharingPanel.setViewer(tileSetViewerPanel);
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SQliteTileCreatorMultithreaded.loadLib();
	}

	public static void main(String[] args) {
		TileCreatorApp app = new TileCreatorApp();
	}
}
