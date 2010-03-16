package net.niconomicon.tile.source.app.sharing.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
		int port = Ref.sharing_port;
		Map<String, String> fileReferences;// = new HashMap<String, String>();
		Set<String> set = new HashSet<String>();
		set.add("/Users/niko/Sites/testApp/mapRepository/tpg.png.mdb");

		fileReferences = Ref.generateIndexFromFileNames(set);
		System.out.println("mapFeed : ");
		System.out.println(fileReferences.get("/" + Ref.sharing_xmlRef));
		boolean log = Boolean.valueOf(args[2]).booleanValue();
	}

	public boolean isStarted() {
		return !(null == server);
	}

	public void start(int port, Map<String, String> documentList) {
		stop();
		System.out.println("STARTING !!!!!!!");
		Ref.extractThumbsAndMiniToTmpFile(documentList);
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
		server = null;
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
