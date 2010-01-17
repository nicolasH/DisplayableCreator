/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author niko
 * 
 */
public class MapSharingService {

	private int servicePort;
	JmDNS jmdns;

	static MapSharingService service;
	// private Thread announcerThread;
	// private SharingServiceAnnouncer announcer;
	boolean shouldExit = false;
	boolean shouldUnregister = false;

	public static MapSharingService getInstance() {
		synchronized (Ref.sharing_serviceName) {
			if (service == null) {
				service = new MapSharingService();
			}
		}
		return service;
	}

	private MapSharingService() {
	// announcer = new SharingServiceAnnouncer();
	// announcerThread = new Thread(announcer);
	}

	public void startSharing(int port) {
		shouldUnregister = false;
		servicePort = port;
		// System.out.println("Running the Thread");
		try {
			if (jmdns != null) {
				jmdns.unregisterAllServices();
			}
			System.out.println("Opening JmDNS");
			jmdns = JmDNS.create();
			System.out.println("Opened JmDNS. Registering the service...");

			try {
				ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort, 0, 0, "path=" + Ref.sharing_xmlRef);
				jmdns.registerService(info);
				System.out.println("\nRegistered Service as " + info);
				if (shouldUnregister) {
					jmdns.unregisterAllServices();
					jmdns.close();
				}
				if (shouldExit) {
					jmdns.unregisterAllServices();
					jmdns.close();
					System.exit(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// announcer.stopService();
		// announcerThread.interrupt();
		// announcerThread = new Thread(announcer);
		// announcerThread.start();
	}

	public void stopSharing() {
		stopService();
	}

	/**
	 * Class to announce the sharing service
	 * 
	 * @author niko
	 * 
	 */
	// public class SharingServiceAnnouncer implements Runnable {
	// public void run() {
	// }
	public void stopService() {
		shouldUnregister = true;
		if (null != jmdns) {
			try {
				System.out.println("unregistering services");
				jmdns.unregisterAllServices();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				System.out.println("closing the jmdns service");
				jmdns.close();
				System.out.println("closed the jmdns service");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	// }

	// public static void main(String[] args) {
	// String file = "/Users/niko/devilstower_pacholka_big.jpg.mdb";
	// ArrayList l = new ArrayList();
	// l.add(file);
	// l.add(file);
	// l.add(file);
	// l.add(file);
	// l.add(file);
	// l.add(file);
	// Map<String,String> urlToFile = new HashMap<String, String>();
	// String s = Ref.generateXML(l,urlToFile);
	// System.out.println(s);
	// }

}
