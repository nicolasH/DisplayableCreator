/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.TileCreatorApp;
import net.niconomicon.tile.source.app.tiling.FastClipper;

/**
 * @author niko
 * 
 */
public class ImageTileSetPanel extends JPanel {
	Connection mapDB;
	public static final int tileSize = 192;

	public static final String getTilesInRange = "select * from tiles_0_0 where x >= ? and x <= ? and y >=? and y <=? and z=?";

	PreparedStatement tilesInRange;

	// public Map<String, byte[]> cache;
	public Map<String, BufferedImage> cache;
	int maxX = 0;
	int maxY = 0;
	int zoom = 0;

	ExecutorService exe;

	public ImageTileSetPanel() {
		super();
		cache = new HashMap<String, BufferedImage>();
		try {
			Class.forName("org.sqlite.JDBC");
			// System.out.println("Loaded the sqliteJDBC Driver class ?");
			// mapDB = DriverManager.getConnection("jdbc:sqlite:memory");
			// mapDB.close();
			// this.setDoubleBuffered(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		exe = Executors.newFixedThreadPool(TileCreatorApp.ThreadCount);
	}

	public void setTileSource(String tileSourcePath) {
		if (cache != null) {
			cache.clear();
		}
		try {
			System.out.println("trying to open the map : " + tileSourcePath);
			mapDB = DriverManager.getConnection("jdbc:sqlite:" + tileSourcePath);
			mapDB.setReadOnly(true);
			tilesInRange = mapDB.prepareStatement(getTilesInRange);
			tilesInRange.clearParameters();
			// String infos0 = "select * from layers_infos where zoom = 0";
			Statement statement = mapDB.createStatement();
			zoom = 0;
			ResultSet rs = statement.executeQuery("select * from " + Ref.layers_infos_table_name + " where zoom=" + zoom);

			while (rs.next()) {
				int width = rs.getInt("width");
				int height = rs.getInt("height");
				maxX = rs.getInt("tiles_x");
				maxY = rs.getInt("tiles_y");

				this.setSize(width, height);
				this.setMinimumSize(new Dimension(width, height));
				this.setPreferredSize(new Dimension(width, height));
			}
			// cache = new HashMap<String, byte[]>();
			setupCacheForTiles(zoom);
			revalidate();
		} catch (Exception ex) {
			System.err.println("ex for map : " + tileSourcePath);
			ex.printStackTrace();
		}
	}

	public void setupCacheForTiles(int zoom) throws Exception {
		tilesInRange.setInt(1, 0);
		tilesInRange.setInt(2, maxX);
		tilesInRange.setInt(3, 0);
		tilesInRange.setInt(4, maxY + 1);
		tilesInRange.setInt(5, zoom);
		// BufferedInputStream
		ResultSet rs = tilesInRange.executeQuery();
		System.out.println("Caching ...");
		while (rs.next()) {
			int x = rs.getInt(1);
			int y = rs.getInt(2);
			int z = rs.getInt(3);
			// System.out.println("found a tile for " + x + " " + y + " " + z);
			byte[] data = rs.getBytes(4);
			String key = x + "_" + y + "_" + z;
			exe.execute(new FlipAndAddAction(data, key));
		}
		exe.awaitTermination(2, TimeUnit.MINUTES);
		System.out.println("Caching done.");
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// System.out.println("paintComponent");
		Graphics2D g2 = (Graphics2D) g;
		Rectangle r = g2.getClipBounds();
		int tileXa = r.x / tileSize;
		int tileXb = tileXa + r.width / tileSize + 2;
		int tileYa = r.y / tileSize;
		int tileYb = tileYa + r.height / tileSize + 1;

		// System.out.println("Painting between " + tileXa + "," + tileYa + "and " + tileXb + ", " + tileYb);
		try {
			int macYb = (maxY - 1 - tileYa);
			int macYa = (maxY - 1 - tileYb);

			macYa = tileYa;
			macYb = tileYb;
			for (int x = tileXa; x < tileXb; x++) {
				for (int y = macYa; y < macYb + 1; y++) {
					// byte[] data = cache.get(x + "_" + y + "_" + zoom);
					BufferedImage tile = cache.get(x + "_" + y + "_" + zoom);
					// if (null != data) {
					if (null != tile) {
						// BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
						// g2.drawImage(image, x * tileSize, (maxY - 1 - y) * tileSize, null);
						// g2.scale(1, 1);
						// g2.drawImage(tile, x * tileSize, (maxY - 1 - y) * tileSize, null);
						// System.out.println("painting " + x + "_" + y + " at " + x * tileSize +" "+( maxY - 1 - y) *
						// tileSize);
						g2.drawImage(tile, x * tileSize, (y) * tileSize, null);
					} else {
						// System.out.println("tile is null for : "+x + "_" + y + "_" + zoom);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		g2.dispose();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dir = "";// /Users/niko/tileSources/mapRepository/
		String file = "test.mdb";
		dir = "/Users/niko/tileSources/serving/";
		file = "busRomaCenter.mdb";
		if (args.length == 1) {
			dir = "";
			file = args[0];
		}
		ImageTileSetPanel mV = new ImageTileSetPanel();
		mV.setTileSource(dir + file);
		JScrollPane p = new JScrollPane(mV);
		JFrame frame = new JFrame("Map Viewer");
		frame.setContentPane(p);
		// frame.setContentPane(new JPanel(new BorderLayout()));
		// frame.getContentPane().add(mV, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private class FlipAndAddAction implements Runnable {
		byte[] tile;
		String key;

		public FlipAndAddAction(byte[] tile, String key) {
			this.tile = tile;
			this.key = key;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				BufferedImage t = ImageIO.read(new ByteArrayInputStream(tile));
				t = FastClipper.fastClip(t, new Rectangle(0, 0, t.getWidth(), t.getHeight()), true);
				synchronized (cache) {
					cache.put(key, t);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
