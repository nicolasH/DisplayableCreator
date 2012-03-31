package net.niconomicon.tile.source.app.tiling.moreParallel;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import net.niconomicon.tile.source.app.tiling.FastClipper;

public class TileJobShrink implements Runnable {

	Set<TileJobClipFlipSaveSerialize> tiles;
	int x;
	int y;
	long z;
	int tileSize;
	public int subTiles;
	Executor executor;
	int imageType;
	public BufferedImage finalTile;

	public TileJobShrink(int x, int y, long z, Executor exe, int tileSize, Map storage) {
		this.z = z;
		this.x = x;
		this.y = y;
		this.tileSize = tileSize;
		tiles = new HashSet<TileJobClipFlipSaveSerialize>();
	}

	public void addTile(TileJobClipFlipSaveSerialize job) {
		tiles.add(job);
		imageType = job.tile.getType();
		if (tiles.size() == subTiles) {
			run();
		}
	}

	public void run() {
		BufferedImage buffer = new BufferedImage(tileSize * 2, tileSize * 2, imageType);
		// now to paste the tiles on the canvas:
		for (TileJobClipFlipSaveSerialize info : tiles) {
//			System.out.println("z:"+info.task.z+" t x:"+info.task.x+" t y:"+info.task.y+" - x:"+ x +" y:"+ y);
			int localX = ((int)info.task.x - 2 * x)* tileSize;
			int localY = ((int)info.task.y - 2 * y) * tileSize;
//			System.out.println("paste x:"+localX + " y:"+localY);
			try {
				FastClipper.fastPaste(info.tile, buffer, localX, localY, info.flip);
			} catch (Exception e) {
				e.printStackTrace();
			}
			info.tile = null;
		}
		tiles.clear();
		Image img = buffer.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH);
		finalTile = new BufferedImage(tileSize, tileSize, imageType);
		Graphics2D g = finalTile.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
	}
}
