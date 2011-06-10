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
 * This class handles the publishing of the DisplayableSharing service over the network. It uses JmDNS.
 * It starts a thread the automatically restarts the sharing when the loalhost address changes.
 */
public class DisplayableSharingServiceAnnouncer {

	private int servicePort;
	JmDNS jmdns;
	static DisplayableSharingServiceAnnouncer service;
	ServiceInfo lastInfos;
	boolean shouldExit = false;
	Boolean shouldUnregister = true;
	Timer lhc;

	public static DisplayableSharingServiceAnnouncer getInstance() {
		synchronized (Ref.sharing_serviceName) {
			if (service == null) {
				service = new DisplayableSharingServiceAnnouncer();
			}
		}
		return service;
	}

	public class LocalHostChecker extends TimerTask {

		String hostname;
		String localHost;

		public LocalHostChecker() {
			try {
				hostname = InetAddress.getLocalHost().getCanonicalHostName();
				localHost = InetAddress.getLocalHost().getCanonicalHostName();
			} catch (Exception ex) {
				hostname = "bla";
				localHost = "notBla";
			}
		}

		public void run() {
			if (shouldExit) { return; }
			synchronized (shouldUnregister) {
				if (!shouldUnregister) {
					try {
						localHost = InetAddress.getLocalHost().getCanonicalHostName();
						// System.out.println("Localhost : " + localHost);
						if (!hostname.equals(localHost)) {
							hostname = localHost;
							reactivateSharing();
							// try {
							// if (jmdns != null) {// assuming the
							// jmdns.unregisterAllServices();
							// jmdns.close();
							// }
							// System.out.println("Th Opening JmDNS");
							// jmdns = JmDNS.create();
							// System.out.println("Th Opened JmDNS. Registering the service...");
							// Map<String, String> m = getJmDNSPayload(servicePort);
							//
							// ServiceInfo info = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName,
							// servicePort, 1, 1, m);
							// jmdns.registerService(info);
							// System.out.println("Th Registered Service as " + info);
							// } catch (Exception e) {
							// e.printStackTrace();
							// }
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private DisplayableSharingServiceAnnouncer() {
		try {
			jmdns = JmDNS.create();
			lhc = new Timer();
			lhc.scheduleAtFixedRate(new LocalHostChecker(), 10000, 10000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setPort(int port) {
		servicePort = port;
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
				if (lastInfos != null && jmdns != null) {
					System.out.println("unregistering " + lastInfos);
					jmdns.unregisterService(lastInfos);
					jmdns.unregisterAllServices();
					lastInfos = null;
				}
				System.out.println("Opening JmDNS");
				jmdns = JmDNS.create();
				System.out.println("Opened JmDNS. Registering the service...");
				Map<String, String> m = new HashMap<String, String>();
				m = getJmDNSPayload(servicePort);
				lastInfos = ServiceInfo.create("_http._tcp.local.", Ref.sharing_serviceName, servicePort, 1, 1, m);
				jmdns.registerService(lastInfos);
				System.out.println("Registered Service as " + lastInfos);
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
					if (lastInfos != null) {
						jmdns.unregisterService(lastInfos);
					}
					jmdns.unregisterAllServices();
					lastInfos = null;
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

	public static Map<String, String> getJmDNSPayload(int port) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("data_path", Ref.sharing_jsonRef);
		map.put("service_name", getServiceName(port));
		return map;
	}

	public static String getServiceName(int port) {
		String localH = "";

		String s = null;
		try {
			s = InetAddress.getLocalHost().getHostName();
		} catch (Exception ex) {}
		if (s == null) {
			try {
				s = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception ex) {}
		}
		if (s == null) {
			localH = "?";
		} else {
			localH = s;
		}
		if (localH.length() > 20) {
			localH = localH.substring(localH.length() - 20);
		}
		return localH + ":" + port;
	}
}
