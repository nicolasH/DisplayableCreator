/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;

/**
 * @author Nicolas Hoibian
 * This class transforms a BufferedImage into a png image and send it to a persister.
 */
public class TileSerializeJob implements Runnable {

	public static final String tileType = "png";
	long x;
	long y;
	long z;
	BufferedImage tile;
	// ConcurrentLinkedQueue<TileWritingJob> tilesToWrite;
	Connection conn;
	PreparedStatement insertTile;
	ExecutorService executor;

	public TileSerializeJob(long x, long y, long z, BufferedImage bu, ExecutorService executor, Connection co, PreparedStatement st) {
		this.x = x;
		this.y = y;
		this.z = z;
		tile = bu;
		this.executor = executor;
		this.conn = co;
		this.insertTile = st;
		// this.tilesToWrite = queue;

	}

	public void run() {

		try {
			long start = System.nanoTime();
			ByteArrayOutputStream byteStorage = new ByteArrayOutputStream();

			ImageIO.write(tile, tileType, byteStorage);

			long stop = System.nanoTime();
			// System.out.println("serializing_tile: " + ((double) (stop - start) / 1000000) + " ms");
			TileWritingJob jobW = new TileWritingJob(x, y, z, byteStorage, conn, insertTile);
			// tilesToWrite.add(jobW);
			executor.execute(jobW);
			// System.out.println("Creating job "+x + ","+y + "," +z);

		} catch (IOException ex) {

			System.out.println("Error while trying to save " + x + "_" + y + "_" + z);

		}
	}
}
