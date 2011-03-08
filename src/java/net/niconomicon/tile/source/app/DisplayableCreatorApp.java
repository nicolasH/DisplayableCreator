/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.niconomicon.tile.source.app.sharing.DisplayableSharingPanel;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;

/**
 * @author niko
 * 
 */
public class DisplayableCreatorApp {

	DisplayableCreatorInputPanel tileCreatorPanel;
	DisplayableSharingPanel mapSharingPanel;
	DisplayableViewer displayableViewer;

	public static int ThreadCount = 8;

	public DisplayableCreatorApp() {
		init();
	}

	private void init() {
		JFrame f = new JFrame("Displayable Creator");
		displayableViewer = DisplayableViewer.createInstance();
		tileCreatorPanel = new DisplayableCreatorInputPanel();
		mapSharingPanel = new DisplayableSharingPanel(displayableViewer);

		tileCreatorPanel.setBorder(BorderFactory.createTitledBorder("Create a Displayable"));
		mapSharingPanel.setBorder(BorderFactory.createTitledBorder("Share Displayables"));
		JPanel p = new JPanel(new BorderLayout());
		p.add(tileCreatorPanel, BorderLayout.NORTH);
		p.add(mapSharingPanel, BorderLayout.CENTER);
		tileCreatorPanel.setSharingService(mapSharingPanel);
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Thread t = new Thread(new Runnable() {
			public void run() {
				SQliteTileCreatorMultithreaded.loadLib();
			}
		});
		t.start();
	}

	public static void main(String[] args) {
		DisplayableCreatorApp app = new DisplayableCreatorApp();
	}
}
