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

import net.niconomicon.tile.source.app.tiling.FastClipper;
import net.niconomicon.tile.source.app.viewer.DisplayableSource;
import net.niconomicon.tile.source.app.viewer.structs.TileCoord;

/**
 * @author Nicolas Hoibian
 * 
 */
public class SingleTileLoader implements Runnable {

	TileCoord coord;
	DisplayableSource displayableSource;
	Connection displayable;
	int tileType;

	public SingleTileLoader(Connection displayable, TileCoord coord, DisplayableSource source, int tileType) {
		this.displayableSource = source;
		this.coord = coord;
		this.displayable = displayable;
		this.tileType = tileType;
	}

	public void run() {
		// long start, stop;
		// start = System.currentTimeMillis();
		int count = 0;
		if (displayableSource.hasImage(coord)) { return; }
		try {
			Statement statement = displayable.createStatement();
			ResultSet rs = statement.executeQuery("select * from tiles_0_0 where z=" + coord.z + " and y=" + coord.y + " and x=" + coord.x);
			while (rs.next()) {
				count++;
				long x = rs.getLong(1);
				long y = rs.getLong(2);
				long z = rs.getLong(3);
				// System.out.println("found a tile for " + x + " " + y + " " + z);
				byte[] data = rs.getBytes(4);
				// cache.put(Ref.getKey(x, y, z), data);
				try {
					BufferedImage t = ImageIO.read(new ByteArrayInputStream(data));
					// System.out.println("Getting type for loaded tile : : " + t.getType());
					t = FastClipper.fastClip(t, new Rectangle(0, 0, t.getWidth(), t.getHeight()), true, tileType);
					// System.out.println("key : " + key + " data " + t + " cache " + cache);
					displayableSource.setImage(x, y, z, t);
					// int tileSize = DisplayableView.tileSize;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// stop = System.currentTimeMillis();
		// System.out.println("Loading done (count = " + count + " time : " + (stop - start) + " ) for z = " + coord.z +
		// " y = " + coord.y + " x = " + coord.x);

	}

	/**
	 * Possible workaround for a bug found on windows 7 + Java 1.6.0_17 b04 where the type of a tile loaded from the db
	 * is TYPE_CUSTOM
	 */
	public static final int[] imageTypes = new int[] { BufferedImage.TYPE_CUSTOM, // 0
	BufferedImage.TYPE_INT_RGB, // 1
	BufferedImage.TYPE_INT_ARGB, // 2
	BufferedImage.TYPE_INT_ARGB_PRE,// 3
	BufferedImage.TYPE_INT_BGR, // 4
	BufferedImage.TYPE_3BYTE_BGR, // 5 //png
	BufferedImage.TYPE_4BYTE_ABGR, // 6 //png
	BufferedImage.TYPE_4BYTE_ABGR_PRE,// 7 //png?
	BufferedImage.TYPE_USHORT_565_RGB, // 8
	BufferedImage.TYPE_USHORT_555_RGB, // 9
	BufferedImage.TYPE_BYTE_GRAY, // 10
	BufferedImage.TYPE_USHORT_GRAY,// 11
	BufferedImage.TYPE_BYTE_BINARY,// 12
	BufferedImage.TYPE_BYTE_INDEXED }; // 13

	public static int getPossibleType(Connection conn) {
		int type = -1;
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("select * from tiles_0_0 where z=0 and x=0 and y=0");
			while (rs.next()) {
				long x = rs.getLong(1);
				long y = rs.getLong(2);
				long z = rs.getLong(3);
				System.out.println("found a tile for " + x + " " + y + " " + z);
				byte[] data = rs.getBytes(4);

				try {
					BufferedImage t = ImageIO.read(new ByteArrayInputStream(data));
					if (t.getType() == BufferedImage.TYPE_CUSTOM) {
						System.out.println("Getting type for loaded tile : : " + t.getType());
						for (int i = 0; i < imageTypes.length; i++) {
							type = imageTypes[i];
							try {
								t = FastClipper.fastClip(t, new Rectangle(0, 0, t.getWidth(), t.getHeight()), true, type);
								System.out.println("Succeeded with type " + type);
								return type;
							} catch (Exception ex) {
								System.out.println("Exception with type " + type);
								// ex.printStackTrace();
							}
						}
					} else {
						return t.getType();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return type;
	}
}
