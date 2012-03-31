package net.niconomicon.tile.source.app.tiling.moreParallel;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

import javax.imageio.ImageIO;

import net.niconomicon.tile.source.app.tiling.FastClipper;
import net.niconomicon.tile.source.app.tiling.parallel.TileJobWrite;

public class TileJobClipFlipSaveSerialize implements Runnable {

	Rectangle clip;
	BufferedImage source;
	boolean flip;
	int tileSize;
	String tileType;

	Executor plumber;

	// these two are read by external classes
	public BufferedImage tile;
	public TileJobWrite task;

	// this one is written by external classes
	public TileJobShrink shrink;

	public TileJobClipFlipSaveSerialize(BufferedImage source, Rectangle clip, boolean flip, int tileSize, String tileType, Executor plumber,
			TileJobWrite writeJob) {
		this.clip = clip;
		this.source = source;
		this.flip = flip;
		this.tileType = tileType;
		this.plumber = plumber;
		this.task = writeJob;
		this.tileSize = tileSize;
	}

	public void run() {
		try {

			tile = FastClipper.fastClip(source, clip, flip);
			ByteArrayOutputStream byteStorage = new ByteArrayOutputStream();
			ImageIO.write(tile, tileType, byteStorage);
			task.data = byteStorage;
			plumber.execute(task);
			if (shrink != null) {
				shrink.addTile(this);
			}
		} catch (Exception ex) {
			System.err.println("Error for the cutting of tile at z" + task.z + " x" + task.x + " y" + task.y);
			System.err.println("clip:" + clip);
			ex.printStackTrace();
		}
	}
}
