/**
 * 
 */
package net.niconomicon.tile.source.app.viewer;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JLabel;

import net.niconomicon.tile.source.app.DisplayableCreatorApp;
import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.viewer.actions.SingleTileLoader;
import net.niconomicon.tile.source.app.viewer.trivia.TileCoord;
import net.niconomicon.tile.source.app.viewer.trivia.ZoomLevel;

/**
 * @author Nicolas Hoibian Provides facilities to read a Displayable file.
 */
public class DisplayableSource {

	Connection mapDB;
	public static final int tileSize = 192;

	public static final String getTilesInRange = "select * from tiles_0_0 where x >= ? and x <= ? and y >=? and y <=? and z=?";

	PreparedStatement tilesInRange;
	DisplayableView view;

	ConcurrentLinkedQueue<TileCoord> neededTiles;
	Map<String, BufferedImage> cache;

	List<ZoomLevel> levels;
	ExecutorService tileLoader;
	Timer loader;

	public DisplayableSource(String tileSourcePath, JLabel loadingLabel, DisplayableView view) {
		tileLoader = Executors.newFixedThreadPool(DisplayableCreatorApp.ThreadCount / 2);
		neededTiles = new ConcurrentLinkedQueue<TileCoord>();
		this.view = view;
		LinkedHashMap<String, BufferedImage> cacheImpl = new LinkedHashMap<String, BufferedImage>(200) {
			protected boolean removeEldestEntry(java.util.Map.Entry<String, BufferedImage> eldest) {
				return size() == 200;
			}
		};

		cache = Collections.synchronizedMap(cacheImpl);
		loadInfos(tileSourcePath, loadingLabel);

		loader = new Timer();
		loader.schedule(new LiveCacheLoader(), 1000, 1000);

	}

	public BufferedImage getImage(int x, int y, int z) {
		BufferedImage t = cache.get(Ref.getKey(x, y, z));
		if (null == t) {
			neededTiles.add(new TileCoord(x, y, z));
			return null;
		} else {
			return t;
		}
	}

	public void setImage(long x, long y, long z, BufferedImage im) {
		cache.put(Ref.getKey(x, y, z), im);
		view.repaintTile(x, y, z);
	}

	public class LiveCacheLoader extends TimerTask {

		public void run() {
			TileCoord c = neededTiles.poll();
			while (c != null) {
				SingleTileLoader loader = new SingleTileLoader(mapDB, c, DisplayableSource.this);
				tileLoader.submit(loader);
				c = neededTiles.poll();
			}
		}
	}

	public void loadInfos(String tileSourcePath, JLabel loadingLabel) {
		if (cache != null) {
			cache.clear();
		}
		if (neededTiles != null) {
			neededTiles.clear();
		}
		levels = new ArrayList<ZoomLevel>();
		try {
			System.out.println("trying to open the map : " + tileSourcePath);
			mapDB = DriverManager.getConnection("jdbc:sqlite:" + tileSourcePath);
			mapDB.setReadOnly(true);

			Statement statement = mapDB.createStatement();
			// zoom = 0;
			ResultSet rs = statement.executeQuery("select * from " + Ref.layers_infos_table_name);

			int totalTiles = 0;
			while (rs.next()) {
				ZoomLevel zl = new ZoomLevel();
				zl.width = rs.getLong("width");
				zl.height = rs.getLong("height");
				zl.tiles_x = rs.getLong("tiles_x");
				zl.tiles_y = rs.getLong("tiles_y");
				zl.z = rs.getInt("zoom");
				System.out.println("Tiles for level " + zl.z + " : " + zl.tiles_x * zl.tiles_y);
				totalTiles += zl.tiles_x * zl.tiles_y;
				levels.add(zl.z, zl);
			}
			ZoomLevel currentLevel = levels.get(levels.size() - 1);
			System.out.println("Setting current level to " + currentLevel.z + " total tiles : " + totalTiles);
			cache = new ConcurrentHashMap<String, BufferedImage>(totalTiles, 1.0f);
			if (view != null) {
				view.resetSizeEtc(currentLevel);
			}
			System.out.println("fully cached !");
		} catch (Exception ex) {
			System.err.println("ex for map : " + tileSourcePath);
			ex.printStackTrace();
		}
	}

	public List<ZoomLevel> getILevelInfos() {
		return levels;
	}

	public int getMaxZ() {
		return levels.size();
	}

	public ZoomLevel getMaxInfo() {
		return levels.get(0);
	}

	public void done() {
		try {
			mapDB.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
