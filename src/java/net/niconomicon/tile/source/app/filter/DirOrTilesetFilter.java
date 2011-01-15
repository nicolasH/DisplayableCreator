/**
 * 
 */
package net.niconomicon.tile.source.app.filter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DirOrTilesetFilter extends FileFilter {

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) { return true; }
		if (f.getName().toLowerCase().endsWith(Ref.ext_db.toLowerCase())) { return true; }
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return "TileSet or directory";
	}
}
