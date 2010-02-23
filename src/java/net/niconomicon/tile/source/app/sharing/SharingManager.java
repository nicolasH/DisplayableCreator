/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.sharing.server.MapSharingServiceAnnouncer;
import net.niconomicon.tile.source.app.sharing.server.WebServer;

/**
 * @author niko
 * 
 */
public class SharingManager {

	public static FilenameFilter filter;
	
	static {
		filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(Ref.ext_db);
			}
		};
	}
	int port = -1;
	MapSharingServiceAnnouncer sharingAnnouncer;
	WebServer sharingServer;
	Map<String, String> urlToFile;

//	public static void main(String[] args) {
//		if (args.length == 0) { // assumed to be started as a server.
//			System.out.println("arguments :");
//			int i = 0;
//			System.out.println(i++ + " : tile set directory");
//			System.out.println(i++ + " : port");
//			return;
//		}
//		String dirPath = args[0];
//		int port = Integer.parseInt(args[1]);
//		SharingManager manager = new SharingManager();
//		
//
//		File dir = new File(dirPath);
//		String[] children = dir.list();
//		children = dir.list(filter);
//
//		manager.setPort(port);
//		Arrays.sort(children);
//		List<String> fileList = new ArrayList(children.length);
//		for (String string : children) {
//			fileList.add(dirPath+string);
//		}
//		manager.setSharingList(fileList);
//		manager.startSharing();
//	}

	public SharingManager() {
		sharingAnnouncer = sharingAnnouncer.getInstance();
		sharingServer = new WebServer();
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSharing(){
		return sharingServer.isStarted();
	}
	
	public void setSharingList(Collection<String> sharedMaps) {
		System.out.println("should re-start sharing the maps if necessary.");
		urlToFile = Ref.generateXMLFromMapFileNames(sharedMaps);
//		System.out.println("Generated the XML : "+urlToFile);
		if (sharingServer.isStarted()) {
			sharingServer.stop();
			sharingServer.start(port, urlToFile);
		}
		System.out.println("shared maps :");
		// System.out.println(mapFeed);
	}

	public void startSharing() {
		System.out.println("supposedly starting to share on port :"+ port);
		sharingAnnouncer.startSharing(port);
		sharingServer.start(port, urlToFile);
	}

	public void stopSharing() {
		sharingAnnouncer.stopSharing();
		sharingServer.stop();
	}

}
