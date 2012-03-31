package net.niconomicon.tile.source.app.tiling.parallel;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

import javax.imageio.ImageIO;

import net.niconomicon.tile.source.app.tiling.FastClipper;
import net.niconomicon.tile.source.app.tiling.original.TileWritingJob;

public class TileJobClipFlipSerialize implements Runnable {

	Rectangle clip;
	BufferedImage source;
	boolean flip;
	int tileSize;
	String tileType;

	Executor plumber;
	TileJobWrite task;

	public TileJobClipFlipSerialize(BufferedImage source, Rectangle clip,
			boolean flip, int tileSize, String tileType, Executor plumber,
			TileJobWrite writeJob) {
		this.clip = clip;
		this.source = source;
		this.flip = flip;
		this.tileType = tileType;
		this.plumber = plumber;
		this.task = writeJob;
	}

	public void run() {
		try {
			BufferedImage tile = FastClipper.fastClip(source, clip, flip);
			long start = System.nanoTime();
			ByteArrayOutputStream byteStorage = new ByteArrayOutputStream();
			ImageIO.write(tile, tileType, byteStorage);
			task.data = byteStorage;
			plumber.execute(task);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
