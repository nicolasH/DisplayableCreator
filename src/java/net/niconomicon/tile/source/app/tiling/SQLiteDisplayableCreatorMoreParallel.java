package net.niconomicon.tile.source.app.tiling;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.tiling.moreParallel.TileJobClipFlipSaveSerialize;
import net.niconomicon.tile.source.app.tiling.moreParallel.TileJobShrink;
import net.niconomicon.tile.source.app.tiling.parallel.TileJobWrite;

public class SQLiteDisplayableCreatorMoreParallel extends SQliteTileCreatorMultithreaded {

	public void calculateTiles(String destinationFile, String pathToFile, int tileSize, String tileType, TilingStatusReporter progressIndicator,
			int nThreads, boolean flipVertically, Inhibitor inhibitor) throws IOException, InterruptedException {
		System.out.println("calculating tiles...");
		long mapID = 0;

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
		int imageType = img.getType();
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
		Map<Point, TileJobShrink> shrunk = new HashMap<Point, TileJobShrink>();

		ExecutorService parallelPool = Executors.newFixedThreadPool(nThreads);
		// writing to an SQLite DB : 1 Thread max
		ExecutorService serialPool = Executors.newFixedThreadPool(1);

		addLevelInfos(fileSansDot, mapID, zoom, width, height, nbX, nbY, 0, 0);

		start = System.nanoTime();
		Map<Point, TileJobShrink> shrinked = writeLevel(img, null, scaledWidth, scaledHeight, zoom, tileSize, flipVertically, tileType, serialPool,
				parallelPool, insertTile, zoom != aaMaxZoom);
		System.out.println(" ... waiting up to 30 minutes for original tiles to be cut and shrunk ...");
		parallelPool.shutdown();
		parallelPool.awaitTermination(30, TimeUnit.MINUTES);
		stop = System.nanoTime();
		System.out.println("Clipping and shrinking for z=0 took : " + ((double) (stop - start) / 1000000) + " ms");
		img = null;
		// Still the original size
		while (scaledWidth > minimumDimension || scaledHeight > minimumDimension) {
			if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
				parallelPool.shutdownNow();
				serialPool.shutdownNow();
				return;
			}
			start = System.nanoTime();
			parallelPool = Executors.newFixedThreadPool(nThreads);

			if (!miniatureCreated && (scaledWidth / 2 < MINIATURE_WIDTH || scaledHeight / 2 < MINIATURE_HEIGHT)) {
				start = System.nanoTime();
				Dimension src = new Dimension(scaledWidth/2, scaledHeight/2);
				Dimension dst = GenericTileCreator.getRecommendedDim(src, new Dimension(MINIATURE_WIDTH, MINIATURE_HEIGHT));

				BufferedImage small = GenericTileCreator.assembleAndShrinkMiniature(shrinked, src, dst, imageType, tileSize, tileType);
				thumb = GenericTileCreator.getMiniatureBytes(small, 47, 47, tileType);
				ByteArrayOutputStream byteStorage = new ByteArrayOutputStream();
				ImageIO.write(small, tileType, byteStorage);
				mini = byteStorage.toByteArray();
				stop = System.nanoTime();
				miniatureCreated = true;
			}
			scaledWidth = (int) Math.ceil(scaledWidth * ZOOM_FACTOR);
			scaledHeight = (int) Math.ceil(scaledHeight * ZOOM_FACTOR);
			nbX = (int) Math.ceil((double) scaledWidth / tileSize);
			nbY = (int) Math.ceil((double) scaledHeight / tileSize);
			zoom++;
			shrinked = writeLevel(null, shrinked, scaledWidth, scaledHeight, zoom, tileSize, flipVertically, tileType, serialPool, parallelPool,
					insertTile, zoom != aaMaxZoom);
			addLevelInfos(fileSansDot, mapID, zoom, scaledWidth, scaledHeight, nbX, nbY, 0, 0);

			System.out.println(" ... waiting up to 30 minutes for original tiles to be cut and shrunk ...");
			parallelPool.shutdown();
			parallelPool.awaitTermination(30, TimeUnit.MINUTES);
			stop = System.nanoTime();
			System.out.println("Clipping and shrinking for z=" + zoom + " took : " + ((double) (stop - start) / 1000000) + " ms");

		}

		if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
			parallelPool.shutdownNow();
			serialPool.shutdownNow();
			return;
		}
		start = System.nanoTime();
		System.out.println(" ... waiting up to 30 minutes for tiles to be written ...");
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

	// Later version: return the original border tiles.
	/**
	 * This will add tile operation to the parallel pool, connect them to their
	 * tile job shrink operation and return the map of shrink jobs.
	 * 
	 * @param image
	 * @param currentLayer
	 * @param width
	 * @param height
	 * @param zoom
	 * @param tileSize
	 * @param flipVertically
	 * @param tileType
	 * @param serialPool
	 * @param parallelPool
	 * @param insertTile
	 * @param shrinkThisLevelToo
	 * @return
	 */
	public static Map<Point, TileJobShrink> writeLevel(BufferedImage image, Map<Point, TileJobShrink> currentLayer, int width, int height, int zoom,
			int tileSize, boolean flipVertically, String tileType, Executor serialPool, Executor parallelPool, PreparedStatement insertTile,
			boolean shrinkThisLevelToo) {

		Map<Point, Set<TileJobClipFlipSaveSerialize>> buckets = new HashMap<Point, Set<TileJobClipFlipSaveSerialize>>();
		Map<Point, Rectangle> clips;
		if (zoom > 0) {
			clips = FastClipper.getClips(width, height, tileSize, false);
		} else {
			clips = FastClipper.getClips(width, height, tileSize, true);
		}
		for (Point p : clips.keySet()) {
			Rectangle clip = clips.get(p);
			TileJobWrite writeJob = new TileJobWrite(p.x, p.y, zoom, insertTile);
			TileJobClipFlipSaveSerialize job;
			if (zoom > 0) {
				TileJobShrink thisTile = currentLayer.get(p);
				job = new TileJobClipFlipSaveSerialize(thisTile.finalTile, clip, flipVertically, tileSize, tileType, serialPool, writeJob);
			} else {
				job = new TileJobClipFlipSaveSerialize(image, clip, flipVertically, tileSize, tileType, serialPool, writeJob);
			}
			Point bucket = new Point(p.x / 2, p.y / 2);

			Set<TileJobClipFlipSaveSerialize> slot;
			if (!buckets.containsKey(bucket)) {
				slot = new HashSet<TileJobClipFlipSaveSerialize>();
				buckets.put(bucket, slot);
			} else {
				slot = buckets.get(bucket);
			}
			slot.add(job);
		}

		Map<Point, TileJobShrink> shrinks = null;
		if (shrinkThisLevelToo) {
			shrinks = new HashMap<Point, TileJobShrink>();
			for (Point p : buckets.keySet()) {
				Set<TileJobClipFlipSaveSerialize> set = buckets.get(p);
				TileJobShrink shrinkJob = new TileJobShrink(p.x, p.y, (long) zoom + 1, parallelPool, tileSize, null);
				shrinkJob.subTiles = set.size();
				for (TileJobClipFlipSaveSerialize job : set) {
					job.shrink = shrinkJob;
					parallelPool.execute(job);
				}
				shrinks.put(p, shrinkJob);
			}
		} else {
			for (Point p : buckets.keySet()) {
				Set<TileJobClipFlipSaveSerialize> set = buckets.get(p);
				for (TileJobClipFlipSaveSerialize job : set) {
					parallelPool.execute(job);
				}
			}
		}
		return shrinks;
	}

}
