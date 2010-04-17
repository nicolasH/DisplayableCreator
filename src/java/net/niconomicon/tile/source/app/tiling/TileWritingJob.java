/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author niko
 * 
 */
public class TileWritingJob implements Runnable {
	long x;
	long y;
	long z;
	byte[] bytes;
	Connection connection;
	PreparedStatement insertTile;

	public TileWritingJob(long x, long y, long z, ByteArrayOutputStream data, Connection co, PreparedStatement st) {
		this.x = x;
		this.y = y;
		this.z = z;
		bytes = data.toByteArray();
		this.connection = co;
		this.insertTile = st;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// TODO Auto-generated method stub
		long stop, start;
		start = System.nanoTime();
		// String stat = "insert into ? values(" + x + "," + y + "," + z + ",?)";
		// System.out.println(stat);
		try {
			insertTile.setLong(1, x);
			insertTile.setLong(2, y);
			insertTile.setLong(3, z);

			insertTile.setBytes(4, bytes);
			insertTile.executeUpdate();
			System.out.println(x + "_" + y + "_" + z );
		} catch (SQLException e) {
			System.err.println("Export failed for "+x + "," + y + "," + z );
			e.printStackTrace();
		}
		stop = System.nanoTime();
		// System.out.println("writing_tile: " + ((double) (stop - start) / 1000000) + " ms");
	}
}
