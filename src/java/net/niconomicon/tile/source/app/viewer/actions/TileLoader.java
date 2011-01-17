/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Nicolas Hoibian
 * 
 */
public class TileLoader implements Runnable {

	Connection connection;
	int startY;
	int z;
	ConcurrentHashMap cache;
	ExecutorService exe;

	public TileLoader(Connection connection, int startY, int z, ConcurrentHashMap cache, ExecutorService exe) {
		this.connection = connection;
		this.cache = cache;
		this.startY = startY;
		this.z = z;
		this.exe = exe;
	}

	public void run() {
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from tiles_0_0 where z=" + z + " and y=" + startY);

			while (rs.next()) {
				int x = rs.getInt(1);
				int y = rs.getInt(2);
				int z = rs.getInt(3);
				// System.out.println("found a tile for " + x + " " + y + " " + z);
				byte[] data = rs.getBytes(4);
				String key = x + "_" + y + "_" + z;
				exe.execute(new FlipAndAddAction(cache, data, key));
			}
			// exe.awaitTermination(1, TimeUnit.SECONDS);
			// System.out.println("Caching done.");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
