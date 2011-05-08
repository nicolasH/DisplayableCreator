/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author Nicolas Hoibian
 * 
 */
public class SharingWidget extends JPanel {

	public static enum STATUS {
		ACTIVATING, ACTIVE, DEACTIVATING, DEACTIVATED
	}

	JLabel sharingStatus;
	JLabel sharingLocation;
	JButton actionButton;

	Color defaultColor;
	JSpinner portNumber;

	public SharingWidget(JButton actionButton) {
		super(new GridBagLayout());
		GridBagConstraints c;

		portNumber = new JSpinner(new SpinnerNumberModel(Ref.sharing_port, 1025, 65536, 1));

		this.actionButton = actionButton;
		sharingStatus = new JLabel(" ... ");
		sharingLocation = new JLabel(" ... ");
		defaultColor = sharingStatus.getBackground();

		JLabel l;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = c.LINE_END;
		l = new JLabel("Sharing port : ");
		this.add(l, c);

		c = new GridBagConstraints();
		c.gridx = 1;
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
		c.fill = GridBagConstraints.HORIZONTAL;
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
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(this.sharingLocation, c);
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

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
				sharingStatus.setText(" Activating ...");
				setTooltipHostname(localaddr);
				actionButton.setEnabled(false);
				setTooltipHostname(null);
				break;
			case ACTIVE:
				sharingStatus.setText("Active ");
				sharingStatus.setBackground(Color.GREEN);
				setTooltipHostname(localaddr);
				actionButton.setText("Stop");
				actionButton.setEnabled(true);
				break;
			case DEACTIVATING:
				sharingStatus.setText(" Stopping ...");
				sharingStatus.revalidate();
				actionButton.setEnabled(false);
				setTooltipHostname(null);
				break;
			case DEACTIVATED:
				setTooltipHostname(null);
				sharingStatus.setText(" Stopped");
				sharingStatus.setBackground(Color.ORANGE);
				actionButton.setText("Start");
				actionButton.setEnabled(true);
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
		sharingStatus.setToolTipText("If the list of items do not appear quickly on your iPhone/iPod touch, try accessing http://" + host
				.getHostAddress() + ":" + getPort() + "/ in your iPhone / iPod touch web browser");
		sharingLocation.setText("<html><body>Also accessible in Safari at http://" + host.getHostAddress() + ":" + getPort() + "/ </body></html>");

	}
}
