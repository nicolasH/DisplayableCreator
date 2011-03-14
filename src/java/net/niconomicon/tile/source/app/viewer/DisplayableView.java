/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.List;

import javax.swing.JPanel;

import net.niconomicon.tile.source.app.viewer.trivia.ZoomLevel;

/**
 * @author niko
 * 
 */
public class DisplayableView extends JPanel {

	Connection mapDB;
	public static final int tileSize = 192;

	List<ZoomLevel> levels;

	ZoomLevel currentLevel;

	DisplayableSource displayableSource;

	public DisplayableView() {
		super();

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setDisplayable(DisplayableSource source) {
		displayableSource = source;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// System.out.println("paintComponent");
		Graphics2D g2 = (Graphics2D) g;
		Rectangle r = g2.getClipBounds();
		int tileXa = r.x / tileSize;
		int tileXb = tileXa + (int) (((double) r.width / (double) tileSize)) + 2;
		int tileYa = r.y / tileSize;
		int tileYb = tileYa + (int) (((double) r.height / (double) tileSize)) + 1;

		// System.out.println("Painting between " + tileXa + "," + tileYa + "and " + tileXb + ", " + tileYb);
		try {
			int macYb = ((int) currentLevel.tiles_x - 1 - tileYa);
			int macYa = ((int) currentLevel.tiles_y - 1 - tileYb);

			macYa = tileYa;
			macYb = tileYb;
			for (int x = tileXa; x < tileXb; x++) {
				for (int y = macYa; y < macYb + 1; y++) {
					BufferedImage tile = displayableSource.getImage(x, y, currentLevel.z);
					if (null != tile) {
						g2.drawImage(tile, x * tileSize, (y) * tileSize, null);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		g2.dispose();
	}

	public void resetSizeEtc(ZoomLevel zl) {
		System.out.println("Gonna invalidate to zoom level " + zl);
		this.setSize((int) zl.width, (int) zl.height);
		this.setMinimumSize(new Dimension((int) zl.width, (int) zl.height));
		this.setPreferredSize(new Dimension((int) zl.width, (int) zl.height));

		invalidate();
		// revalidate();
		repaint();

	}

	/**
	 * 
	 * @return true if reached the max Zoom;
	 */
	public boolean incrZ() {
		if (currentLevel.z > 0) {
			ZoomLevel zl = levels.get(currentLevel.z - 1);
			currentLevel = zl;
			resetSizeEtc(zl);
			if (zl.z == 0) { return true; }
		} else {
			System.out.println("Already at max Zoom");
			return true;
		}
		return false;
	}

	public void repaintTile(long x, long y, long z) {
		if (currentLevel.z == z) {
			repaint((int) x * tileSize, (int) y * tileSize, tileSize, tileSize);
		}
	}

	/**
	 * 
	 * @return true if reached the min zoom
	 */
	public boolean decrZ() {
		if (currentLevel.z < levels.size() - 1) {
			ZoomLevel zl = levels.get(currentLevel.z + 1);
			currentLevel = zl;
			resetSizeEtc(zl);
			if (zl.z == levels.size() - 1) { return true; }
		} else {
			System.out.println("Already at min Zoom");
			return true;
		}
		return false;
	}

	public int getMaxZ() {
		return levels.size();
	}

	public ZoomLevel getMaxInfo() {
		return levels.get(0);
	}

}
