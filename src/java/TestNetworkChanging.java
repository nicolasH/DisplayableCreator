import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceInfo;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.sharing.server.TilesetSharingServiceAnnouncer;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class TestNetworkChanging implements NetworkTopologyListener {

	private int servicePort;
	JmmDNS jmmdns;

	static TilesetSharingServiceAnnouncer service;
	boolean shouldExit = false;
	boolean shouldUnregister = false;

	public TestNetworkChanging() throws Exception {
		servicePort = 8888;
		jmmdns = JmmDNS.Factory.getInstance();
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
			System.out.println("Unregistering everything !");
			jmmdns.unregisterAllServices();
			jmmdns.close();
			jmmdns = JmmDNS.Factory.getInstance();
			System.out.println("Registering something !");
			Map<String, String> m = new HashMap<String, String>();
			m.put("path", "json");
			ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort, 1, 1, m);
			jmmdns.registerService(info);
			System.out.println("Registered Service as " + info);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String hostname = "bla";
		String localHost = "notBla";

		TestNetworkChanging bla = new TestNetworkChanging();
		while (true) {

			try {
				localHost = InetAddress.getLocalHost().getCanonicalHostName();
				if (!hostname.equals(localHost)) {
					hostname = localHost;
					bla.reactivateSharing();
				}
				System.out.println("Gonna sleep");
				Thread.sleep(10000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
