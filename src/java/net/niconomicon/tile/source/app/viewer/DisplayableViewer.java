/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.BorderLayout;
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
		DragListener dl = new DragListener();
		sp.addMouseMotionListener(dl);
		sp.addMouseListener(dl);
		sp.setMinimumSize(new Dimension(340, 340));
		this.add(sp, BorderLayout.CENTER);

		this.setMinimumSize(new Dimension(320, 320));
		viewerFrame.setContentPane(this);
		viewerFrame.setMinimumSize(new Dimension(300, 300));
		viewerFrame.setLocation(400, 200);
		viewerFrame.setSize(340, 500);
		this.setPreferredSize(new Dimension(340, 340));

		toolBar = new JToolBar("Zoom", JToolBar.HORIZONTAL);
		currentZoom = new JLabel();
		infos = new JLabel();

		loadingLabel = new JLabel();
		progress = new JProgressBar();

		IconsLoader loader = IconsLoader.getIconsLoader();

		zM = FontLoader.getButton(FontLoader.iconZoomOut);
		zM.addActionListener(new ZoomAction());
		toolBar.add(zM);

		toolBar.add(currentZoom);

		zP = FontLoader.getButton(FontLoader.iconZoomIn);
		zP.addActionListener(new ZoomAction());
		toolBar.add(zP);

		JButton b;
		// toolBar.addSeparator();
		// b = new JButton(loader.ic_itouch_24_v);
		// b.setToolTipText("Resize window to iphone screen size (vertical : ~ 320x480 pixels)");
		// b.addActionListener(new Resizer(viewerFrame, new Dimension(340,
		// 500)));
		// toolBar.add(b);
		// b = new JButton(loader.ic_itouch_24_h);
		// b.setToolTipText("Resize window to iphone screen size (horizontal: ~ 480x320 pixels)");
		// b.addActionListener(new Resizer(viewerFrame, new Dimension(500,
		// 340)));
		// toolBar.add(b);

		// Buggy when closing the toolbar in full screen mode:(
		// toolBar.addSeparator();
		// b = FontLoader.getButton(FontLoader.iconExpand);
		// b.setToolTipText("Make the Displayable view fullscreen");
		// b.addActionListener(new FullScreenResizer(viewerFrame, b, toolBar));
		// toolBar.add(b);

		// toolBar.add(infos);
		// toolBar.add(loadingLabel);
		// toolBar.add(progress);

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

	public static void main(String[] args) {
		SQLiteDisplayableCreatorParallel.loadLib();
		DisplayableViewer viewer = DisplayableViewer.createInstance();
		viewer.setDisplayable("/Users/niko/displayables/" + "tpg-urbain-a218f603b2fc.disp");

		viewer.viewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	private class DragListener implements MouseMotionListener, MouseListener {
		private Point lastPoint;

		public void mousePressed(MouseEvent e) {
			lastPoint = e.getPoint();
			System.out.println("Last Point" + lastPoint);
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

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseMoved(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}
	}
}
