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
import net.niconomicon.tile.source.app.input.QueueListView;
import net.niconomicon.tile.source.app.sharing.DisplayableSharingWidget;
import net.niconomicon.tile.source.app.sharing.DisplayableSharingWidget.DA;
import net.niconomicon.tile.source.app.sharing.SharingWidget;
import net.niconomicon.tile.source.app.tiling.SQLiteDisplayableCreatorParallel;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;

/**
 * @author Nicolas Hoibian This is the Main class for the Displayable Creator
 *         GUI.
 */
public class DisplayableCreatorApp {

	DisplayableCreatorInputPanel tileCreatorPanel;
	DisplayableSharingWidget sharingWidget;
	DisplayableViewer displayableViewer;

	JFrame frame;
	public static int ThreadCount = 4;

	public DisplayableCreatorApp() {
		ThreadCount = Math.max(Runtime.getRuntime().availableProcessors() * 2, ThreadCount);
		init();
	}

	private void init() {
		frame = new JFrame("Displayable Creator");
		displayableViewer = DisplayableViewer.createInstance();
		QueueListView queue = new QueueListView(displayableViewer);
		sharingWidget = new DisplayableSharingWidget(queue);
		tileCreatorPanel = new DisplayableCreatorInputPanel(queue, sharingWidget);

		Font font = new Font(null, Font.BOLD, 16);
		Border etch = BorderFactory.createEtchedBorder();
		tileCreatorPanel.setBorder(BorderFactory.createTitledBorder(etch, "Create a Displayable", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, font));

		// JPanel p = new JPanel(new BorderLayout());
		// p.add(tileCreatorPanel, BorderLayout.CENTER);
		frame.setContentPane(tileCreatorPanel);
		frame.pack();

		frame.setSize(300, 350);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Thread t = new Thread(new Runnable() {
			public void run() {
				SQliteTileCreatorMultithreaded.loadLib();
			}
		});
		t.start();
		Thread shutdownThread = new Thread(new Runnable() {
			public void run() {
				sharingWidget.switchSharing(DA.DEACTIVATE);
				frame.setVisible(false);
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		sharingWidget.switchSharing(DA.ACTIVATE);
	}

	public static void main(String[] args) {
		DisplayableCreatorApp app = new DisplayableCreatorApp();
	}
}
