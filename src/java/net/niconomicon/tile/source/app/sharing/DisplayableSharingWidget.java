/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.niconomicon.tile.source.app.AppPreferences;
import net.niconomicon.tile.source.app.DisplayablesSource;
import net.niconomicon.tile.source.app.fonts.FontLoader;
import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;

/**
 * @author Nicolas Hoibian This class is designed to assemble and present the
 *         widgets related to the Displayable sharing functionality.
 */
public class DisplayableSharingWidget {

	private static final String action_start = "Start sharing the current displayables over the local network"; // broadcasting
	private static final String action_starting = "Starting the local network sharing ...";
	private static final String action_stopping = "Stopping the local network sharing ...";
	private static final String action_stop = "Stop sharing the dislpayables over the network.";

	private static final String action_export = "Export the displayable as a folder which contains an index.html with links to a local copy of all the displayables in the list (advanced).";

	private static final long inet_check_interval = 10000;

	public static enum DS {
		ACTIVATING, ACTIVE, DEACTIVATING, DEACTIVATED
	}

	public static enum DA {
		ACTIVATE, DEACTIVATE, UPDATELIST, RESTART
	}

	Color COLOR_INACTIVE = Color.ORANGE;
	Color defaultColor;

	DS currentStatus = DS.DEACTIVATED;
	SharingManager sharingManager;

	JCheckBox actionCheckBox;
	JTextArea addressLabel;

	DisplayablesSource displayablesSource;
	Timer timer;

	Queue<DA> switchQueues;
	IconsLoader ic;

	public DisplayableSharingWidget(DisplayablesSource dispList) {
		this.displayablesSource = dispList;
		init();
	}

	public void init() {
		addressLabel = FontLoader.getBoringTextArea(new JLabel(), 60, "");
		addressLabel.setFont(addressLabel.getFont().deriveFont(Font.ITALIC));
		addressLabel.setForeground(Color.DARK_GRAY);
		this.ic = IconsLoader.getIconsLoader();
		switchQueues = new ConcurrentLinkedQueue<DA>();
		sharingManager = new SharingManager();
		timer = new Timer();

		// //////////////////////////////////////////
		// start sharing
		actionCheckBox = new JCheckBox(FontLoader.iconAction);
		JButton b = FontLoader.getButton("");
		actionCheckBox.setFont(b.getFont());
		actionCheckBox.setToolTipText(action_start);
		actionCheckBox.setForeground(COLOR_INACTIVE);

		actionCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionCheckBox.setEnabled(false);
				synchronized (currentStatus) {

					if (currentStatus == DS.ACTIVATING || currentStatus == DS.ACTIVE) {
						System.out.println("Action: deactivate");
						switchQueues.add(DA.DEACTIVATE);
					} else {
						switchQueues.add(DA.ACTIVATE);
					}
				}
				synchronized (switchQueues) {
					switchQueues.notifyAll();
				}
			}
		});

		timer.scheduleAtFixedRate(new LocalHostChecker(), inet_check_interval, inet_check_interval);

		Thread t1 = new Thread(new StatusSwitcher());
		t1.start();
		Thread t2 = new Thread(new DisplayableMonitor());
		t2.start();
		Thread t3 = new Thread(new PortMonitor());
		t3.start();
	}

	public JCheckBox getSharingButton() {
		return actionCheckBox;
	}

	public JTextArea getAddressComponent() {
		return addressLabel;
	}

	public void switchSharing(DA action) {
		switchQueues.add(action);
		synchronized (switchQueues) {
			switchQueues.notifyAll();
		}
	}

	private void updateSharing() {
		synchronized (currentStatus) {
			currentStatus = DS.ACTIVATING;
		}
		System.out.println("setting the displayable list to:" + displayablesSource.getDisplayables());
		sharingManager.setSharingList(displayablesSource.getDisplayables());
		sharingManager.restartAnnouncerSync();
		synchronized (currentStatus) {
			currentStatus = DS.ACTIVE;
		}
	}

	/**
	 * @param shouldPopup
	 *            should an alert message be displayed in case of error.
	 * @return if the operation was successful.
	 */
	private boolean startSharing(boolean shouldPopup) {
		System.out.println("should start sharing the maps, with " + (shouldPopup ? "popup" : "no popup") + " in case of problem");

		actionCheckBox.setEnabled(false);
		synchronized (currentStatus) {
			currentStatus = DS.ACTIVATING;
		}
		int port = AppPreferences.getPreferences().getPort();
		actionCheckBox.setText(FontLoader.iconWait);
		actionCheckBox.setToolTipText(action_starting);

		try {
			sharingManager.setPort(port);
			sharingManager.setSharingList(displayablesSource.getDisplayables());
			sharingManager.startSharing();
			// sharingManager.startSharing();
		} catch (Exception ex) {
			try {
				sharingManager.stopSharing();
			} catch (Exception ex1) {
				System.out.println("ex1 : ");
				ex1.printStackTrace();
			}
			if (shouldPopup) {
				JOptionPane.showConfirmDialog(this.actionCheckBox, "<html><body>Error while starting the sharing component on port [" + port
						+ "]: <br/><i>" + ex.getMessage() + "</i><br/>You might want to change the port.</body></html>",
						"Error creating starting the sharing component", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			}
			ex.printStackTrace();
			synchronized (currentStatus) {
				currentStatus = DS.DEACTIVATED;
			}
			actionCheckBox.setEnabled(true);
			actionCheckBox.setText(FontLoader.iconAction);
			actionCheckBox.setForeground(COLOR_INACTIVE);
			return false;
		}
		actionCheckBox.setText(FontLoader.iconAction);
		actionCheckBox.setForeground(Color.GREEN);
		actionCheckBox.setToolTipText(action_stop);

		synchronized (currentStatus) {
			currentStatus = DS.ACTIVE;
		}
		actionCheckBox.setEnabled(true);

		return true;
	}

	private void stopSharing() {
		System.out.println("deactivating");
		actionCheckBox.setEnabled(false);
		synchronized (currentStatus) {
			currentStatus = DS.DEACTIVATING;
		}
		actionCheckBox.setText(FontLoader.iconWait);
		actionCheckBox.setToolTipText(action_stopping);
		try {
			sharingManager.stopSharing();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		actionCheckBox.setText(FontLoader.iconAction);
		actionCheckBox.setForeground(Color.GRAY);
		actionCheckBox.setToolTipText(action_start);
		synchronized (currentStatus) {
			currentStatus = DS.DEACTIVATED;
		}
		actionCheckBox.setEnabled(true);
	}

	private void updateAddressUIElements() {
		synchronized (currentStatus) {
			switch (currentStatus) {
			case ACTIVE:
				try {
					String localhost = InetAddress.getLocalHost().getHostAddress();
					addressLabel.setText("Serving disp. on http://" + localhost + ":" + AppPreferences.getPreferences().getPort() + "/");
				} catch (Exception e) {
					addressLabel.setText("Serving disp. on http://[this machine address]:" + AppPreferences.getPreferences().getPort() + "/");
				}
				actionCheckBox.setSelected(true);
				break;

			case DEACTIVATED:
				addressLabel.setText("");
				actionCheckBox.setSelected(false);
				break;
			}
		}
	}

	public class LocalHostChecker extends TimerTask {
		String hostname = "bla";
		String localHost = "notBla";

		public void run() {
			try {
				localHost = InetAddress.getLocalHost().getCanonicalHostName();
				if (!hostname.equals(localHost)) {
					hostname = localHost;
					switchQueues.add(DA.UPDATELIST);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public class PortMonitor implements Runnable {

		public void run() {
			while (true) {
				synchronized (AppPreferences.portNumberChangeLock) {
					try {
						System.out.println("Waiting for a port number change");
						AppPreferences.portNumberChangeLock.wait();
					} catch (Exception ex) {
						System.out.println("Exception");
						ex.printStackTrace();
					} finally {
						// something happened to the displayableList
						System.out.println("Port number changed, requesting a sharing restart");
						switchQueues.add(DA.RESTART);
						synchronized (switchQueues) {
							switchQueues.notifyAll();
						}
					}
				}
			}
		}
	}

	private class DisplayableMonitor implements Runnable {
		public void run() {
			while (true) {
				synchronized (displayablesSource.getDisplayablesLock()) {
					System.out.println("Gonna sleep on the displayable lock");
					try {
						displayablesSource.getDisplayablesLock().wait();
						System.out.println("Stopped waiting on the displayable lock");
					} catch (Exception ex) {
						System.out.println("Exception");
						ex.printStackTrace();
					} finally {
						// something happened to the displayableList
						System.out.println("Finally - Requesting a displayable list update");
						switchQueues.add(DA.UPDATELIST);
						synchronized (switchQueues) {
							switchQueues.notifyAll();
						}
					}
				}
			}
		}
	}

	private class StatusSwitcher implements Runnable {

		public void run() {
			while (true) {
				DA next = null;
				DA tmp = DA.UPDATELIST;
				while (tmp != null) {
					synchronized (switchQueues) {
						tmp = switchQueues.poll();
					}
					if (tmp != null) {
						next = tmp;
					}
				}
				if (next == null) {
					try {
						synchronized (switchQueues) {
							switchQueues.wait();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue;
				}
				switch (next) {
				case ACTIVATE:
					if (currentStatus == DS.DEACTIVATED || currentStatus == DS.DEACTIVATING) {
						startSharing(true);
					} else {// else do nothing
						//System.out.println("Switching: ACTIVATE while already active or activating: doing nothing");
					}
					break;
				case DEACTIVATE:
					if (currentStatus == DS.ACTIVE || currentStatus == DS.ACTIVATING) {
						stopSharing();
					} else {// else do nothing
						//System.out.println("Switching: DEACTIVATE while already deactivating or deactivated: doing nothing");
					}
					break;
				case UPDATELIST:
					if (currentStatus == DS.ACTIVE) {
						updateSharing();
					}
					if (currentStatus == DS.DEACTIVATED || currentStatus == DS.DEACTIVATING) {
						// do nothing //startSharing(true);
					}
					break;
				case RESTART:
					if (currentStatus == DS.ACTIVE || currentStatus == DS.ACTIVATING) {
						// not so sure about the second one
						startSharing(true);
					}
					// if not started, no need to start
				}
				updateAddressUIElements();
			}
		}
	}
}
