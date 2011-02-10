/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	JmmDNS jmmdns;

	static TilesetSharingServiceAnnouncer service;
	boolean shouldExit = false;
	boolean shouldUnregister = false;

	public static TilesetSharingServiceAnnouncer getInstance() {
		synchronized (Ref.sharing_serviceName) {
			if (service == null) {
				service = new TilesetSharingServiceAnnouncer();
			}
		}
		return service;
	}

	private TilesetSharingServiceAnnouncer() {
		jmmdns = JmmDNS.Factory.getInstance();
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
			jmmdns = JmmDNS.Factory.getInstance();

			System.out.println("Opened JmDNS. Registering the service...");
			Map<String, String> m = new HashMap<String, String>();
			m.put("path", "json");

			ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort, 1, 1, m);
			jmmdns.registerService(info);
			jmmdns.addNetworkTopologyListener(this);
			System.out.println("\nRegistered Service as " + info);
			// if (shouldUnregister) {
			// jmmdns.unregisterAllServices();
			// jmmdns.close();
			// }
			// if (shouldExit) {
			// jmmdns.unregisterAllServices();
			// jmmdns.close();
			// System.exit(0);
			// }
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
