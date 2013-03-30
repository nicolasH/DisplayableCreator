/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.niconomicon.tile.source.app.help.HelpWidget;
import net.niconomicon.tile.source.app.input.DisplayableCreatorInputPanel;
import net.niconomicon.tile.source.app.input.QueueListView;
import net.niconomicon.tile.source.app.sharing.DisplayableSharingWidget;
import net.niconomicon.tile.source.app.sharing.DisplayableSharingWidget.DA;
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
		ThreadCount = Math.max(Runtime.getRuntime().availableProcessors() + 1, ThreadCount);
		init();
	}

	private void init() {
		frame = new JFrame("Displayable Creator");
		displayableViewer = DisplayableViewer.createInstance();
		QueueListView queue = new QueueListView(displayableViewer);
		sharingWidget = new DisplayableSharingWidget(queue);
		tileCreatorPanel = new DisplayableCreatorInputPanel(queue, sharingWidget);

		// Font font = new Font(null, Font.BOLD, 16);
		// Border etch = BorderFactory.createEtchedBorder();
		// tileCreatorPanel.setBorder(BorderFactory.createTitledBorder(etch,
		// "Create a Displayable", TitledBorder.DEFAULT_JUSTIFICATION,
		// TitledBorder.DEFAULT_POSITION, font));

		JPanel p = new JPanel(new BorderLayout());
		p.add(tileCreatorPanel, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.setMaximumSize(new Dimension(100, 50));

		bottom.add(sharingWidget.getSharingButton());
		bottom.add(tileCreatorPanel.getListButton());
		bottom.add(AppPreferences.getPreferences().getPreferencesButton());
		// bottom.add(sharingWidget.getExportButton());
		bottom.add(HelpWidget.createHelpWidget());
		JPanel south = new JPanel(new BorderLayout());
		south.add(bottom, BorderLayout.CENTER);
		south.add(sharingWidget.getAddressComponent(), BorderLayout.SOUTH);
		p.add(south, BorderLayout.SOUTH);
		frame.setContentPane(p);
		frame.pack();

		frame.setSize(300, 350);
		frame.setMinimumSize(new Dimension(250, 200));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Thread t = new Thread(new Runnable() {
			public void run() {
				SQliteTileCreatorMultithreaded.loadLib();
			}
		});
		t.start();

		if (AppPreferences.getPreferences().getAutostart()) {
			sharingWidget.switchSharing(DA.ACTIVATE);
		}
		if (Ref.getCheckForUpdates()) {
			UpdateChecker.checkForUpdate(p, false, false);
		}
		Thread shutdownThread = new Thread(new Runnable() {
			public void run() {
				frame.setVisible(false);
				sharingWidget.switchSharing(DA.DEACTIVATE);
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	public static void main(String[] args) {
		try {
			// if os == windows
			// String motif = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			// // to 40^2 is too small for the 24pt icon on main screen
			String gtk = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			// String metal ="javax.swing.plaf.metal.MetalLookAndFeel"; // to
			// 40^2 is too small for the 24pt icon on main screen
			String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			String osx = "com.apple.laf.AquaLookAndFeel";
			// String lnf_id_metal = "Metal";
			// String lnf_id_osx = "Acqua";
			// String lnf_id_gtk = "GTK"; // ok. font in the list look too big
			// String lnf_id_windows = "";
			// String lnf_id_motif = "Motif";

			// UIManager.setLookAndFeel(motif);
			// UIManager.setLookAndFeel(gtk);
			// UIManager.setLookAndFeel(metal); // buttons too small
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				UIManager.setLookAndFeel(windows);
			}
			if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
				UIManager.setLookAndFeel(osx);
			}
			if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
				UIManager.setLookAndFeel(osx);
			}

			// System.out.println("UI look and feel:" +
			// UIManager.getLookAndFeel().getID());
			// System.out.println("UI look and feel:" +
			// UIManager.getLookAndFeel().getName());
			// System.out.println("UI look and feel:" +
			// UIManager.getLookAndFeel().getClass());

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		DisplayableCreatorApp app = new DisplayableCreatorApp();
	}
}
