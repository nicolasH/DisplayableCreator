package net.niconomicon.tile.source.app.tiling.parallel;

import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;

public class TileJobWriteSynchronized extends TileJobWrite {
	public long x;
	public long y;
	public long z;
	public static final String lock = "lock";

	PreparedStatement insertTile;
	public ByteArrayOutputStream data;

	public TileJobWriteSynchronized(long x, long y, long z, PreparedStatement tileSt) {
		super(x, y, z, tileSt);
	}

	public void run() {
		synchronized (lock) {
			super.run();
		}
	}
	
}
