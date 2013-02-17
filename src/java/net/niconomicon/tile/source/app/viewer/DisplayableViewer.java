/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import net.niconomicon.tile.source.app.fonts.FontLoader;
import net.niconomicon.tile.source.app.tiling.SQLiteDisplayableCreatorParallel;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;
import net.niconomicon.tile.source.app.viewer.structs.ZoomLevel;

/**
 * @author Nicolas Hoibian A class used to view a Displayable - Handles the
 *         scrolling, zooming and other user interactions.
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
		tileViewer.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		DragListener dl = new DragListener();
		sp.addMouseMotionListener(dl);
		sp.addMouseListener(dl);
		sp.setMinimumSize(new Dimension(340, 340));
		this.add(sp, BorderLayout.CENTER);

		this.setMinimumSize(new Dimension(500, 320));
		viewerFrame.setContentPane(this);
		viewerFrame.setMinimumSize(new Dimension(500, 300));
		viewerFrame.setLocation(400, 200);
		viewerFrame.setSize(600, 400);
		this.setPreferredSize(new Dimension(600, 400));

		toolBar = new JToolBar("Zoom", JToolBar.HORIZONTAL);
		currentZoom = new JLabel();
		infos = new JLabel();

		loadingLabel = new JLabel();
		progress = new JProgressBar();

		IconsLoader loader = IconsLoader.getIconsLoader();

		zM = FontLoader.getButton(FontLoader.iconZoomOut);
		zM.setMaximumSize(new Dimension(40, 40));
		zM.addActionListener(new ZoomAction());
		toolBar.add(zM);

		zP = FontLoader.getButton(FontLoader.iconZoomIn);
		zP.addActionListener(new ZoomAction());
		zP.setMaximumSize(new Dimension(40, 40));
		toolBar.add(zP);

		JButton b;

		toolBar.setOrientation(JToolBar.VERTICAL);
		this.add(toolBar, BorderLayout.WEST);
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

	protected void updateTitle() {
		ZoomLevel zl = currentSource.getMaxInfo();
		float megapixels = (zl.width * zl.height) / 100000;
		megapixels = Math.round(megapixels) / 10;
		String title = currentSource.getTitle() + " \u2014 " + zl.width + "x" + zl.height + " (" + megapixels + " Mpx)";
		title += " \u2014 Zoom: " + (currentSource.getMaxZ() - tileViewer.currentLevel.z) + "/" + currentSource.getMaxZ() + " ";
		viewerFrame.setTitle(title);
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
			// currentZoom.setText(" ? / ? ");
			currentSource = new DisplayableSource(displayableLocation, loadingLabel, tileViewer);
			// currentSource.registerView(tileViewer);
			tileViewer.setDisplayable(currentSource);
			// currentZoom.setText(" " + (currentSource.getMaxZ() -
			// tileViewer.currentLevel.z) + " / " + currentSource.getMaxZ() +
			// " ");
			ZoomLevel zl = currentSource.getMaxInfo();
			updateTitle();
			// infos.setText(" Original size : " + zl.width + " px * " +
			// zl.height + " px. ");

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
				tileViewer.incrZ();
			}
			if (e.getSource().equals(zM)) {
				tileViewer.decrZ();
			}
			zP.setEnabled(tileViewer.canZoomIn());
			zM.setEnabled(tileViewer.canZoomOut());
			updateTitle();
			// currentZoom.setText(" " + (currentSource.getMaxZ() -
			// tileViewer.currentLevel.z) + " / " + currentSource.getMaxZ() +
			// " ");
		}
	}

	private class DragListener implements MouseMotionListener, MouseListener {
		private Point lastPoint;

		public void mousePressed(MouseEvent e) {
			lastPoint = e.getPoint();
			// System.out.println("Last Point" + lastPoint);
		}

		public void mouseReleased(MouseEvent e) {
			lastPoint = null;
		}

		public void mouseDragged(MouseEvent e) {
			if (lastPoint != null) {
				Point delta = new Point(lastPoint.x - e.getPoint().x, lastPoint.y - e.getPoint().y);
				Rectangle current = tileViewer.getVisibleRect();
				tileViewer.scrollRectToVisible(new Rectangle(current.x + delta.x, current.y + delta.y, current.width, current.height));
				lastPoint = e.getPoint();
			}
		}

		public void mouseClicked(MouseEvent e) {}

		public void mouseMoved(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}
	}

	public static void main(String[] args) {
		SQLiteDisplayableCreatorParallel.loadLib();
		DisplayableViewer viewer = DisplayableViewer.createInstance();
		viewer.setDisplayable("/Users/niko/TileSources/displayables/" + "tpg-plan-urbain-mai-2012-300-dpi.disp");
		viewer.viewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
