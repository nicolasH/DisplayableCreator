/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.tiling.FastClipper;
import net.niconomicon.tile.source.app.viewer.DisplayableView;

/**
 * @author Nicolas Hoibian
 * 
 */
public class FlipAndAddAction implements Runnable {
	byte[] tile;
	String key;
	ConcurrentHashMap cache;
	JPanel toRepaint;
	long x;
	long y;
	long z;

	public FlipAndAddAction(ConcurrentHashMap cache, byte[] tile, JPanel toRepaint, long x, long y, long z) {
		this.tile = tile;
		this.cache = cache;
		this.toRepaint = toRepaint;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			BufferedImage t = ImageIO.read(new ByteArrayInputStream(tile));
			t = FastClipper.fastClip(t, new Rectangle(0, 0, t.getWidth(), t.getHeight()), true);
			// System.out.println("key : " + key + " data " + t + " cache " + cache);
			cache.put(Ref.getKey(x, y, z), t);
			int tileSize = DisplayableView.tileSize;
			toRepaint.repaint((int) x * tileSize, (int) y * tileSize, t.getWidth(), t.getHeight());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
