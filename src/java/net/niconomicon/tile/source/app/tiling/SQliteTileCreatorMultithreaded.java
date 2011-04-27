package net.niconomicon.tile.source.app.tiling;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JProgressBar;

import net.niconomicon.tile.source.app.Ref;

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
	 * This method will open the file as a sqlite db and drop then create the tables used for storing a Displayable.
	 * //An archive is a collection of Displayable. a Displayable is a collection of layers. a layer has an area
	 * 
	 * @param archivePath
	 */
	public void initSource(String archivePath) {
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
			connection = DriverManager.getConnection("jdbc:sqlite:" + archivePath);
			connection.setAutoCommit(false);
			// System.out.println("Archive name : " + archiveName);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(3); // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists infos");
			statement.executeUpdate("drop table if exists tile_infos");
			statement.executeUpdate("drop table if exists level_infos");
			statement.executeUpdate("drop table if exists tiles_" + tilesetKey);
			//
			statement
					.executeUpdate("CREATE TABLE tile_info (mapKey LONG, tileExt STRING, tileWidth LONG, tileHeight LONG, emptyTile BLOB, flippedVertically BOOL , javaImageType INT)");
			statement
					.executeUpdate("CREATE TABLE infos (title STRING, mapKey LONG, description STRING, author STRING, source STRING, date STRING, zindex LONG, " + "width LONG, height LONG," + "miniature BLOB,thumb BLOB)");
			// currently the layer name should be the same as the map name, as only one layer is supported
			statement
					.executeUpdate("CREATE TABLE layers_infos (" + "layerName STRING, mapKey LONG, zindex LONG, zoom  LONG, width LONG,height LONG, tiles_x LONG,tiles_y LONG, offset_x LONG, offset_y LONG)");
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

	/**
	 * This method will write the info about the Displayable (name, author, title , size etc...) and call the commit and
	 * close method on the db connection.
	 */
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

	/**
	 * Connect to the database and changes the title before disconnecting.
	 * 
	 * @param dbFile
	 * @param oldTitle
	 * @param newTitle
	 */
	public static void updateTitle(String dbFile, String oldTitle, String newTitle) {
		String stat = "UPDATE infos SET title=? WHERE title=?";
		String date = new Date(System.currentTimeMillis()).toString();
		// System.out.println("stat = " + stat);
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
			connection.setAutoCommit(false);

			PreparedStatement ps = connection.prepareStatement(stat);

			ps.setString(1, newTitle);
			ps.setString(2, oldTitle);
			ps.executeUpdate();
			connection.commit();
			connection.close();
		} catch (SQLException e) {
			System.err.println("Information insertion failed.");
			e.printStackTrace();
		}
	}

	public static String getTitle(String currentLocation) throws SQLException {
		Connection connection = null;
		// create a database connection
		connection = DriverManager.getConnection("jdbc:sqlite:" + currentLocation);
		connection.setAutoCommit(false);
		// System.out.println("Archive name : " + archiveName);
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(3);
		ResultSet set = statement.executeQuery("SELECT title FROM infos");
		String s = set.getString(1);
		connection.commit();
		connection.close();
		return s;
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
	 * 
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
			System.err.println("Tile Export failed !");
			e.printStackTrace();
		}
		stop = System.nanoTime();
	}

	public void addLevelInfos(String name, long mapID, int zoom, int width, int height, int tiles_x, int tiles_y, int offsetX, int offsetY) {
		String layerName = "no name";
		long zindex = 0;
		String stat = "INSERT INTO layers_infos VALUES(?,?,?,?,?,?,?,?,?,?)";
		try {
			// System.out.println("stat = " + stat);
			PreparedStatement ps = connection.prepareStatement(stat);
			int i = 1;
			ps.setString(i++, layerName);
			ps.setLong(i++, mapID);
			ps.setLong(i++, zindex);
			ps.setInt(i++, zoom);
			ps.setInt(i++, width);
			ps.setInt(i++, height);
			ps.setInt(i++, tiles_x);
			ps.setInt(i++, tiles_y);
			ps.setInt(i++, offsetX);
			ps.setInt(i++, offsetY);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Level Information insertion failed.");
			e.printStackTrace();
		}
	}

	public void calculateTiles(String destinationFile, String pathToFile, int tileSize, String tileType, TilingStatusReporter progressIndicator, int nThreads, boolean flipVertically, Inhibitor inhibitor) throws IOException, InterruptedException {
		System.out.println("calculating tiles...");
		long mapID = 0;
		ExecutorService serialPool = Executors.newFixedThreadPool(nThreads);
		ExecutorService plumberPool = Executors.newFixedThreadPool(1);// writing to a SQLite DB : 1 thread max :-(

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
				serialPool.shutdownNow();
				plumberPool.shutdownNow();
				return;
			}
			if (!miniatureCreated && (scaledWidth / 2 < 320 || scaledHeight / 2 < 430)) {
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
			for (int y = 0; y < nbY; y++) {
				for (int x = 0; x < nbX; x++) {
					if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
						serialPool.shutdownNow();
						plumberPool.shutdownNow();
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
					otherBuffer = FastClipper.fastClip(img, clip, flipVertically);

					// //////////////////////////////////////
					// Saving the tile
					serialPool.execute(new TileSerializeJob(x, y, zoom, otherBuffer, plumberPool, connection, insertTile));
				}
			}
			scaledWidth = (int) Math.ceil(scaledWidth * ZOOM_FACTOR);
			scaledHeight = (int) Math.ceil(scaledHeight * ZOOM_FACTOR);
			nbX = (int) Math.ceil((double) scaledWidth / tileSize);
			nbY = (int) Math.ceil((double) scaledHeight / tileSize);

			zoom++;
			if (null != inhibitor && inhibitor.hasRunInhibitionBeenRequested()) {
				serialPool.shutdownNow();
				plumberPool.shutdownNow();
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
			serialPool.shutdownNow();
			plumberPool.shutdownNow();
			return;
		}
		System.out.println(" ... waiting for tiles to be serialized ...");
		serialPool.shutdown();
		serialPool.awaitTermination(30, TimeUnit.MINUTES);
		start = System.nanoTime();
		System.out.println(" ... waiting for tiles to be written ...");

		plumberPool.shutdown();
		plumberPool.awaitTermination(30, TimeUnit.MINUTES);
		System.out.println(" ... setting tile info");
		setTileInfo(tilesetKey, tileType, tileSize, tileSize, null, flipVertically, bufferedImageTileType);
		System.out.println(" ... creating index ...");
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

	public void setTileInfo(long mapID, String tileType, long tileWidth, long tileHeight, InputStream emptyTile, boolean verticallyFlipped, int bufferedImageTileType) {
		try {
			PreparedStatement st = connection.prepareStatement("insert into tile_info values (?, ?, ?, ?, ?, ?, ?)");
			st.setLong(1, mapID);
			st.setString(2, tileType);
			st.setLong(3, tileWidth);
			st.setLong(4, tileHeight);
			// st.setBlob(5, emptyTile.);
			st.setBoolean(6, verticallyFlipped);
			st.setInt(7, bufferedImageTileType);
			st.execute();
		} catch (SQLException e) {
			System.err.println("Information insertion failed.");
			e.printStackTrace();
		}

	}
}