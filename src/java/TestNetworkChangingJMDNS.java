import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceInfo;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.sharing.server.DisplayableSharingServiceAnnouncer;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class TestNetworkChangingJMDNS {

	/**
	 * Problem: JmDNS pegs the CPU at 100 % when network topology changes. Fix : Use the latest version of JmDNS (3.4.0)
	 * 
	 * The code bellow implements an announcer service that survives network changes : - no 100% cpu when changing
	 * network - no blocking - service is announced on the new network
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String hostname = "bla";
		String localHost = "notBla";
		JmDNS jmdns = JmDNS.create();

		while (true) {
			try {
				localHost = InetAddress.getLocalHost().getCanonicalHostName();
				System.out.println("Localhost : " + localHost);
				if (!hostname.equals(localHost)) {
					hostname = localHost;
					synchronized (jmdns) {
						if (jmdns != null) {
							try {
								System.out.println("Unregistering everything !");
								jmdns.unregisterAllServices();
								jmdns.close();
								jmdns = JmDNS.create();
								System.out.println("Registering something !");
								Map<String, String> m = new HashMap<String, String>();
								m.put("path", "json");
								ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, 8888, 0, 0, m);
								jmdns.registerService(info);
								System.out.println("Registered Service as " + info);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
				Thread.sleep(10000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 * 
	 *            public static void main(String[] args) throws Exception { String hostname = "bla"; String localHost =
	 *            "notBla"; JmmDNS jmdns = JmmDNS.Factory.getInstance();
	 * 
	 *            TestNetworkChanging bla = new TestNetworkChanging(); while (true) {
	 * 
	 *            try { localHost = InetAddress.getLocalHost().getCanonicalHostName(); System.out.println("Localhost : "
	 *            + localHost); if (!hostname.equals(localHost)) { hostname = localHost; try {
	 *            System.out.println("Unregistering everything !"); jmdns.unregisterAllServices(); jmdns.close(); jmdns
	 *            = JmmDNS.Factory.getInstance(); System.out.println("Registering something !"); Map<String, String> m =
	 *            new HashMap<String, String>(); m.put("path", "json"); ServiceInfo info =
	 *            ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, 8888, 0, 0, m);
	 *            jmdns.registerService(info); System.out.println("Registered Service as " + info); } catch (Exception
	 *            ex) { ex.printStackTrace(); } } // System.out.println("Gonna sleep"); Thread.sleep(10000); } catch
	 *            (Exception ex) { ex.printStackTrace(); } } }
	 */
}
