/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.exporter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author Nicolas Hoibian
 * 
 *         The aim of this class is to export the JSON feed, the html page, the thumbnails, the minatures and the .disp
 *         files into a single zip or tar file so that the user can unzip it into a web page.
 * 
 */
public class JSONArchiveExporter {

	public void export(List files) {
		Map<String, String> m = Ref.generateIndexFromFileNames(files);
		for (Entry entry : m.entrySet()) {
			System.out.println("Entry : " + entry.getKey());
			System.out.println("      : " + entry.getValue());
		}
	}
}
