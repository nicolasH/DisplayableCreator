/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.util.Collection;

import net.niconomicon.tile.source.app.sharing.server.TilesetSharingServiceAnnouncer;
import net.niconomicon.tile.source.app.sharing.server.jetty.JettyImageServerServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author niko
 * 
 */
public class SharingManager {
	/*
		int port = -1;
		MapSharingServiceAnnouncer sharingAnnouncer;
		WebServer sharingServer;
	//	Map<String, String> urlToFile;

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
			urlToFile = Ref.generateIndexFromFileNames(sharedMaps);
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

		*/

	int port = -1;
	TilesetSharingServiceAnnouncer sharingAnnouncer;
	JettyImageServerServlet service;
	Server server;
	ServletContextHandler context;

	public SharingManager() {
		sharingAnnouncer = sharingAnnouncer.getInstance();
		service = new JettyImageServerServlet();

		context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(new ServletHolder(service), "/*");

	}

	public void setPort(int port) throws Exception {
		this.port = port;
		if (null != server && server.isRunning()) {
			startSharing();
		}
	}

	public boolean isSharing() {
		if(null != server)
		{		return server.isRunning();}
		return false;
	}

	public void setSharingList(Collection<String> sharedMaps) {
		service.addImages(sharedMaps);
	}

	public void startSharing() throws Exception {
		System.out.println("supposedly starting to share on port :" + port);
		
		sharingAnnouncer.startSharing(port);
		if (null != server && server.isRunning()) {
			server.stop();
		}
		server = new Server(port);
		server.setHandler(context);
		server.start();
	}

	public void stopSharing() throws Exception {
		sharingAnnouncer.stopSharing();
		server.stop();
	}

}
