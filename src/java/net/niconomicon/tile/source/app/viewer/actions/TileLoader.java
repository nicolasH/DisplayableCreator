/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

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
	JPanel toRefresh;

	public TileLoader(Connection connection, int startY, int z, ConcurrentHashMap cache, JPanel toRefresh, ExecutorService exe) {
		this.connection = connection;
		this.cache = cache;
		this.startY = startY;
		this.z = z;
		this.exe = exe;
		this.toRefresh = toRefresh;
	}

	public void run() {
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from tiles_0_0 where z=" + z + " and y=" + startY);
			while (rs.next()) {
				long x = rs.getLong(1);
				long y = rs.getLong(2);
				long z = rs.getLong(3);
				// System.out.println("found a tile for " + x + " " + y + " " + z);
				byte[] data = rs.getBytes(4);
				exe.execute(new FlipAndAddAction(cache, data, toRefresh, x, y, z));
			}

			// exe.awaitTermination(2, TimeUnit.MINUTES);
			System.out.println("Loading done for y = " + startY);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
