/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import net.niconomicon.tile.source.app.viewer.structs.ZoomLevel;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableViewer extends JPanel {

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

		zP = new JButton("+");
		zP.addActionListener(new ZoomAction());
		toolBar.add(zP);
		zM = new JButton("-");
		zM.addActionListener(new ZoomAction());
		toolBar.add(zM);
		toolBar.add(infos);
		toolBar.add(currentZoom);
		toolBar.add(loadingLabel);
		toolBar.add(progress);
		this.add(toolBar, BorderLayout.NORTH);
	}

	public void setDisplayable(String displayableLocation) {

		if (currentSource != null) {
			currentSource.done();
		}
		infos.setText(" Original size : loading ... ");
		currentZoom.setText(" Current zoom level : ");
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
			currentZoom.setText("Current zoom : ... ");
			currentSource = new DisplayableSource(displayableLocation, loadingLabel, tileViewer);
			// currentSource.registerView(tileViewer);
			tileViewer.setDisplayable(currentSource);
			currentZoom.setText("Current zoom : " + (currentSource.getMaxZ() - tileViewer.currentLevel.z) + " / " + currentSource.getMaxZ());
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
			if (e.getActionCommand().equals("+")) {
				System.out.println("zoom +");
				tileViewer.incrZ();
			}
			if (e.getActionCommand().equals("-")) {
				System.out.println("zoom -");
				tileViewer.decrZ();
			}
			zP.setEnabled(tileViewer.canZoomIn());
			zM.setEnabled(tileViewer.canZoomOut());

			currentZoom.setText("Current zoom : " + (currentSource.getMaxZ() - tileViewer.currentLevel.z) + "/" + currentSource.getMaxZ());
		}
	}

}
