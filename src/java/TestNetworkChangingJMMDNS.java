import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceInfo;

import net.niconomicon.tile.source.app.Ref;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class TestNetworkChangingJMMDNS implements NetworkTopologyListener {

	private int servicePort;
	public JmmDNS jmmdns;

	boolean shouldExit = false;
	boolean shouldUnregister = false;

	public ServiceInfo info;

	public TestNetworkChangingJMMDNS() throws Exception {
		servicePort = 8888;
		jmmdns = JmmDNS.Factory.getInstance();

		Map<String, String> m = new HashMap<String, String>();
		m.put("path", "json");
		info = ServiceInfo.create("_http._tcp.local.", "TiledImageSharingService", servicePort, 0, 0, m);

	}

	public void inetAddressAdded(NetworkTopologyEvent arg0) {
		try {
			jmmdns.registerService(info);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("inetAddressAdded : " + arg0.getInetAddress());
	}

	public void inetAddressRemoved(NetworkTopologyEvent arg0) {
		System.out.println("inetAddressRemoved : " + arg0.getInetAddress());
		try {
			jmmdns.registerService(info);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This methods takes 7 seconds to work.
	 */
	public void reactivateSharing() {
		try {
			System.out.println("Unregistering everything !");
			jmmdns.unregisterAllServices();
			jmmdns.close();
			jmmdns = JmmDNS.Factory.getInstance();
			// useless :
			jmmdns.addNetworkTopologyListener(this);
			// System.out.println("Registering something !");
			jmmdns.registerService(info);
			System.out.println("Registered Service as " + info);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This methods takes 9 seconds to work.
	 */
	public void reallyActivateSharing() {
		try {
			// System.out.println("Unregistering " + info);
			// jmmdns.unregisterService(info);
			// Thread.sleep(3000);
			// jmmdns.close();
			// Thread.sleep(3000);
			jmmdns = JmmDNS.Factory.getInstance();
			Thread.sleep(3000);
			jmmdns.registerService(info);

			System.out.println("Registered " + info);
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

		TestNetworkChangingJMMDNS bla = new TestNetworkChangingJMMDNS();
		bla.jmmdns.unregisterAllServices();
		// bla.reallyActivateSharing();
		while (true) {

			try {
				localHost = InetAddress.getLocalHost().getCanonicalHostName();
				System.out.println("Localhost : " + localHost);

				if (!hostname.equals(localHost)) {
					hostname = localHost;
					bla.reallyActivateSharing();
					// bla.jmmdns.unregisterService(bla.info);
					// bla.jmmdns.close();
					// bla.jmmdns = JmmDNS.Factory.getInstance();
					// Thread.sleep(5000);
					// bla.jmmdns.registerService(bla.info);
					// // System.out.println("localhost is different");
					// // bla.reactivateSharing();
				}
				// System.out.println("Gonna sleep");
				Thread.sleep(10000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}	
}
