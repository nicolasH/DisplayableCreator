/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.io.File;

import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
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
		return title.toLowerCase().compareTo(o.title.toLowerCase());
	}

	public String tooltip() {
		float s = size / 1024;
		String w = "KB";
		if (s > 1024.0f) {
			s = s / 1024.0f;
			w = "MB";
		}
		s = s * 10;// 1.0045678 -> 10.04
		int t = (int) s;
		s = t / 10;
		return "Weight : " + s + w + " Dimensions : " + source.getMaxInfo().width + " * " + source.getMaxInfo().height + " pixels";
	}
}
