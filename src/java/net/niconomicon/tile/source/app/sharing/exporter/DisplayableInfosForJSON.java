/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.exporter;

import com.google.gson.Gson;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DisplayableInfosForJSON {

	String title;
	String source;
	String thumb;
	String preview;
	long weight;
	long width;
	long height;
	String description;

	public DisplayableInfosForJSON(String title, String name, String thumb, String mini, long weight, long width, long height, String description) {

		this.title = title;

		this.description = description;

		this.source = name;
		this.preview = mini;
		this.thumb = thumb;

		this.weight = weight;
		this.height = height;
		this.width = width;
	}

	public static String toJSON(Gson jsonConverter, DisplayableInfosForJSON disp){
		return jsonConverter.toJson(disp);
	}
}
