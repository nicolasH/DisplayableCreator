package net.niconomicon.tile.source.app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JProgressBar;

import net.niconomicon.tile.source.app.filter.ImageAndPDFFileFilter;

public class SQliteTileCreator {
	Connection connection;

	public static final double MINIATURE_SIZE = 500;

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

	long mapKey;
	long layerKey;
	int zIndex;
	int sourceWidth;
	int sourceHeigth;
	byte[] mini;
	byte[] thumb;

	private static class libLoader implements Runnable {
		public void run() {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
				return;
			}
			Connection connection = null;
			try {
				File temp = File.createTempFile("tempMap", Ref.ext_db);
				// create a database connection
				System.out.println("Opening a connection to the temp db");
				connection = DriverManager.getConnection("jdbc:sqlite:" + temp.getAbsolutePath());
				System.out.println("Connected to the temp file");
				connection.close();
				temp.delete();
				System.out.println("Deleted the temp file");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void loadLib() {

		Thread t = new Thread(new libLoader());
		t.start();

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
		mapKey = -1;
		layerKey = -1;

		if (mapKey == -1) {
			mapKey = 0;// currently only one map per database
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
			// System.out.println("Archive name : " + archiveName);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(3); // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists infos");
			statement.executeUpdate("drop table if exists tile_infos");
			statement.executeUpdate("drop table if exists level_infos");
			statement.executeUpdate("drop table if exists tiles_" + mapKey);
			// 
			statement.executeUpdate("CREATE TABLE tile_info (mapKey LONG, tileExt STRING, tileWidth LONG, tileHeight LONG, emptyTile BLOB)");
			statement.executeUpdate("CREATE TABLE infos (title STRING, mapKey LONG, description STRING, author STRING, source STRING, date STRING, zindex LONG, " + "width LONG, height LONG," + "miniature BLOB,thumb BLOB)");
			// currently the layer name should be the same as the map name, as only one layer is supported
			statement.executeUpdate("CREATE TABLE layers_infos (" + "layerName STRING, mapKey LONG, zindex LONG, zoom  LONG, width LONG,height LONG, tiles_x LONG,tiles_y LONG, offset_x LONG, offset_y LONG)");
			statement.executeUpdate("CREATE TABLE tiles_" + mapKey + "_" + layerKey + " (x LONG , y LONG, z LONG, data BLOB)");
			// Prepare most frequently used statement;
			String insertTiles = "insert into tiles_" + mapKey + "_" + layerKey + " values( ?, ?, ?, ?)";
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
			// connection.commit();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// add source, author,date , thumb
	}

	public void addInfos(String name, String author, String source, String title, String description, int zindex, int width, int height, byte[] mini, byte[] thumb) {
		// TABLE infos (name STRING, title STRING,description STRING, author
		// STRING, source STRING, date STRING, zindex INTEGER, miniature
		// BLOB,thumb BLOB)");

		// String stat = "INSERT INTO infos VALUES(\"" + name + "\",\"" + title
		// + "\",\""
		// + description + "\",\"" + author + "\",\"" + source + "\",\""
		// + System.currentTimeMillis() + "\"," + zindex + ", ?,?)";
		//		
		long mapID = 0;
		// CREATE TABLE infos (title STRING, mapKey LONG, description STRING, author STRING, source STRING, date STRING,
		// zindex LONG, " + "width LONG, height LONG," + "miniature BLOB,thumb BLOB)");

		String stat = "INSERT INTO infos VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		String date = new Date(System.currentTimeMillis()).toString();
		System.out.println("stat = " + stat);
		try {
			PreparedStatement ps = connection.prepareStatement(stat);
			int i = 1;
			ps.setString(i++, title);
			ps.setLong(i++, mapKey);
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
		// System.out.println("stat = " + stat);
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

	public void calculateTiles(String destinationFile, String pathToFile, int tileSide, String tileType, JProgressBar progressIndicator) throws Exception {
		System.out.println("calculating tiles...");
		long mapID = 0;

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

		if (ImageAndPDFFileFilter.getLowerCaseExt(pathToFile).endsWith("pdf")) {
			img = PDFToImageRenderer.getImageFromPDFFile(pathToFile, 300);
		} else {
			ImageInputStream inStream = ImageIO.createImageInputStream(originalFile);
			img = ImageIO.read(inStream);
		} // //////////////////////////////

		stop = System.nanoTime();
		System.out.println("opening_image: " + ((double) (stop - start) / 1000000) + " ms");

		int width = img.getWidth();
		int height = img.getHeight();

		sourceWidth = width;
		sourceHeigth = height;

		int nbX = (width / tileSide) + 1;
		int nbY = (height / tileSide) + 1;

		int scaledWidth = width;
		int scaledHeight = height;
		int zoom = 0;
		// //////////////////////
		Image xxx = null;
		ByteArrayOutputStream byteStorage = new ByteArrayOutputStream();
		byteStorage.reset();

		// level 0
		addLevelInfos(fileSansDot, mapID, zoom, width, height, nbX, nbY, 0, 0);
		// //////////////////////////
		// Creating Tiles.
		BufferedImage buffer = null;
		BufferedImage otherBuffer = null;
		scaledWidth = width;
		scaledHeight = height;

		int aaMaxZoom = 0;
		double aaX = scaledWidth;
		double aaY = scaledHeight;
		while (Math.min(aaX, aaY) > tileSide) {
			aaMaxZoom++;
			aaX = aaX * ZOOM_FACTOR;
			aaY = aaY * ZOOM_FACTOR;
		}
		if (null != progressIndicator) {
			progressIndicator.setValue(100 / (aaMaxZoom + 1));
			// progressIndicator.setString("Creating zoom level " + (zoom + 1) + " / " + (aaMaxZoom + 1));
		}
		boolean alreadyCreatedMiniature = false;
		// while (Math.min(scaledWidth, scaledHeight) > tileSize) {
		while ((scaledWidth > 0 && scaledHeight > 0) && (scaledWidth > 320 || scaledHeight > 320)) {
			if (!alreadyCreatedMiniature && (scaledWidth / 2 < 320 || scaledHeight / 2 < 430)) {
				start = System.nanoTime();
				mini = GenericTileCreator.getMiniatureBytes(img, 320, 430, tileType);
				thumb = GenericTileCreator.getMiniatureBytes(img, 47, 47, tileType);
				stop = System.nanoTime();
				// System.out.println("creating_minitatures: " + ((double) (stop - start) / 1000000) + " ms");
				alreadyCreatedMiniature = true;
			}

			int fillX = 0;
			int fillY = 0;
			fillX = ((nbX * tileSide) - scaledWidth);
			fillY = ((nbY * tileSide) - scaledHeight);
			// System.out.println("fill x =" + fillX + " fill y=" + fillY);
			for (int y = 0; y < nbY; y++) {
				for (int x = 0; x < nbX; x++) {
					int copyX = x * tileSide;
					int copyY = y * tileSide;
					int copyWidth = tileSide;
					int copyHeight = tileSide;
					int pasteWidth = tileSide;
					int pasteHeight = tileSide;
					int pasteX = 0;
					int pasteY = 0;

					// first column
					if (x == 0) {
						copyX = 0;
						copyWidth = tileSide;
						pasteX = 0;
						pasteWidth = tileSide;
					}
					// first line
					/*if (y == 0) {
						copyY = 0;
						copyHeight = tileSize - fillY;
						pasteY = 0;
						pasteHeight = tileSize - fillY;
					} else {
						copyY = copyY - fillY;
					}*/
					// last column
					if (x == nbX - 1) {
						copyX = x * tileSide;
						copyWidth = tileSide - fillX;
						pasteX = 0;
						pasteWidth = copyWidth;// copyWidth;
					}

					// last line
					if (y == nbY - 1) {
						copyY = y * tileSide;
						copyHeight = tileSide - fillY;
						pasteY = 0;
						pasteHeight = copyHeight;
					}
					// System.out.println("x ="+x+" y="+y+ " copyX="+
					// copyX+" copyY="+copyY+" copyWidth="+copyXX+" copyHeight="
					// +copyYY+ " pasteX=" + pasteX
					// +" -> pasteY="+pasteY+" pasteWidth="+
					// pasteXX+" pasteHeight=" +pasteYY );
					start = System.nanoTime();
					buffer = new BufferedImage(copyWidth, copyHeight, BufferedImage.TYPE_INT_RGB);
					Graphics2D g2 = buffer.createGraphics();
					g2.setColor(Color.DARK_GRAY);
					g2.fillRect(0, 0, tileSide, tileSide);

					// buffer = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
					// Graphics2D g2 = buffer.createGraphics();
					// g2.setColor(Color.DARK_GRAY);
					// g2.fillRect(0, 0, tileSize, tileSize);

					g2.drawImage(img, pasteX, pasteY, pasteWidth, pasteHeight, copyX, copyY, copyX + copyWidth, copyY + copyHeight, null);
					g2.dispose();
					stop = System.nanoTime();
					// System.out.println("copy_pasting_tile: " + ((double) (stop - start) / 1000000) + " ms");

					start = System.nanoTime();
					otherBuffer = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.OPAQUE);
					Graphics2D g3 = (Graphics2D) otherBuffer.getGraphics();
					g3.scale(1, -1);
					g3.drawImage(buffer, 0, -buffer.getHeight(), null);
					g3.dispose();
					stop = System.nanoTime();
					// System.out.println("flipping_tile: " + ((double) (stop - start) / 1000000) + " ms");

					// //////////////////////////////////////
					// Writing the tiles

					try {
						start = System.nanoTime();
						byteStorage.reset();
						ImageIO.write(otherBuffer, tileType, byteStorage);
						stop = System.nanoTime();
						// System.out.println("serializing_tile: " + ((double) (stop - start) / 1000000) + " ms");

						// addTile(x, (nbY - 1) - y, zoom, byteStorage.toByteArray(), fileSansDot);
						addTile(x, y, zoom, byteStorage.toByteArray(), fileSansDot);
						// out.createImageFile(localPath, fileName, buffer,
						// tileType);// "jpg");
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					// //////////////////////////////////////
					// TODO NOTE : to save memory, re read everything
					// cleanly afterwards ?
					// ServerSideTile t = new ServerSideTile(x, y, 0, out);
					// tiles.put(getKey(x, y, 0), t);
				}
			}
			scaledWidth = (int) (scaledWidth * ZOOM_FACTOR);
			scaledHeight = (int) (scaledHeight * ZOOM_FACTOR);
			// System.out.println("scaled width " + scaledWidth + " height " + scaledHeight);
			nbX = (scaledWidth / tileSide) + 1;
			nbY = (scaledHeight / tileSide) + 1;

			zoom++;
			if (null != progressIndicator) {
				progressIndicator.setValue((int) ((100 / (aaMaxZoom + 1)) * (zoom + 1)));
				progressIndicator.setString("Creating zoom level " + (zoom + 1) + " / " + (aaMaxZoom + 1));
			}

			start = System.nanoTime();
			xxx = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
			img = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g0 = img.createGraphics();

			g0.drawImage(xxx, 0, 0, scaledWidth, scaledHeight, 0, 0, scaledWidth, scaledHeight, null);
			g0.dispose();
			stop = System.nanoTime();
			System.out.println("scaling_image_" + zoom + ":" + ((double) (stop - start) / 1000000) + " ms");

			// System.out.println("zoom layer : " + zoom + " image size:" + img.getWidth() + "x" + img.getHeight());
			addLevelInfos(fileSansDot, mapID, zoom, scaledWidth, scaledHeight, nbX, nbY, 0, 0);
		}
		System.out.println(" ... setting tile info");
		setTileInfo("" + mapKey, tileType, tileSide, tileSide, null);
		System.out.println(" ... creating index");
		createIndexOnTileTable(connection, mapKey, layerKey);
		System.out.println("tiles created");

		doneCalculating = true;
	}

	public static void createIndexOnTileTable(Connection conn, long mapID, long layerID) {
		try {
			System.err.println("Quick fix : adding index for index_tiles_" + mapID + "_" + layerID);
			Statement st = conn.createStatement();
			st.execute("CREATE INDEX index_tiles_" + mapID + "_" + layerID + " ON tiles_" + mapID + "_" + layerID + "(z,y,x)");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setTileInfo(String mapID, String tileType, long tileWidth, long tileHeight, InputStream emptyTile) {
		try {
			PreparedStatement st = connection.prepareStatement("insert into table tile_info value (?, ?, ?, ?, ?)");
			// (mapKey, tileExt, tileWidth , tileHeight, emptyTile ):
			st.setString(0, "" + mapID);
			st.setString(1, tileType);
			st.setLong(2, tileWidth);
			st.setLong(3, tileHeight);
			st.setBlob(4, emptyTile);
			st.execute();
		} catch (SQLException e) {
			System.err.println("Information insertion failed.");
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		// "Beijing.pdf","Lijnennetkaartjul09kaartkant.pdf","allochrt.pdf",
		String[] files;
		// files = new String[] { "globcover_MOSAIC_H.png", "Beijingsubway2008.pdf", "CentraalStation09.pdf",
		// "GVBStopGo17mrt08.pdf", "Lijnennetkaartjul09kaartkant.pdf", "Meyrin_A3_Paysage.pdf",
		// "OpmNacht2009-06 Z los.pdf", "Prevessin_A3_Paysage.pdf", "WhyWeHere.pdf", "allochrt.pdf", "manbus.pdf" };
		files = new String[] { "globcover_MOSAIC_H.png" };// 
		files = new String[] { "manbus.pdf" };// , "Beijingsubway2008.pdf",
		// "Meyrin_A3_Paysage.pdf", "Prevessin_A3_Paysage.pdf", "manbus.pdf" };
		// default behavior:
		// manbus : 64 ~> 40 seconds on second iteration.
		// globcover : 20 ~> 14 seconds on second iteration.
		String destDir = "/Users/niko/tileSources/bench/";
		String src = "/Users/niko/tileSources/";
		// String file = ;
		// files = new String[]{"manbus.pdf"};
		SQliteTileCreator.loadLib();
		SQliteTileCreator creator = new SQliteTileCreator();
		long start, stop;
		int count = 10;
		for (int i = 0; i < count; i++) {
			for (String file : files) {
				creator.title = file.substring(0, file.lastIndexOf("."));
				System.out.println("Processing " + creator.title);
				String dstFile = destDir + creator.title + Ref.ext_db;
				File f = new File(dstFile);
				if (f.exists()) {
					System.out.println("removing this file : " + dstFile);
					f.delete();
				}
				start = System.nanoTime();
				creator.calculateTiles(destDir + creator.title + Ref.ext_db, src + file, 192, "png", new JProgressBar());
				creator.finalizeFile();
				stop = System.nanoTime();
				System.out.println("total_time: " + ((double) (stop - start) / 1000000) + " ms");
			}
		}
	}
}