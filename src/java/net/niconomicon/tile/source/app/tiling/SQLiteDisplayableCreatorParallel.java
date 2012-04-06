package net.niconomicon.tile.source.app.tiling;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.tiling.parallel.TileJobClipFlipSerialize;
import net.niconomicon.tile.source.app.tiling.parallel.TileJobWrite;

public class SQLiteDisplayableCreatorParallel extends SQliteTileCreatorMultithreaded {

	public void calculateTiles(String destinationFile, String pathToFile, int tileSize, String tileType, TilingStatusReporter progressIndicator,
			int nThreads, boolean flipVertically, Inhibitor inhibitor) throws IOException, InterruptedException {
		System.out.println("calculating tiles...");
		long mapID = 0;
		ExecutorService parallelPool = Executors.newFixedThreadPool(nThreads);
		// writing to an SQLite DB : 1 Thread max
		ExecutorService serialPool = Executors.newFixedThreadPool(1);

		if (destinationFile == null || pathToFile == null) { return; }
		// the pathTo file includes the fileName.
		File originalFile = new File(pathToFile);
		String fileSansDot = Ref.fileSansDot(pathToFile);
		initSource(destinationFile);

		name = fileSansDot;
		title = fileSansDot;
		description = (null == description ? "No Description" : description);

		// /////////////////////////////////
		System.out.println("Creating the tiles:");
		// //////////////////////////////
		BufferedImage img = null;
		long stop, start;
		System.out.print("Opening the image ... ");
		// System.out.flush();
		start = System.nanoTime();

		if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) { return; }
		ImageInputStream inStream = ImageIO.createImageInputStream(originalFile);
		img = ImageIO.read(inStream);
		if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) { return; }
		// //////////////////////////////

		stop = System.nanoTime();
		System.out.println(" done. It took " + ((double) (stop - start) / 1000000) + " ms");
		// Scaled image to 413 x 281. It took 404.18 ms
		int width = img.getWidth();
		int height = img.getHeight();
		System.out.println("Original size : " + width + " x " + height);
		sourceWidth = width;
		sourceHeigth = height;

		int nbX = (int) Math.ceil((double) width / tileSize);
		int nbY = (int) Math.ceil((double) height / tileSize);

		int scaledWidth = width;
		int scaledHeight = height;
		int zoom = 0;
		// //////////////////////
		Image xxx = null;
		// //////////////////////////
		// Creating Tiles.
		BufferedImage otherBuffer = null;
		scaledWidth = width;
		scaledHeight = height;

		int minimumDimension = 320;
		int aaMaxZoom = 0;
		double aaX = scaledWidth;
		double aaY = scaledHeight;
		while (Math.max(aaX, aaY) > minimumDimension) {
			aaMaxZoom++;
			aaX = aaX * ZOOM_FACTOR;
			aaY = aaY * ZOOM_FACTOR;
		}
		boolean miniatureCreated = false;
		int bufferedImageTileType = img.getType();
		while (scaledWidth > minimumDimension || scaledHeight > minimumDimension) {
			if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
				parallelPool.shutdownNow();
				serialPool.shutdownNow();
				return;
			}
			if (!miniatureCreated && (scaledWidth / 2 < MINIATURE_SIZE || scaledHeight / 2 < MINIATURE_SIZE)) {
				start = System.nanoTime();
				mini = GenericTileCreator.getMiniatureBytes(img, 320, 430, tileType);
				thumb = GenericTileCreator.getMiniatureBytes(img, 47, 47, tileType);
				stop = System.nanoTime();
				miniatureCreated = true;
			}
			addLevelInfos(fileSansDot, mapID, zoom, scaledWidth, scaledHeight, nbX, nbY, 0, 0);
			if (null != progressIndicator) {
				double ratio = (float) (zoom + 1) / (float) aaMaxZoom;
				progressIndicator.setTilingStatus("Creating zoom level " + (zoom + 1) + " / " + (aaMaxZoom), ratio);
			}
			int fillX = 0;
			int fillY = 0;
			fillX = ((nbX * tileSize) - scaledWidth);
			fillY = ((nbY * tileSize) - scaledHeight);
			Map<Point, Set<TileJobClipFlipSerialize>> buckets = new HashMap<Point, Set<TileJobClipFlipSerialize>>();
			for (int y = 0; y < nbY; y++) {
				for (int x = 0; x < nbX; x++) {
					if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
						parallelPool.shutdownNow();
						serialPool.shutdownNow();
						return;
					}

					int copyX = x * tileSize;
					int copyY = y * tileSize;
					int copyWidth = tileSize;
					int copyHeight = tileSize;

					// first column
					if (x == 0) {
						copyX = 0;
						copyWidth = tileSize;
					}
					// last column
					if (x == nbX - 1) {
						copyX = x * tileSize;
						copyWidth = tileSize - fillX;
					}

					// last line
					if (y == nbY - 1) {
						copyY = y * tileSize;
						copyHeight = tileSize - fillY;
					}
					Rectangle clip = new Rectangle(copyX, copyY, copyWidth, copyHeight);
					TileJobWrite writeJob = new TileJobWrite(x, y, zoom, insertTile);
					TileJobClipFlipSerialize job = new TileJobClipFlipSerialize(img, clip, flipVertically, tileSize, tileType, serialPool, writeJob);
					Point bucket = new Point(x / 2, y / 2);
					System.out.println("Bucket point of " + x + "," + y + "=" + bucket);
					Set<TileJobClipFlipSerialize> slot;
					if (!buckets.containsKey(bucket)) {
						slot = new HashSet<TileJobClipFlipSerialize>();
						buckets.put(bucket, slot);
					} else {
						slot = buckets.get(bucket);
					}
					slot.add(job);

				}
			}
			for (Set<TileJobClipFlipSerialize> set : buckets.values()) {
				for (TileJobClipFlipSerialize job : set) {
					parallelPool.execute(job);
				}
			}
			scaledWidth = (int) Math.ceil(scaledWidth * ZOOM_FACTOR);
			scaledHeight = (int) Math.ceil(scaledHeight * ZOOM_FACTOR);
			nbX = (int) Math.ceil((double) scaledWidth / tileSize);
			nbY = (int) Math.ceil((double) scaledHeight / tileSize);

			zoom++;
			if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
				parallelPool.shutdownNow();
				serialPool.shutdownNow();
				return;
			}

			start = System.nanoTime();
			xxx = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
			int ype = img.getType();
			img = null;
			img = new BufferedImage(scaledWidth, scaledHeight, ype);
			Graphics g0 = img.createGraphics();

			g0.drawImage(xxx, 0, 0, scaledWidth, scaledHeight, 0, 0, scaledWidth, scaledHeight, null);
			g0.dispose();
			stop = System.nanoTime();
			System.out.println("Scaled image to " + scaledWidth + " x " + scaledHeight + ". It took " + ((double) (stop - start) / 1000000) + " ms");
		}
		if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
			parallelPool.shutdownNow();
			serialPool.shutdownNow();
			return;
		}
		System.out.println(" ... waiting for tiles to be serialized ...");
		parallelPool.shutdown();
		parallelPool.awaitTermination(30, TimeUnit.MINUTES);
		start = System.nanoTime();
		System.out.println(" ... waiting for tiles to be written ...");

		serialPool.shutdown();
		serialPool.awaitTermination(30, TimeUnit.MINUTES);
		System.out.println(" ... setting tile info");
		setTileInfo(tilesetKey, tileType, tileSize, tileSize, null, flipVertically, bufferedImageTileType);
		System.out.println(" ... creating index ...");
		createIndexOnTileTable(connection, tilesetKey, layerKey);
		System.out.println("tiles created");
		stop = System.nanoTime();
		// System.out.println("scaled_image_" + zoom + ": " + ((double) (stop -
		// start) / 1000000) + " ms");
		System.out.println("tiles created : Delta serializing / writing : " + ((double) (stop - start) / 1000000) + " ms");
		doneCalculating = true;
	}
}
