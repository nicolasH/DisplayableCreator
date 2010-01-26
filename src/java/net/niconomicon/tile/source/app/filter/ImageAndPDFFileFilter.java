/**
 * 
 */
package net.niconomicon.tile.source.app.filter;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

/**
 * @author niko
 * 
 */
public class ImageAndPDFFileFilter extends FileFilter {

	public final Set<String> extensions = new HashSet<String>(Arrays.asList("png", "gif", "pdf", "jpeg", "jpg", "bmp"));

	public boolean accept(File f) {
		if (f.isDirectory()) { return true; }

		if (extensions.contains(getLowerCaseExt(f))) { return true; }

		return false;
	}

	public String getDescription() {
		return "Supported source images";
	}

	/**
	 * 
	 * @param File
	 *            the file
	 * @return the file's extension in lowercase, without the "."
	 */
	public static String getLowerCaseExt(File f) {
		return getLowerCaseExt(f.getName());
	}

	/**
	 * 
	 * @param String
	 *            a file name
	 * @return the file's extension in lowercase, without the "."
	 */
	public static String getLowerCaseExt(String s) {
		String ext = null;
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}
