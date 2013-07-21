package net.niconomicon.tile.source.app.tools;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.viewer.actions.SingleTileLoader;
import net.niconomicon.tile.source.app.viewer.structs.TileCoord;
import net.niconomicon.tile.source.app.viewer.structs.ZoomLevel;

public class DisplayableSourceBase {

	Connection mapDB;
	String title;
	String description;

	public static final String getTilesInRange = "select * from tiles_0_0 where x >= ? and x <= ? and y >=? and y <=? and z=?";

	int type = -1;
	Dimension tileSize = null;
	List<ZoomLevel> levels;
	String currentTileCoord;
	BufferedImage currentTile;

	public DisplayableSourceBase(String tileSourcePath){
		loadInfos(tileSourcePath);
	}
	public void loadInfos(String tileSourcePath) {

		levels = new ArrayList<ZoomLevel>();
		try {
			System.out.println("trying to open the map : " + tileSourcePath);
			mapDB = DriverManager.getConnection("jdbc:sqlite:" + tileSourcePath);
			mapDB.setReadOnly(true);
			type = SingleTileLoader.getPossibleType(mapDB);
			tileSize = SingleTileLoader.getTileSize(mapDB);

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
				// System.out.println("Tiles for level " + zl.z + " : " +
				// zl.tiles_x * zl.tiles_y);
				totalTiles += zl.tiles_x * zl.tiles_y;
				levels.add(zl.z, zl);
			}
			ZoomLevel currentLevel = levels.get(levels.size() - 1);
			// System.out.println("Setting current level to " + currentLevel.z +
			// " total tiles : " + totalTiles);
			rs = statement.executeQuery("select * from infos");
			while (rs.next()) {
				title = rs.getString("title");
				description = rs.getString("description");
			}

			// System.out.println("fully cached !");
		} catch (Exception ex) {
			System.err.println("ex for map : " + tileSourcePath);
			ex.printStackTrace();
		}
	}

	public boolean hasImage(TileCoord coord) {
		return false;
	}

	public void setImage(long x, long y, long z, BufferedImage im) {
		currentTileCoord = Ref.getKey(x, y, z);
		currentTile = im;
	}

	public BufferedImage getImage(int x, int y, int z) {
		String k = Ref.getKey(x, y, z);
		TileCoord c = new TileCoord(x, y, z);
		SingleTileLoader loader = new SingleTileLoader(mapDB, c, this, type);
		loader.run();
		return currentTile;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
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
}
