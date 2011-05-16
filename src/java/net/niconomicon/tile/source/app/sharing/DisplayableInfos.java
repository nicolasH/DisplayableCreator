/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.io.File;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableInfos implements Comparable<DisplayableInfos> {
	String title;
	String location;
	boolean shouldShare;
	long size;// in bytes;
	long width; // in pixels
	long height; // in pixels

	public DisplayableInfos(String path, String title) {
		this.title = title;
		this.location = path;
		this.shouldShare = true;
	}

	public class InfoLoader implements Runnable {
		public void run() {
			File f = new File(location);
			size = f.length();
			// load the displayable infos from the file.
		}
	}

	public int compareTo(DisplayableInfos o) {
		return title.toLowerCase().compareTo(o.title.toLowerCase());
	}

	public String tooltip() {
		return "Weight : " + size + " Dimensions : " + width + " * " + height + " pixels";
	}
}
