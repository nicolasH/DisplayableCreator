/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.niconomicon.tile.source.app.Ref;

/**
 * This class manages the user interaction elements for the parameters of the "Displayable sharing" functionnality.
 * 
 * @author Nicolas Hoibian
 * 
 */
public class SharingWidget extends JPanel {

	public static enum STATUS {
		ACTIVATING, ACTIVE, DEACTIVATING, DEACTIVATED
	}

	JLabel sharingStatus;
	JTextField sharingLocation;
	JButton actionButton;

	JButton exportButton;

	Color defaultColor;
	JSpinner portNumber;

	public SharingWidget(JButton actionButton, JButton exportButton) {
		super(new GridBagLayout());
		GridBagConstraints c;

		portNumber = new JSpinner(new SpinnerNumberModel(Ref.sharing_port, 1025, 65536, 1));

		this.actionButton = actionButton;
		this.exportButton = exportButton;
		sharingStatus = new JLabel(" ... ");
		sharingStatus.setHorizontalAlignment(JLabel.CENTER);
		sharingStatus.setVerticalAlignment(JLabel.CENTER);

		sharingLocation = new JTextField(" ... ");
		sharingLocation.setBorder(null);
		sharingLocation.setOpaque(false);
		sharingLocation.setEditable(false);
		sharingLocation.setHorizontalAlignment(JLabel.CENTER);
		defaultColor = sharingStatus.getBackground();

		JLabel l;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = c.LINE_END;
		l = new JLabel("Sharing port : ");
		this.add(l, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.anchor = c.LINE_START;

		this.add(portNumber, c);

		c = new GridBagConstraints();
		c.gridy = 1;
		c.gridx = 0;
		c.anchor = c.LINE_END;
		l = new JLabel("Sharing is : ");
		this.add(l, c);

		c = new GridBagConstraints();
		c.gridy = 1;
		c.gridx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 4.0;
		this.add(sharingStatus, c);

		c = new GridBagConstraints();
		c.gridy = 1;
		c.gridx = 2;
		c.weightx = 1.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(this.actionButton, c);

		c = new GridBagConstraints();
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 3;
		c.weighty = 1.5;

		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(this.sharingLocation, c);

		JLabel empty = new JLabel();
		empty.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black));

		c = new GridBagConstraints();
		c.gridy = 3;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(empty, c);

		c = new GridBagConstraints();
		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		l = new JLabel("Export the shared Displayables list as a website...");
		this.add(l, c);

		c = new GridBagConstraints();
		c.gridy = 4;
		c.gridx = 2;
		c.gridwidth = 1;
		c.weighty = 1.5;

		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(this.exportButton, c);
	}

	public void setStatus(STATUS status) throws InvocationTargetException, InterruptedException {
		// SwingUtilities.invokeLater(
		new StatusInfoUpdater(status).run();
	}

	private class StatusInfoUpdater implements Runnable {
		STATUS status;

		public StatusInfoUpdater(STATUS status) {
			this.status = status;
		}

		public void run() {

			InetAddress localaddr = null;
			try {
				localaddr = InetAddress.getLocalHost();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			switch (status) {
			case ACTIVATING:
				portNumber.setEnabled(false);
				sharingStatus.setText(" Starting ... ");
				setTooltipHostname(localaddr);
				actionButton.setEnabled(false);
				setTooltipHostname(null);
				actionButton.setText("Stop");
				break;
			case ACTIVE:
				sharingStatus.setText(" Active ");
				sharingStatus.setBackground(Color.GREEN);
				setTooltipHostname(localaddr);
				actionButton.setText("Stop");
				actionButton.setEnabled(true);
				break;
			case DEACTIVATING:
				sharingStatus.setText(" Stopping ... ");
				sharingStatus.revalidate();
				actionButton.setEnabled(false);
				setTooltipHostname(null);
				actionButton.setText("Start");
				break;
			case DEACTIVATED:
				setTooltipHostname(null);
				sharingStatus.setText(" Stopped ");
				sharingStatus.setBackground(Color.ORANGE);
				actionButton.setText("Start");
				actionButton.setEnabled(true);
				portNumber.setEnabled(true);
				break;

			default:
				break;
			}
			sharingStatus.revalidate();
		}
	}

	public int getPort() {
		return ((SpinnerNumberModel) portNumber.getModel()).getNumber().intValue();
	}

	public void setTooltipHostname(InetAddress host) {

		if (host == null) {
			sharingStatus.setToolTipText(null);
			sharingLocation.setText(" ");
			sharingStatus.setOpaque(true);
			sharingStatus.setBackground(Color.ORANGE);
			return;
		}
		sharingStatus.setOpaque(true);
		sharingStatus.setBackground(Color.GREEN);
		String add = "http://" + host.getHostAddress() + ":" + getPort() + "/";
		sharingStatus
				.setToolTipText("If the list of Displayables does not appear quickly on your iPhone/iPod touch, try accessing "
						+ add + " in your iPhone / iPod touch web browser");
		sharingLocation.setText("The Displayables are also accessible in Safari at http://" + host.getHostAddress()
				+ ":" + getPort() + "/");
	}
}
