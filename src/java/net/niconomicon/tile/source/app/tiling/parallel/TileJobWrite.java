package net.niconomicon.tile.source.app.tiling.parallel;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TileJobWrite implements Runnable {
	public long x;
	public long y;
	public long z;

	PreparedStatement insertTile;
	public ByteArrayOutputStream data;

	public TileJobWrite(long x, long y, long z, PreparedStatement tileSt) {
		this.insertTile = tileSt;

		this.x = x;
		this.y = y;
		this.z = z;

	}

	public void run() {
		// TODO Auto-generated method stub
		long stop, start;
		start = System.nanoTime();
		// String stat = "insert into ? values(" + x + "," + y + "," + z +
		// ",?)";
		// System.out.println(stat);
		try {
			insertTile.setLong(1, x);
			insertTile.setLong(2, y);
			insertTile.setLong(3, z);

			insertTile.setBytes(4, data.toByteArray());
			insertTile.executeUpdate();
			// System.out.println(x + "_" + y + "_" + z );
		} catch (SQLException e) {
			System.err.println("Export failed for " + x + "," + y + "," + z);
			e.printStackTrace();
		}
		stop = System.nanoTime();
		// System.out.println("writing_tile: " + ((double) (stop - start) /
		// 1000000) + " ms");
	}
}
