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
import net.niconomicon.tile.source.app.viewer.DisplayableView.ZoomLevel;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableViewer extends JPanel {

	DisplayableView tileViewer;
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
		this.add(new JScrollPane(tileViewer), BorderLayout.CENTER);

		this.setMinimumSize(new Dimension(400, 400));
		viewerFrame.setContentPane(this);
		viewerFrame.setMinimumSize(new Dimension(400, 400));
		viewerFrame.setLocation(400, 200);
		viewerFrame.setSize(500, 500);
		this.setPreferredSize(new Dimension(500, 500));

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
		System.out.println("setting tile set");
		infos.setText(" Size : ? px * ? px. ");
		currentZoom.setText("Current zoom : ");
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
			loadingLabel.setText("Loading displayable ...");
			progress.setIndeterminate(true);
			currentZoom.setText("Current zoom : ... ");
			tileViewer.setTileSource(displayableLocation, loadingLabel);
			currentZoom.setText("Current zoom : " + (tileViewer.getMaxZ() - tileViewer.currentLevel.z) + " / " + tileViewer.getMaxZ());
			ZoomLevel zl = tileViewer.getMaxInfo();
			infos.setText("Maximum Size : " + zl.width + " px * " + zl.height + " px. ");
			tileViewer.revalidate();
			revalidate();
			viewerFrame.pack();
			viewerFrame.setVisible(true);

			progress.setIndeterminate(false);
			progress.setVisible(false);
			loadingLabel.setVisible(false);

		}
	}

	public class ZoomAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			boolean reached;
			System.out.println("Action command : [" + e.getActionCommand() + "]");
			if (e.getActionCommand().equals("+")) {
				System.out.println("zoom +");
				reached = tileViewer.incrZ();
				if (reached) {
					zP.setEnabled(false);
				} else {
					zP.setEnabled(true);
				}
				zM.setEnabled(true);
			}
			if (e.getActionCommand().equals("-")) {
				System.out.println("zoom -");
				reached = tileViewer.decrZ();
				if (reached) {
					zM.setEnabled(false);
				} else {
					zM.setEnabled(true);
				}
				zP.setEnabled(true);
			}
			currentZoom.setText("Current zoom : " + (tileViewer.getMaxZ() - tileViewer.currentLevel.z) + "/" + tileViewer.getMaxZ());
		}
	}

}
