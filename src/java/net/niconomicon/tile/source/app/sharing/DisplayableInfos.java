/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;


/**
 * @author Nicolas Hoibian
 *
 */
public class DisplayableInfos implements Comparable<DisplayableInfos>{
	String title;
	String location;
	boolean shouldShare;

	public DisplayableInfos(String path, String title) {
		this.title = title;
		this.location = path;
		this.shouldShare = true;
	}

	public int compareTo(DisplayableInfos o) {
		return title.toLowerCase().compareTo(o.title.toLowerCase());
	}
}
