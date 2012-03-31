/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.niconomicon.tile.source.app.input.DisplayableCreatorInputPanel;
import net.niconomicon.tile.source.app.sharing.DisplayableSharingPanel;
import net.niconomicon.tile.source.app.tiling.SQLiteDisplayableCreatorParallel;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;

/**
 * @author Nicolas Hoibian This is the Main class for the Displayable Creator
 *         GUI.
 */
public class DisplayableCreatorApp {

	DisplayableCreatorInputPanel tileCreatorPanel;
	DisplayableSharingPanel mapSharingPanel;
	DisplayableViewer displayableViewer;

	public static int ThreadCount = 4;

	public DisplayableCreatorApp() {
		ThreadCount = Math.max(Runtime.getRuntime().availableProcessors() * 2,
				ThreadCount);
		init();
	}

	private void init() {
		JFrame f = new JFrame("Displayable Creator");
		displayableViewer = DisplayableViewer.createInstance();
		mapSharingPanel = new DisplayableSharingPanel(displayableViewer);
		tileCreatorPanel = new DisplayableCreatorInputPanel(mapSharingPanel);
		Font font = new Font(null, Font.BOLD, 16);
		Border etch = BorderFactory.createEtchedBorder();
		tileCreatorPanel.setBorder(BorderFactory.createTitledBorder(etch,
				"Create a Displayable", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, font));
		mapSharingPanel.setBorder(BorderFactory.createTitledBorder(etch,
				"Share Displayables", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, font));
		
		GridBagConstraints c_top = new GridBagConstraints();
		c_top.gridx = 0;
		c_top.gridy = 0;
		c_top.gridwidth = 1;
		c_top.gridheight =3;
		c_top.fill = GridBagConstraints.BOTH;
		
		GridBagConstraints c_bottom = new GridBagConstraints();
		c_bottom.gridx = 0;
		c_bottom.gridy = 4;
		c_bottom.gridwidth = 1;
		c_bottom.gridheight =5;
		c_bottom.fill = GridBagConstraints.BOTH;
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(tileCreatorPanel, BorderLayout.NORTH);
		p.add(mapSharingPanel, BorderLayout.CENTER);
		f.setContentPane(p);
		f.pack();
		f.setSize(350, 500);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Thread t = new Thread(new Runnable() {
			public void run() {
				SQliteTileCreatorMultithreaded.loadLib();
			}
		});
		t.start();
		Thread shutdownThread = new Thread(new Runnable() {
			public void run() {
				mapSharingPanel.stopSharing();
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	public static void main(String[] args) {
		DisplayableCreatorApp app = new DisplayableCreatorApp();
	}
}
