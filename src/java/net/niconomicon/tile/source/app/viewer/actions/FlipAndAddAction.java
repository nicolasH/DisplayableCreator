/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import net.niconomicon.tile.source.app.tiling.FastClipper;

/**
 * @author Nicolas Hoibian
 * 
 */
public class FlipAndAddAction implements Runnable {
	byte[] tile;
	String key;
	ConcurrentHashMap cache;

	public FlipAndAddAction(ConcurrentHashMap cache, byte[] tile, String key) {
		this.tile = tile;
		this.key = key;
		this.cache = cache;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			BufferedImage t = ImageIO.read(new ByteArrayInputStream(tile));
			t = FastClipper.fastClip(t, new Rectangle(0, 0, t.getWidth(), t.getHeight()), true);
//			System.out.println("key : " + key + " data " + t + " cache " + cache);
			cache.put(key, t);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
