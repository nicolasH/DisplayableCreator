package net.niconomicon.tile.source.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class AppPreferences extends JPanel {

	public int tileSize = 256;
	public int saveDir;
	public boolean autoShare = true;

	JSpinner portNumber;

	private static AppPreferences prefs;

	public static AppPreferences getPreferences() {
		if (prefs == null) {
			prefs = new AppPreferences();
		}
		return prefs;
	}

	private AppPreferences() {
		super();
		JPanel pixels = new JPanel();
		pixels.setBorder(BorderFactory.createTitledBorder("Tile Size"));
		pixels.setLayout(new BoxLayout(pixels, BoxLayout.PAGE_AXIS));
		ButtonGroup choices = new ButtonGroup();

		JRadioButton _192 = new JRadioButton(new PixelSizeAction(192, "non-retina iPhone & iPod touch"));
		JRadioButton _256 = new JRadioButton(new PixelSizeAction(256, "retina iPhone, iPod touch and non-retina iPad"));
		JRadioButton _384 = new JRadioButton(new PixelSizeAction(384, "retina iPad, retina iPhone & iPod Touch"));
		JRadioButton _512 = new JRadioButton(new PixelSizeAction(512, "retina iPad"));

		choices.add(_192);
		choices.add(_256);
		choices.add(_384);
		choices.add(_512);

		pixels.add(_192);
		pixels.add(_256);
		pixels.add(_384);
		pixels.add(_512);

		this.add(pixels);

		JPanel sharing = new JPanel();
		// sharing.setLayout(new BoxLayout(sharing, BoxLayout.Y_AXIS));
		sharing.setBorder(BorderFactory.createTitledBorder("Sharing details"));
		portNumber = new JSpinner(new SpinnerNumberModel(Ref.sharing_port, 1025, 65536, 1));
		JLabel legend = new JLabel("Sharing port:");
		JLabel help = new JLabel("( ? )");
		sharing.add(legend);
		sharing.add(portNumber);
		sharing.add(help);
		this.add(sharing);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Preferences");
		frame.setContentPane(new AppPreferences());
		frame.pack();
		frame.setVisible(true);
	}

	public int getPort() {
		return ((SpinnerNumberModel) portNumber.getModel()).getNumber().intValue();

	}

	private class PixelSizeAction implements Action {
		int size = 0;
		boolean enabled = true;

		HashMap<String, Object> values;

		public PixelSizeAction(int pixels, String bestFor) {
			this.size = pixels;
			values = new HashMap<String, Object>();
			values.put("ShortDescription", "Set the tile size of the next created displayables. Best if the displayable is going to be shown on "
					+ bestFor);
			values.put("Name", pixels + " px");
		}

		public void actionPerformed(ActionEvent e) {
			AppPreferences.this.tileSize = size;
			System.out.println("set the tilesize to" + AppPreferences.this.tileSize);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
		}

		public Object getValue(String key) {
			return values.get(key);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void putValue(String key, Object value) {
			values.put(key, value);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
		}

		public void setEnabled(boolean b) {
			this.enabled = b;
		}
	}

	public class AppPreferencesAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComponent parentComponent = (JComponent) e.getSource();
			int selected = JOptionPane.showOptionDialog(parentComponent, getPreferences(), "Preferences", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, null, null);
			System.out.println("selected:" + selected);
			if (selected == JOptionPane.OK_OPTION) {
				System.out.println("Should save and apply the preferences");
			} else {
				System.out.println("Should reset the preferences");

			}
		}
	}
}
