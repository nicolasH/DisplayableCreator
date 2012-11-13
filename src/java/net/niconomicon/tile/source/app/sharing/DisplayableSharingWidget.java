/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import net.niconomicon.tile.source.app.AppPreferences;
import net.niconomicon.tile.source.app.DisplayablesSource;
import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;

/**
 * @author Nicolas Hoibian This class is designed to assemble and present the
 *         widgets related to the Displayable sharing functionality.
 */
public class DisplayableSharingWidget {

	private static final String action_start = "Start the network broadcasting";
	private static final String action_starting = "Starting the network broadcasting ...";
	private static final String action_stopping = "Stopping the network broadcasting ...";
	private static final String action_stop = "Stop network broadcasting";

	private static final long inet_check_interval = 10000;

	public static enum DS {
		ACTIVATING, ACTIVE, DEACTIVATING, DEACTIVATED
	}

	public static enum DA {
		ACTIVATE, DEACTIVATE, UPDATELIST
	}

	DS currentStatus = DS.DEACTIVATED;
	SharingManager sharingManager;

	JButton actionButton;
	JButton exportButton;

	Color defaultColor;

	DisplayablesSource displayablesSource;
	Timer timer;

	Queue<DA> switchQueues;
	IconsLoader ic;

	public DisplayableSharingWidget(DisplayablesSource dispList) {
		this.displayablesSource = dispList;
		init();
	}

	public JButton getExportButton() {
		return exportButton;
	}

	public JButton getSharingButton() {
		return actionButton;
	}

	public void init() {

		this.ic = IconsLoader.getIconsLoader();
		switchQueues = new ConcurrentLinkedQueue<DA>();
		sharingManager = new SharingManager();
		timer = new Timer();

		// //////////////////////////////////////////
		// start sharing
		actionButton = new JButton(ic.ic_sharingOff_24);
		actionButton.setToolTipText(action_start);
		exportButton = new JButton("Export...");

		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionButton.setEnabled(false);
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

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sharingManager.exportDisplayables(DisplayableSharingWidget.this.exportButton);
			}
		});

		timer.scheduleAtFixedRate(new LocalHostChecker(), inet_check_interval, inet_check_interval);
		Thread t0 = new Thread(new DisplayableMonitor());
		t0.start();
		Thread t1 = new Thread(new StatusSwitcher());
		t1.start();

	}

	// public static JComponent createDirSelectionPanel(JPanel parent) {
	// JTextArea explanation = new JTextArea();
	// explanation.setBorder(null);
	// explanation.setEditable(false);
	// explanation.setBackground(parent.getBackground());
	// explanation.setWrapStyleWord(true);
	// explanation.setLineWrap(true);
	// explanation.setColumns(30);
	// explanation.setFont(explanation.getFont().deriveFont(Font.ITALIC));
	// explanation.setText("Share your displayables over the network to download them on your iPhone or iPod touch.");
	// return explanation;
	// }

	public void switchSharing(DA action) {
		switch (action) {
		case ACTIVATE:
			startSharing(true);
			break;
		case UPDATELIST:
			updateSharing();
			break;
		case DEACTIVATE:
			stopSharing();
			break;
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

		actionButton.setEnabled(false);
		synchronized (currentStatus) {
			currentStatus = DS.ACTIVATING;
		}
		int port = AppPreferences.getPreferences().getPort();
		actionButton.setIcon(ic.ic_loading_16);
		actionButton.setToolTipText(action_starting);

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
				JOptionPane.showConfirmDialog(this.actionButton, "<html><body>Error while starting the sharing component on port [" + port
						+ "]: <br/><i>" + ex.getMessage() + "</i><br/>You might want to change the port.</body></html>",
						"Error creating starting the sharing component", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			}
			ex.printStackTrace();
			synchronized (currentStatus) {
				currentStatus = DS.DEACTIVATED;
			}
			actionButton.setEnabled(true);
			actionButton.setIcon(ic.ic_sharingOff_24);
			return false;
		}
		actionButton.setIcon(ic.ic_sharingOn_24);
		actionButton.setToolTipText(action_stop);
		synchronized (currentStatus) {
			currentStatus = DS.ACTIVE;
		}
		actionButton.setEnabled(true);

		return true;
	}

	private void stopSharing() {
		System.out.println("deactivating");
		actionButton.setEnabled(false);
		synchronized (currentStatus) {
			currentStatus = DS.DEACTIVATING;
		}
		actionButton.setIcon(ic.ic_loading_16);
		actionButton.setToolTipText(action_stopping);
		try {
			sharingManager.stopSharing();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		actionButton.setIcon(ic.ic_sharingOff_24);
		actionButton.setToolTipText(action_start);
		synchronized (currentStatus) {
			currentStatus = DS.DEACTIVATED;
		}
		actionButton.setEnabled(true);
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
					System.out.println("Polling - tmp:" + tmp);
					if (tmp != null) {
						next = tmp;
					}
				}
				System.out.println("Polling - next:" + next);
				if (next == null) {
					try {
						synchronized (switchQueues) {
							System.out.println("Waiting on the queue");
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
						System.out.println("Switching: ACTIVATE while already active or activating: doing nothing");
					}
					break;
				case DEACTIVATE:
					if (currentStatus == DS.ACTIVE || currentStatus == DS.ACTIVATING) {
						stopSharing();
					} else {// else do nothing
						System.out.println("Switching: DEACTIVATE while already deactivating or deactivated: doing nothing");
					}
					break;
				case UPDATELIST:
					if (currentStatus == DS.ACTIVE) {
						updateSharing();
					}
					if (currentStatus == DS.DEACTIVATED || currentStatus == DS.DEACTIVATED) {
						startSharing(true);
					}
					break;
				}
			}
		}
	}
}
