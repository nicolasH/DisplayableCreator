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
public class DisplayableFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return isProbablyDisplayableFile(f);
	}

	@Override
	public String getDescription() {
		return "Displayable file";
	}

	public static boolean isProbablyDisplayableFile(File f) {
		if (f.getName().toLowerCase().endsWith(Ref.ext_db.toLowerCase())) { return true; }
		return false;
	}
}