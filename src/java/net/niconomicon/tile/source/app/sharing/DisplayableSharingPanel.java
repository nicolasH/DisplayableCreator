/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.niconomicon.tile.source.app.DisplayablesSource;
import net.niconomicon.tile.source.app.sharing.SharingWidget.STATUS;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;

/**
 * @author Nicolas Hoibian This class is designed to assemble and present the
 *         widgets related to the Displayable sharing functionality.
 */
public class DisplayableSharingPanel extends JPanel {

	boolean currentlySharing = false;
	SharingManager sharingManager;

	SharingWidget widget;

	Color defaultColor;

	DisplayableViewer viewer;
DisplayablesSource displayablesSource;
	Timer timer;

	public DisplayableSharingPanel(DisplayableViewer viewer, DisplayablesSource dispList) {
		this.viewer = viewer;
		this.displayablesSource = dispList;
		init();
	}

	public void init() {

		sharingManager = new SharingManager();
		timer = new Timer();

		this.setLayout(new BorderLayout());
		this.add(createDirSelectionPanel(this), BorderLayout.NORTH);
		// //////////////////////////////////////////
		// port number
		// start sharing
		JButton shareButton = new JButton("Start sharing");
		JButton exportButton = new JButton("Export...");
		widget = new SharingWidget(shareButton, exportButton);
		this.add(widget, BorderLayout.SOUTH);

		shareButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Runnable runn = new Runnable() {
					public void run() {
						switchSharing(true);
					}
				};
				Thread t = new Thread(runn);
				t.start();
			}
		});

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sharingManager.exportDisplayables(DisplayableSharingPanel.this);
			}
		});

		long delta = 10000;
		timer.scheduleAtFixedRate(new LocalHostChecker(), delta, delta);
		Thread t = new Thread(new DisplayableMonitor());
		t.start();

	}

	public static JComponent createDirSelectionPanel(JPanel parent) {
		JTextArea explanation = new JTextArea();
		explanation.setBorder(null);
		explanation.setEditable(false);
		explanation.setBackground(parent.getBackground());
		explanation.setWrapStyleWord(true);
		explanation.setLineWrap(true);
		explanation.setColumns(30);
		explanation.setFont(explanation.getFont().deriveFont(Font.ITALIC));
		explanation.setText("Share your displayables over the network to download them on your iPhone or iPod touch.");
		return explanation;
	}

	public void switchSharing(boolean shouldPopup) {
		// synchronized (widget) {
		if (!currentlySharing) {
			try {
				widget.setStatus(STATUS.ACTIVATING);
			} catch (InvocationTargetException ex) {
			} catch (InterruptedException ex) {
			}
			if (startSharing(shouldPopup)) {
				try {
					widget.setStatus(STATUS.ACTIVE);
				} catch (InvocationTargetException ex) {
				} catch (InterruptedException ex) {
				}
				currentlySharing = true;
				return;
			}
		}
		// stopping sharing or start sharing failed.
		try {
			widget.setStatus(STATUS.DEACTIVATING);
		} catch (InvocationTargetException ex) {
		} catch (InterruptedException ex) {
		}
		stopSharing();
		try {
			widget.setStatus(STATUS.DEACTIVATED);
		} catch (InvocationTargetException ex) {
		} catch (InterruptedException ex) {
		}
		currentlySharing = false;
		// }
	}

	public boolean startSharing(boolean shouldPopup) {
		// HashSet<String> sharedDB = new HashSet<String>();
		System.out.println("should start sharing the maps, with " + (shouldPopup ? "popup" : "no popup") + " in case of problem");
		// generate the xml;
		try {
			sharingManager.setPort(widget.getPort());
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
				JOptionPane.showConfirmDialog(this, "<html><body>Error while starting the sharing component on port [" + widget.getPort()
						+ "]: <br/><i>" + ex.getMessage() + "</i><br/>You might want to change the port.</body></html>",
						"Error creating starting the sharing component", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			}
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public void stopSharing() {
		try {
			sharingManager.stopSharing();
		} catch (Exception ex) {
			ex.printStackTrace();
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
					if (sharingManager.isSharing()) {
						widget.setStatus(STATUS.ACTIVE);
					}
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
						System.out.println("Exeption");
						ex.printStackTrace();
					} finally {
						// something happened to the displayableList
						System.out.println("Finally");
						sharingManager.setSharingList(displayablesSource.getDisplayables());
						sharingManager.restartAnnouncer();
					}
				}
			}
		}
	}
}
