/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.io.File;

import net.niconomicon.tile.source.app.viewer.DisplayableSource;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableInfos implements Comparable<DisplayableInfos> {
	String title;
	String location;

	boolean shouldShare;
	long size;// in bytes;

	DisplayableSource source;

	public DisplayableInfos(String path, String title) {
		this.title = title;
		this.location = path;
		this.shouldShare = true;
		InfoLoader loader = new InfoLoader();
		loader.run();
	}

	public String getDescription() {
		return source.getDescription();
	}

	public String getTitle() {
		return source.getTitle();
	}

	public class InfoLoader implements Runnable {
		public void run() {
			File f = new File(location);
			size = f.length();
			// load the displayable infos from the file.
			source = new DisplayableSource(location, null, null);
		}
	}

	public int compareTo(DisplayableInfos o) {
		// return title.toLowerCase().compareTo(o.title.toLowerCase());
		if (title.compareToIgnoreCase(o.title) == 0) {
			return location.compareTo(o.location);
		} else {
			return title.compareToIgnoreCase(o.title);
		}
	}

	public String toString() {
		return "dispInfos : [" + location + "]{" + title + "}";
	}

	public String tooltip() {
		long s = size / 100;
		String w = "KB";
		if (s > 10000) {// MB Size Range
			s = s / 1000;
			w = "MB";
		}
		double si = s / 10.0;
		return "Weight : " + si + w + " Dimensions : " + source.getMaxInfo().width + " * " + source.getMaxInfo().height
				+ " pixels";
	}
}
