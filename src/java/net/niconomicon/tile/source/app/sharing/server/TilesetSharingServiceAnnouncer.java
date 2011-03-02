/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author niko
 * 
 */
public class TilesetSharingServiceAnnouncer {

	private int servicePort;
	JmDNS jmdns;
	static TilesetSharingServiceAnnouncer service;
	boolean shouldExit = false;
	Boolean shouldUnregister = true;
	Timer lhc;

	public static TilesetSharingServiceAnnouncer getInstance() {
		synchronized (Ref.sharing_serviceName) {
			if (service == null) {
				service = new TilesetSharingServiceAnnouncer();
			}
		}
		return service;
	}

	public class LocalHostChecker extends TimerTask {
		String hostname = "bla";
		String localHost = "notBla";

		public void run() {
			if (shouldExit) { return; }
			synchronized (shouldUnregister) {
				if (!shouldUnregister) {
					try {
						localHost = InetAddress.getLocalHost().getCanonicalHostName();
						// System.out.println("Localhost : " + localHost);
						if (!hostname.equals(localHost)) {
							hostname = localHost;
							try {
								if (jmdns != null) {// assuming the
									jmdns.unregisterAllServices();
									jmdns.close();
								}
								System.out.println("Opening JmDNS");
								jmdns = JmDNS.create();
								System.out.println("Opened JmDNS. Registering the service...");
								Map<String, String> m = new HashMap<String, String>();
								// m.put("path", "");
								m.put("data_path", Ref.sharing_jsonRef);
								ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort, 1, 1, m);
								jmdns.registerService(info);
								System.out.println("\nRegistered Service as " + info);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private TilesetSharingServiceAnnouncer() {
		try {
			jmdns = JmDNS.create();
			lhc = new Timer();
			lhc.scheduleAtFixedRate(new LocalHostChecker(), 10000, 10000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startSharing(int port) {
		synchronized (shouldUnregister) {
			shouldUnregister = false;
			servicePort = port;
		}
		reactivateSharing();
	}

	public void reactivateSharing() {
		synchronized (shouldUnregister) {
			try {
				// jmdns.unregisterAllServices();
				// jmdns.close();
				System.out.println("Opening JmDNS");
				jmdns = JmDNS.create();
				System.out.println("Opened JmDNS. Registering the service...");
				Map<String, String> m = new HashMap<String, String>();
				// m.put("path", "");
				m.put("data_path", Ref.sharing_jsonRef);
				ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort, 1, 1, m);
				jmdns.registerService(info);
				System.out.println("\nRegistered Service as " + info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stopSharing() {
		stopService();
	}

	public void stopService() {
		synchronized (shouldUnregister) {
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
					jmdns = null;
					System.out.println("closed the jmdns service");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
