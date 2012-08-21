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

import net.niconomicon.tile.source.app.viewer.structs.ZoomLevel;

/**
 * @author Nicolas Hoibian A class that represents a view of a Displayable -
 *         Only handles the painting.
 */
public class DisplayableView extends JPanel {

	Connection mapDB;

	List<ZoomLevel> levels;

	Dimension tileSize;
	ZoomLevel currentLevel;

	DisplayableSource displayableSource;

	public DisplayableView() {
		super();
	}

	public void setDisplayable(DisplayableSource source) {
		displayableSource = source;
		tileSize = source.tileSize;
		levels = source.getILevelInfos();
		currentLevel = levels.get(levels.size() - 1);
		revalidate();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (null == displayableSource) { return;/*
												 * Component shown but
												 * displayable not yet set.
												 */}

		// System.out.println("paintComponent");
		Graphics2D g2 = (Graphics2D) g;
		Rectangle r = g2.getClipBounds();
		int tileXa = r.x / tileSize.width;
		int tileXb = tileXa + (int) (((double) r.width / (double) tileSize.width)) + 2;
		int tileYa = r.y / tileSize.height;
		int tileYb = tileYa + (int) (((double) r.height / (double) tileSize.height)) + 1;

		// System.out.println("Painting between " + tileXa + "," + tileYa +
		// "and " + tileXb + ", " + tileYb);
		try {
			int macYb = ((int) currentLevel.tiles_x - 1 - tileYa);
			int macYa = ((int) currentLevel.tiles_y - 1 - tileYb);

			macYa = tileYa;
			macYb = tileYb;
			macYb = Math.min(macYb, (int) currentLevel.tiles_y - 1);
			tileXb = Math.min(tileXb, (int) currentLevel.tiles_x);
			for (int x = tileXa; x < tileXb; x++) {
				for (int y = macYa; y < macYb + 1; y++) {
					BufferedImage tile = displayableSource.getImage(x, y, currentLevel.z);
					if (null != tile) {
						g2.drawImage(tile, x * tileSize.width, (y) * tileSize.height, null);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		g2.dispose();
	}

	public void resetSizeEtc(ZoomLevel zl) {
		System.out.println("Gonna invalidate to zoom level " + zl.z);
		currentLevel = zl;
		this.setSize((int) zl.width, (int) zl.height);
		this.setMinimumSize(new Dimension((int) zl.width, (int) zl.height));
		this.setPreferredSize(new Dimension((int) zl.width, (int) zl.height));

		invalidate();
		// revalidate();
		repaint();

	}

	public boolean canZoomIn() {
		return currentLevel.z > 0;
	}

	public boolean canZoomOut() {
		return currentLevel.z < levels.size() - 1;
	}

	public void incrZ() {
		if (currentLevel.z > 0) {
			ZoomLevel zl = levels.get(currentLevel.z - 1);
			currentLevel = zl;
			resetSizeEtc(zl);
		}
	}

	public void decrZ() {
		if (currentLevel.z < levels.size() - 1) {
			ZoomLevel zl = levels.get(currentLevel.z + 1);
			currentLevel = zl;
			resetSizeEtc(zl);
		}
	}

	public void repaintTile(long x, long y, long z) {
		if (currentLevel.z == z) {
			// System.out.println("repainting " + x + " " + y + " " + z);
			repaint((int) x * tileSize.width, (int) y * tileSize.height, tileSize.width, tileSize.height);
		}
	}

}
