/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.actions.Resizer;
import net.niconomicon.tile.source.app.viewer.actions.SingleTileLoader;
import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;
import net.niconomicon.tile.source.app.viewer.structs.ZoomLevel;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableViewer extends JPanel {

	public static String _PLUS = " + ";
	public static String _MINUS = " - ";

	DisplayableView tileViewer;
	DisplayableSource currentSource;
	String displayableLocation;
	JFrame viewerFrame;
	JLabel currentZoom;
	JLabel infos;
	JToolBar toolBar;
	JLabel loadingLabel;
	JProgressBar progress;

	JButton zP;
	JButton zM;

	public static DisplayableViewer createInstance() {
		return new DisplayableViewer(new DisplayableView());
	}

	private DisplayableViewer(DisplayableView tileViewer) {
		super();
		this.tileViewer = tileViewer;
		init();
	}

	private void init() {
		viewerFrame = new JFrame();
		this.setLayout(new BorderLayout());
		JScrollPane sp = new JScrollPane(tileViewer);
		sp.setMinimumSize(new Dimension(340, 340));
		this.add(sp, BorderLayout.CENTER);

		this.setMinimumSize(new Dimension(400, 400));
		viewerFrame.setContentPane(this);
		viewerFrame.setMinimumSize(new Dimension(340, 340));
		viewerFrame.setLocation(400, 200);
		viewerFrame.setSize(340, 500);
		this.setPreferredSize(new Dimension(340, 340));

		toolBar = new JToolBar("Zoom", JToolBar.HORIZONTAL);
		currentZoom = new JLabel();
		infos = new JLabel();

		loadingLabel = new JLabel();
		progress = new JProgressBar();

		JLabel l = new JLabel("Zoom : ");
		toolBar.add(l);
		IconsLoader loader = IconsLoader.getIconsLoader();
		zP = new JButton(loader.ic_zoomIn_24);

		zP.addActionListener(new ZoomAction());
		toolBar.add(zP);

		toolBar.add(currentZoom);

		zM = new JButton(loader.ic_zoomOut_24);
		zM.addActionListener(new ZoomAction());
		toolBar.add(zM);

		JButton b;
		b = new JButton(loader.ic_itouch_24_v);
		b.setToolTipText("Resize window to iphone screen size (vertical : ~ 320x480 pixels)");
		b.addActionListener(new Resizer(viewerFrame, new Dimension(340, 500)));
		toolBar.add(b);
		b = new JButton(loader.ic_itouch_24_h);
		b.setToolTipText("Resize window to iphone screen size (horizontal: ~ 480x320 pixels)");
		b.addActionListener(new Resizer(viewerFrame, new Dimension(500, 340)));
		toolBar.add(b);

		toolBar.add(infos);
		toolBar.add(loadingLabel);
		toolBar.add(progress);

		this.add(toolBar, BorderLayout.NORTH);
	}

	public void setDisplayable(String displayableLocation) {

		if (currentSource != null) {
			currentSource.done();
		}
		infos.setText(" Original size : loading ... ");
		currentZoom.setText(" ? / ? ");
		this.displayableLocation = displayableLocation;
		try {
			String title = SQliteTileCreatorMultithreaded.getTitle(displayableLocation);
			viewerFrame.setTitle(title);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		viewerFrame.pack();
		viewerFrame.setVisible(true);

		Thread t = new Thread(new TileSourceSetter(displayableLocation));
		t.start();
	}

	private class TileSourceSetter implements Runnable {
		String tileSourceLocation;

		public TileSourceSetter(String location) {
			displayableLocation = location;
		}

		public void run() {
			zM.setEnabled(false);
			zP.setEnabled(false);
			loadingLabel.setText("Loading displayable ...");
			progress.setIndeterminate(true);
			currentZoom.setText(" ? / ? ");
			currentSource = new DisplayableSource(displayableLocation, loadingLabel, tileViewer);
			// currentSource.registerView(tileViewer);
			tileViewer.setDisplayable(currentSource);
			currentZoom.setText(" " + (currentSource.getMaxZ() - tileViewer.currentLevel.z) + " / " + currentSource.getMaxZ() + " ");
			ZoomLevel zl = currentSource.getMaxInfo();
			infos.setText(" Original size : " + zl.width + " px * " + zl.height + " px. ");

			tileViewer.revalidate();
			revalidate();
			viewerFrame.pack();
			viewerFrame.setVisible(true);

			progress.setIndeterminate(false);
			progress.setVisible(false);
			loadingLabel.setVisible(false);
			zP.setEnabled(tileViewer.canZoomIn());
			zM.setEnabled(tileViewer.canZoomOut());
		}
	}

	public class ZoomAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(zP)) {
				System.out.println("zoom +");
				tileViewer.incrZ();
			}
			if (e.getSource().equals(zM)) {
				System.out.println("zoom -");
				tileViewer.decrZ();
			}
			zP.setEnabled(tileViewer.canZoomIn());
			zM.setEnabled(tileViewer.canZoomOut());

			currentZoom.setText(" " + (currentSource.getMaxZ() - tileViewer.currentLevel.z) + " / " + currentSource.getMaxZ() + " ");
		}
	}

}
