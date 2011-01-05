package net.niconomicon.tile.source.app.tiling;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JProgressBar;

public class SQliteTileCreatorMultithreaded {
	Connection connection;

	public static final double MINIATURE_SIZE = 960;

	public static double ZOOM_FACTOR = 0.5;
	// public static int TILE_SIZE = 256;

	public static boolean CREATE_MINIATURE = true;

	public static int MAX_ZOOM_LEVEL = 0;

	public String tileSetID;
	public String name;
	public String title;
	public String description;

	public String author;
	public String source;

	long tilesetKey;
	long layerKey;
	int zIndex;
	int sourceWidth;
	int sourceHeigth;
	byte[] mini;
	byte[] thumb;


	public static void loadLib() {
		System.out.println("Trying to load the sqlite JDBC driver ...");
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException ex) {
			System.out.println("Loading the sqlite JDBC driver failed.");
			ex.printStackTrace();
			return;
		}
		Connection connection = null;
		try {
			File temp = File.createTempFile("tempMap", "tmp");
			// create a database connection
			System.out.println("Opening a connection to the temp db");
			connection = DriverManager.getConnection("jdbc:sqlite:" + temp.getAbsolutePath());
			System.out.println("Connected to the temp file");
			connection.close();
			temp.delete();
			System.out.println("Deleted the temp file");
			System.out.println("SQLite JDBC driver loaded.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean doneCalculating = false;

	public PreparedStatement insertTile;

	/**
	 * an archive is a collection of maps. a map is a collection of layers. a layer has an area
	 * 
	 * @param archiveName
	 * @param fileSansDot
	 */
	public void initSource(String archiveName, String fileSansDot) {
		tilesetKey = -1;
		layerKey = -1;

		if (tilesetKey == -1) {
			tilesetKey = 0;// currently only one map per database
		}
		if (layerKey == -1) {
			layerKey = 0;// currently only one layer per map
		}

		// load the sqlite-JDBC driver using the current class loader
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			return;
		}

		connection = null;
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + archiveName);
			connection.setAutoCommit(false);
			// System.out.println("Archive name : " + archiveName);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(3); // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists infos");
			statement.executeUpdate("drop table if exists tile_infos");
			statement.executeUpdate("drop table if exists level_infos");
			statement.executeUpdate("drop table if exists tiles_" + tilesetKey);
			//
			statement.executeUpdate("CREATE TABLE tile_info (mapKey LONG, tileExt STRING, tileWidth LONG, tileHeight LONG, emptyTile BLOB)");
			statement.executeUpdate("CREATE TABLE infos (title STRING, mapKey LONG, description STRING, author STRING, source STRING, date STRING, zindex LONG, " + "width LONG, height LONG," + "miniature BLOB,thumb BLOB)");
			// currently the layer name should be the same as the map name, as only one layer is supported
			statement.executeUpdate("CREATE TABLE layers_infos (" + "layerName STRING, mapKey LONG, zindex LONG, zoom  LONG, width LONG,height LONG, tiles_x LONG,tiles_y LONG, offset_x LONG, offset_y LONG)");
			statement.executeUpdate("CREATE TABLE tiles_" + tilesetKey + "_" + layerKey + " (x LONG , y LONG, z LONG, data BLOB)");
			// Prepare most frequently used statement;
			String insertTiles = "insert into tiles_" + tilesetKey + "_" + layerKey + " values( ?, ?, ?, ?)";
			insertTile = connection.prepareStatement(insertTiles);
			insertTile.clearParameters();
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void finalizeFile() {
		addInfos(name, author, source, title, description, zIndex, sourceWidth, sourceHeigth, mini, thumb);
		try {
			connection.commit();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// add source, author,date , thumb
	}

	public void addInfos(String name, String author, String source, String title, String description, int zindex, int width, int height, byte[] mini, byte[] thumb) {
		long mapID = 0;

		String stat = "INSERT INTO infos VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		String date = new Date(System.currentTimeMillis()).toString();
		// System.out.println("stat = " + stat);
		try {
			PreparedStatement ps = connection.prepareStatement(stat);
			int i = 1;
			ps.setString(i++, title);
			ps.setLong(i++, tilesetKey);
			// ps.setString(i++, name);
			ps.setString(i++, description);
			ps.setString(i++, author);
			ps.setString(i++, source);
			ps.setString(i++, date);
			ps.setLong(i++, zindex);
			ps.setLong(i++, width);
			ps.setLong(i++, height);
			ps.setBytes(i++, mini);
			ps.setBytes(i++, thumb);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Information insertion failed.");
			e.printStackTrace();
		}
	}

	/**
	 * Adds a tile to the current tile source
	 * @param x
	 * @param y
	 * @param z
	 * @param data
	 * @param fileSansDot
	 */
	public void addTile(int x, int y, int z, byte[] data, String fileSansDot) {
		long stop, start;
		start = System.nanoTime();
		// String stat = "insert into ? values(" + x + "," + y + "," + z +
		// ",?)";
		try {
			insertTile.setInt(1, x);
			insertTile.setInt(2, y);
			insertTile.setInt(3, z);

			insertTile.setBytes(4, data);
			insertTile.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Export failed !");
			e.printStackTrace();
		}
		stop = System.nanoTime();
		// System.out.println("writing_tile: " + ((double) (stop - start) / 1000000) + " ms");
	}

	public void addLevelInfos(String name, long mapID, int zoom, int width, int height, int tiles_x, int tiles_y, int offsetX, int offsetY) {
		long stop, start;
		start = System.nanoTime();
		String layerName = "no name";
		long zindex = 0;
		String stat = "INSERT INTO layers_infos VALUES(\"" + layerName + "\"," + mapID + "," + zindex + "," + zoom + "," + width + "," + height + "," + tiles_x + "," + tiles_y + "," + offsetX + "," + offsetY + ")";
		System.out.println("stat = " + stat);
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(stat);
		} catch (SQLException e) {
			System.err.println("Information insertion failed.");
			e.printStackTrace();
		}
		stop = System.nanoTime();
		// System.out.println("writing_level_info: " + ((double) (stop - start) / 1000000) + " ms");

	}

	public void calculateTiles(String destinationFile, String pathToFile, int tileSize, String tileType, JProgressBar progressIndicator, int nThreads) throws Exception {
		System.out.println("calculating tiles...");
		long mapID = 0;
		ExecutorService serialPool = Executors.newFixedThreadPool(nThreads);
		ExecutorService plumberPool = Executors.newFixedThreadPool(1);

		if (destinationFile == null || pathToFile == null) { return; }
		// the pathTo file includes the fileName.
		File originalFile = new File(pathToFile);
		String fileSansDot = pathToFile.substring(pathToFile.lastIndexOf(File.separator) + 1, pathToFile.lastIndexOf("."));
		initSource(destinationFile, fileSansDot);

		name = fileSansDot;
		title = (null == title ? fileSansDot : title);
		description = (null == description ? "No Description" : description);

		// /////////////////////////////////
		System.out.println("creating the tiles");
		// //////////////////////////////
		BufferedImage img = null;
		long stop, start;
		start = System.nanoTime();

		ImageInputStream inStream = ImageIO.createImageInputStream(originalFile);
		img = ImageIO.read(inStream);
		// //////////////////////////////

		stop = System.nanoTime();
		System.out.println("opening_image: " + ((double) (stop - start) / 1000000) + " ms");

		int width = img.getWidth();
		int height = img.getHeight();

		sourceWidth = width;
		sourceHeigth = height;

		int oldNbX = (width / tileSize) + 1;
		int oldNbY = (height / tileSize) + 1;
		int nbX = (int) Math.ceil((double) width / tileSize);
		int nbY = (int) Math.ceil((double) height / tileSize);

		// System.out.println("old : nbX=" + oldNbX + " nbY=" + oldNbY + " new : +nbX" + nbX + " nbY=" + nbY);

		int scaledWidth = width;
		int scaledHeight = height;
		int zoom = 0;
		// //////////////////////
		Image xxx = null;
		// //////////////////////////
		// Creating Tiles.
		BufferedImage buffer = null;
		BufferedImage otherBuffer = null;
		scaledWidth = width;
		scaledHeight = height;

		int aaMaxZoom = 0;
		double aaX = scaledWidth;
		double aaY = scaledHeight;
		while (Math.min(aaX, aaY) > tileSize) {
			aaMaxZoom++;
			aaX = aaX * ZOOM_FACTOR;
			aaY = aaY * ZOOM_FACTOR;
		}

		boolean miniatureCreated = false;
		while (scaledWidth > 320 || scaledHeight > 320) {
			if (!miniatureCreated && (scaledWidth / 2 < 320 || scaledHeight / 2 < 430)) {
				start = System.nanoTime();
				mini = GenericTileCreator.getMiniatureBytes(img, 320, 430, tileType);
				thumb = GenericTileCreator.getMiniatureBytes(img, 47, 47, tileType);
				stop = System.nanoTime();
				miniatureCreated = true;
			}
			addLevelInfos(fileSansDot, mapID, zoom, scaledWidth, scaledHeight, nbX, nbY, 0, 0);
			if (null != progressIndicator) {
				progressIndicator.setValue(100 / (aaMaxZoom + 1));
				progressIndicator.setString("Creating zoom level " + (zoom + 1) + " / " + (aaMaxZoom + 1));
			}
			int fillX = 0;
			int fillY = 0;
			fillX = ((nbX * tileSize) - scaledWidth);
			fillY = ((nbY * tileSize) - scaledHeight);
			// System.out.println("fill x =" + fillX + " fill y=" + fillY + " nbX=" + nbX + " nbY=" + nbY + " w=" +
			// scaledWidth + " h=" + scaledHeight);
			// System.out.println("fill x =" + fillX + " fill y=" + fillY);
			for (int y = 0; y < nbY; y++) {
				for (int x = 0; x < nbX; x++) {
					int copyX = x * tileSize;
					int copyY = y * tileSize;
					int copyWidth = tileSize;
					int copyHeight = tileSize;
					int pasteWidth = tileSize;
					int pasteHeight = tileSize;
					int pasteX = 0;
					int pasteY = 0;

					// first column
					if (x == 0) {
						copyX = 0;
						copyWidth = tileSize;
						pasteX = 0;
						pasteWidth = tileSize;
					}
					// last column
					if (x == nbX - 1) {
						copyX = x * tileSize;
						copyWidth = tileSize - fillX;
						pasteX = 0;
						pasteWidth = copyWidth;// copyWidth;
					}

					// last line
					if (y == nbY - 1) {
						copyY = y * tileSize;
						copyHeight = tileSize - fillY;
						pasteY = 0;
						pasteHeight = copyHeight;
					}
					Rectangle clip = new Rectangle(copyX, copyY, copyWidth, copyHeight);
					otherBuffer = FastClipper.fastClip(img, clip, true);

					// //////////////////////////////////////
					// Saving the tile
					serialPool.execute(new TileSerializeJob(x, y, zoom, otherBuffer, plumberPool, connection, insertTile));
				}
			}
			scaledWidth = (int) Math.ceil(scaledWidth * ZOOM_FACTOR);
			scaledHeight = (int) Math.ceil(scaledHeight * ZOOM_FACTOR);
			// System.out.println("scaled width " + scaledWidth + " height " + scaledHeight);
			oldNbX = (scaledWidth / tileSize) + 1;
			oldNbY = (scaledHeight / tileSize) + 1;
			nbX = (int) Math.ceil((double) scaledWidth / tileSize);
			nbY = (int) Math.ceil((double) scaledHeight / tileSize);
			System.out.println("old : nbX=" + oldNbX + " nbY=" + oldNbY + " new : +nbX" + nbX + " nbY=" + nbY);

			zoom++;

			start = System.nanoTime();
			xxx = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
			img = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g0 = img.createGraphics();

			g0.drawImage(xxx, 0, 0, scaledWidth, scaledHeight, 0, 0, scaledWidth, scaledHeight, null);
			g0.dispose();
			stop = System.nanoTime();
			System.out.println("scaled_image_" + zoom + ": " + ((double) (stop - start) / 1000000) + " ms");

			// System.out.println("zoom layer : " + zoom + " image size:" + img.getWidth() + "x" + img.getHeight());
		}
		serialPool.shutdown();
		serialPool.awaitTermination(15, TimeUnit.MINUTES);
		start = System.nanoTime();

		plumberPool.shutdown();
		plumberPool.awaitTermination(15, TimeUnit.MINUTES);
		System.out.println(" ... setting tile info");
		setTileInfo(tilesetKey, tileType, tileSize, tileSize, null);
		System.out.println(" ... creating index");
		createIndexOnTileTable(connection, tilesetKey, layerKey);
		System.out.println("tiles created");
		stop = System.nanoTime();
		// System.out.println("scaled_image_" + zoom + ": " + ((double) (stop - start) / 1000000) + " ms");
		System.out.println("tiles created : Delta serializing / writing : " + ((double) (stop - start) / 1000000) + " ms");
		doneCalculating = true;
	}

	public static void createIndexOnTileTable(Connection conn, long mapID, long layerID) {
		try {
			Statement st = conn.createStatement();
			st.execute("CREATE INDEX index_tiles_" + mapID + "_" + layerID + " ON tiles_" + mapID + "_" + layerID + "(z,y,x)");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setTileInfo(long mapID, String tileType, long tileWidth, long tileHeight, InputStream emptyTile) {
		try {
			PreparedStatement st = connection.prepareStatement("insert into tile_info values (?, ?, ?, ?, ?)");
			// (mapKey, tileExt, tileWidth , tileHeight, emptyTile ):
			st.setLong(1, mapID);
			st.setString(2, tileType);
			st.setLong(3, tileWidth);
			st.setLong(4, tileHeight);
			// st.setBlob(5, emptyTile);
			st.execute();
		} catch (SQLException e) {
			System.err.println("Information insertion failed.");
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		String[] files;
		files = new String[] { "pdfs/CERN_Prevessin_A3_Paysage.pdf" };

		
		String destDir = "/Users/niko/tileSources/bench/";
		String src = "/Users/niko/tileSources/";
		//This call blocks until done (duh) - for usually more than 2 second. It loads the native sqlite library.
		SQliteTileCreatorMultithreaded.loadLib();
		SQliteTileCreatorMultithreaded creator = new SQliteTileCreatorMultithreaded();
		long start, stop;
		int count = 1;
		int nThreads = 4;
		int c = 0;
		String extension = ".its";//For "image tile set"
		for (int i = 0; i < count; i++) {
			System.gc();
			for (String file : files) {
				creator.title = file.substring(0, file.lastIndexOf(".")) + "_multi";
				System.out.println("Processing " + creator.title);
				String dstFile = destDir + creator.title + extension;
				File f = new File(dstFile);
				if (f.exists()) {
					System.out.println("removing this file : " + dstFile);
					f.delete();
				}
				start = System.nanoTime();
				System.out.println("Started : " + dstFile);
				creator.calculateTiles(dstFile, src + file, 192, "png", new JProgressBar(), nThreads);
				creator.finalizeFile();
				stop = System.nanoTime();
				System.out.println("## => total_time: " + ((double) (stop - start) / 1000000) + " ms nThreads = " + nThreads + " + 1 mains + 1 writer");
			}
		}
	}
}