/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.imageio.ImageIO;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.tiling.FastClipper;
import net.niconomicon.tile.source.app.viewer.DisplayableSource;
import net.niconomicon.tile.source.app.viewer.DisplayableView;
import net.niconomicon.tile.source.app.viewer.structs.TileCoord;

/**
 * @author Nicolas Hoibian
 * 
 */
public class SingleTileLoader implements Runnable {

	TileCoord coord;
	DisplayableSource displayableSource;
	Connection displayable;

	public SingleTileLoader(Connection displayable, TileCoord coord, DisplayableSource source) {
		this.displayableSource = source;
		this.coord = coord;
		this.displayable = displayable;
	}

	public void run() {
		if (displayableSource.hasImage(coord)) { return; }
		try {
			Statement statement = displayable.createStatement();
			ResultSet rs = statement.executeQuery("select * from tiles_0_0 where z=" + coord.z + " and y=" + coord.y + " and x=" + coord.x);
			int count = 0;
			while (rs.next()) {
				count ++;
				long x = rs.getLong(1);
				long y = rs.getLong(2);
				long z = rs.getLong(3);
				System.out.println("found a tile for " + x + " " + y + " " + z);
				byte[] data = rs.getBytes(4);
				// cache.put(Ref.getKey(x, y, z), data);
				try {
					BufferedImage t = ImageIO.read(new ByteArrayInputStream(data));
					t = FastClipper.fastClip(t, new Rectangle(0, 0, t.getWidth(), t.getHeight()), true);
					// System.out.println("key : " + key + " data " + t + " cache " + cache);
					displayableSource.setImage(x, y, z, t);
//					int tileSize = DisplayableView.tileSize;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			System.out.println("Loading done (count = "+count+") for z = " + coord.z + " y = " + coord.y + " x = " + coord.x);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
