/**
 * 
 */
package net.niconomicon.tile.source.app;

import javax.swing.ImageIcon;

import net.niconomicon.tile.source.app.viewer.TilingPreview;

/**
 * @author niko
 * 
 */
public class Communicator {

	TilingPreview preview;

	public Communicator(TilingPreview preview) {
		this.preview = preview;
	}

	public void doneTile(int x, int y, int z) {
		String s = x + "_" + y + "_" + z;
		preview.addTile(s);
	}

	public void doingLevel(int level) {
		preview.doingLevel(level);
	}

	public void setMiniature(ImageIcon mini) {
		preview.setMiniature(mini);
	}

}
