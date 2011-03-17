/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.structs;

/**
 * @author Nicolas Hoibian
 * 
 */
public class TileCoord {
	public final int x, y, z;

	public TileCoord(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String toString() {
		return "z =" + z + " y=" + y + " x=" + x;
	}
}