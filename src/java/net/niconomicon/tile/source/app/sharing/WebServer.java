package net.niconomicon.tile.source.app.sharing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.niconomicon.tile.source.app.Ref;

/**
 * This program demonstrates a simple multi-threaded web server. A more advanced version of this server can be
 * implemented using NIO and/or thread pooling.
 */
public class WebServer {

	private ServerRunner server;
	private Thread serverThread;

	public static void main(String[] args) {
		// if (args.length != 3) {
		// System.err.println("Usage: WebServer <port> <document-dir> <log=true|false>");
		// } else {
		// int port = Integer.parseInt(args[0]);
		// File documentDir = new File(args[1]);
		// boolean log = Boolean.valueOf(args[2]).booleanValue();
		// httpd(port, documentDir, log);
		// }
		int port = Ref.sharing_port;
		Map<String, String> fileReferences;// = new HashMap<String, String>();
		// fileReferences.put("/"+Ref.sharing_xmlRef, Ref.sharing_xmlRef);
		//
		// fileReferences.put("/devilstower_pacholka_big.jpg" + Ref.ext_db,
		// "/Users/niko/Sites/testApp/mapRepository/devilstower_pacholka_big.jpg.mdb");
		// fileReferences.put("/devilstower_pacholka_big" + Ref.ext_mini,
		// "/Users/niko/Sites/testApp/mapRepository/devilstower_pacholka_big.jpg.mdb");
		// fileReferences.put("/devilstower_pacholka_big" + Ref.ext_thumb,
		// "/Users/niko/Sites/testApp/mapRepository/devilstower_pacholka_big.jpg.mdb");
		//
		// fileReferences.put("/ngc1097_spitzer_big.jpg" + Ref.ext_db,
		// "/Users/niko/Sites/testApp/mapRepository/ngc1097_spitzer_big.jpg.mdb");
		// fileReferences.put("/ngc1097_spitzer_big" + Ref.ext_mini,
		// "/Users/niko/Sites/testApp/mapRepository/ngc1097_spitzer_big.jpg.mdb");
		// fileReferences.put("/ngc1097_spitzer_big" + Ref.ext_thumb,
		// "/Users/niko/Sites/testApp/mapRepository/ngc1097_spitzer_big.jpg.mdb");
		//
		// fileReferences.put("/scienceMapPrintMockupEd2.jpg" + Ref.ext_db,
		// "/Users/niko/Sites/testApp/mapRepository/scienceMapPrintMockupEd2.jpg.mdb");
		// fileReferences.put("/scienceMapPrintMockupEd2" + Ref.ext_mini,
		// "/Users/niko/Sites/testApp/mapRepository/scienceMapPrintMockupEd2.jpg.mdb");
		// fileReferences.put("/scienceMapPrintMockupEd2" + Ref.ext_thumb,
		// "/Users/niko/Sites/testApp/mapRepository/scienceMapPrintMockupEd2.jpg.mdb");

		Set<String> set = new HashSet<String>();
		set.add("/Users/niko/Sites/testApp/mapRepository/tpg.png.mdb");

		fileReferences = Ref.generateXMLFromMapFileNames(set);
		System.out.println("mapFeed : ");
		System.out.println(fileReferences.get("/" + Ref.sharing_xmlRef));
		boolean log = Boolean.valueOf(args[2]).booleanValue();
		// MapSharingService service = MapSharingService.getInstance();
		// service.startSharing(port);
		// httpd(port, fileReferences, mapFeed, log);
		// service.stopSharing();

	}

	public void start(int port, Map<String, String> documentList) {
		stop();
		server = new ServerRunner(port, documentList, true);
		serverThread = new Thread(server);
		serverThread.start();
	}

	public void stop() {
		if (server != null) {
			try {
				server.serverSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private class ServerRunner implements Runnable {
		int port;
		Map<String, String> documentList;
		boolean log;
		ServerSocket serverSocket;

		public ServerRunner(int port, Map<String, String> documentList, boolean log) {
			this.port = port;
			this.documentList = documentList;
			this.log = log;
		}

		public void run() {
			try {
				serverSocket = new ServerSocket(port);
				System.out.println("Started a server on port " + port);
				try {
					while (true) {
						// wait for the next client to connect and get its
						// socket connection
						Socket socket = serverSocket.accept();
						// handle the socket connection by a handler in a new
						// thread
						new Thread(new WebRequestHandler(socket, documentList, log)).start();
					}
				} catch (IOException e) {
					System.out.println("IOException on a server on port " + port + " : " + e.getMessage());
				} finally {
					System.out.println("closing the server socket.");
					serverSocket.close();
				}
			} catch (IOException e) {
				System.err.println("Failed to bind to port " + port);
				e.printStackTrace();
			} catch (Exception ex) {
				System.err.println("message : " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}

}
