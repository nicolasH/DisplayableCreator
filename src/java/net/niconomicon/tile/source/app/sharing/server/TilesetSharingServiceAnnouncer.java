/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceInfo;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author niko
 * 
 */
public class TilesetSharingServiceAnnouncer implements NetworkTopologyListener {

	private int servicePort;
	JmDNS jmmdns;

	static TilesetSharingServiceAnnouncer service;
	boolean shouldExit = false;
	boolean shouldUnregister = false;
	Thread lhc;

	public static TilesetSharingServiceAnnouncer getInstance() {
		synchronized (Ref.sharing_serviceName) {
			if (service == null) {
				service = new TilesetSharingServiceAnnouncer();
			}
		}
		return service;
	}

	public class LocalHostChecker implements Runnable {
		String hostname = "bla";
		String localHost = "notBla";

		public void run() {
			while (!shouldExit) {
				if (!shouldUnregister) {
					try {
						localHost = InetAddress.getLocalHost().getCanonicalHostName();
						// System.out.println("Localhost : " + localHost);
						if (!hostname.equals(localHost)) {
							hostname = localHost;
							service.reactivateSharing();
						}
						// System.out.println("Gonna sleep");
						Thread.sleep(10000);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private TilesetSharingServiceAnnouncer() {
		try {
			jmmdns = JmDNS.create();
			lhc = new Thread(new LocalHostChecker());
			lhc.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startSharing(int port) {
		shouldUnregister = false;
		servicePort = port;
		reactivateSharing();
	}

	public void inetAddressAdded(NetworkTopologyEvent arg0) {
		reactivateSharing();
		System.out.println("inetAddressAdded");
	}

	public void inetAddressRemoved(NetworkTopologyEvent arg0) {
		System.out.println("inetAddressRemoved");
		reactivateSharing();
	}

	public void reactivateSharing() {
		try {
			jmmdns.unregisterAllServices();
			jmmdns.close();
			System.out.println("Opening JmDNS");
			jmmdns = JmDNS.create();

			System.out.println("Opened JmDNS. Registering the service...");
			Map<String, String> m = new HashMap<String, String>();
			m.put("path", "json");

			ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort, 1, 1, m);
			jmmdns.registerService(info);
			// jmmdns.addNetworkTopologyListener(this);
			System.out.println("\nRegistered Service as " + info);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stopSharing() {
		stopService();
	}

	public void stopService() {
		shouldUnregister = true;
		if (null != jmmdns) {
			try {
				System.out.println("unregistering services");
				jmmdns.unregisterAllServices();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				System.out.println("closing the jmdns service");
				jmmdns.close();
				System.out.println("closed the jmdns service");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
