/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.TileCreatorApp;
import net.niconomicon.tile.source.app.viewer.actions.TileLoader;

/**
 * @author niko
 * 
 */
public class ImageTileSetPanel extends JPanel {
	Connection mapDB;
	public static final int tileSize = 192;

	public static final String getTilesInRange = "select * from tiles_0_0 where x >= ? and x <= ? and y >=? and y <=? and z=?";

	PreparedStatement tilesInRange;

	public ConcurrentHashMap<String, BufferedImage> cache;
	// int maxX = 0;
	// int maxY = 0;
	// int zoom = 0;
	List<ZoomLevel> levels;

	ZoomLevel currentLevel;

	public ImageTileSetPanel() {
		super();
		cache = new ConcurrentHashMap<String, BufferedImage>();
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public class ZoomLevel {
		long width;
		long height;
		long tiles_x;
		long tiles_y;
		int z;
	}

	public void setTileSource(String tileSourcePath) {
		// this.getParent().add(toolBar);
		if (cache != null) {
			cache.clear();
		}
		levels = new ArrayList<ZoomLevel>();
		ExecutorService exe = Executors.newFixedThreadPool(TileCreatorApp.ThreadCount / 2);
		ExecutorService eye = Executors.newFixedThreadPool(TileCreatorApp.ThreadCount);
		try {
			System.out.println("trying to open the map : " + tileSourcePath);
			mapDB = DriverManager.getConnection("jdbc:sqlite:" + tileSourcePath);
			mapDB.setReadOnly(true);

			Statement statement = mapDB.createStatement();
			// zoom = 0;
			int maxZoom = 0;
			ResultSet rs = statement.executeQuery("select * from " + Ref.layers_infos_table_name);

			long stop, start;
			start = System.currentTimeMillis();
			while (rs.next()) {
				ZoomLevel zl = new ZoomLevel();
				zl.width = rs.getLong("width");
				zl.height = rs.getLong("height");
				zl.tiles_x = rs.getLong("tiles_x");
				zl.tiles_y = rs.getLong("tiles_y");
				zl.z = rs.getInt("zoom");
				levels.add(zl.z, zl);
			}
			currentLevel = levels.get(levels.size() - 1);
			System.out.println("Setting current level to " + currentLevel.z);
			resetSizeEtc(currentLevel);
			for (ZoomLevel zl : levels) {
				System.out.println("-- zl : " + zl.z);
				for (int i = 0; i < zl.tiles_x; i++) {
					TileLoader loader = new TileLoader(mapDB, i, zl.z, cache, this, exe);
					eye.execute(loader);
				}
			}
			eye.shutdown();
			boolean normalTY = eye.awaitTermination(5, TimeUnit.MINUTES);
			exe.shutdown();
			boolean normalTX = eye.awaitTermination(5, TimeUnit.MINUTES);
			stop = System.currentTimeMillis();
			// System.out.println("Caching took " + (stop - start) + " ms normal x " + normalTX + " normal y " +
			// normalTY);

			mapDB.close();

			System.out.println("fully cached !");
			resetSizeEtc(currentLevel);
		} catch (Exception ex) {
			System.err.println("ex for map : " + tileSourcePath);
			ex.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// System.out.println("paintComponent");
		Graphics2D g2 = (Graphics2D) g;
		Rectangle r = g2.getClipBounds();
		int tileXa = r.x / tileSize;
		int tileXb = tileXa + (int) (((double) r.width / (double) tileSize)) + 2;
		int tileYa = r.y / tileSize;
		int tileYb = tileYa + (int) (((double) r.height / (double) tileSize)) + 1;

		// System.out.println("Painting between " + tileXa + "," + tileYa + "and " + tileXb + ", " + tileYb);
		try {
			int macYb = ((int) currentLevel.tiles_x - 1 - tileYa);
			int macYa = ((int) currentLevel.tiles_y - 1 - tileYb);

			macYa = tileYa;
			macYb = tileYb;
			for (int x = tileXa; x < tileXb; x++) {
				for (int y = macYa; y < macYb + 1; y++) {
					BufferedImage tile = cache.get(x + "_" + y + "_" + currentLevel.z);
					if (null != tile) {
						g2.drawImage(tile, x * tileSize, (y) * tileSize, null);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		g2.dispose();
	}

	public void resetSizeEtc(ZoomLevel zl) {
		System.out.println("Gonna invalidate to zoom level " + zl);
		this.setSize((int) zl.width, (int) zl.height);
		this.setMinimumSize(new Dimension((int) zl.width, (int) zl.height));
		this.setPreferredSize(new Dimension((int) zl.width, (int) zl.height));

		invalidate();
		// revalidate();
		repaint();

	}

	public void incrZ() {
		if (currentLevel.z > 0) {
			ZoomLevel zl = levels.get(currentLevel.z - 1);
			currentLevel = zl;
			resetSizeEtc(zl);
		} else {
			System.out.println("Already at max Zoom");
		}
	}

	public void decrZ() {
		if (currentLevel.z < levels.size() - 1) {
			ZoomLevel zl = levels.get(currentLevel.z + 1);
			currentLevel = zl;
			resetSizeEtc(zl);
		} else {
			System.out.println("Already at min Zoom");
		}
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

}
