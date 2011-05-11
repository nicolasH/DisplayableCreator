/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

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

	public static int ThreadCount = 4;

	public DisplayableCreatorApp() {
		ThreadCount = Math.max(Runtime.getRuntime().availableProcessors() * 2, ThreadCount);
		init();
	}

	private void init() {
		JFrame f = new JFrame("Displayable Creator");
		displayableViewer = DisplayableViewer.createInstance();
		tileCreatorPanel = new DisplayableCreatorInputPanel();
		mapSharingPanel = new DisplayableSharingPanel(displayableViewer);
		Font font = new Font(null,Font.BOLD,16);
		Border etch = BorderFactory.createEtchedBorder();
		tileCreatorPanel.setBorder(BorderFactory.createTitledBorder(etch, "Create a Displayable",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,font));
		mapSharingPanel.setBorder(BorderFactory.createTitledBorder(etch, "Share Displayables",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,font));
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
