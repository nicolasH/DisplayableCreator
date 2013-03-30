package net.niconomicon.tile.source.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import net.niconomicon.tile.source.app.fonts.FontLoader;

public class AppPreferences extends JPanel {

	private static final String SHOWPREFS_TOOLTIP = "<html><body>Shows the preferences window. <br>You can adjust the sharing port and the next displayables tile sizes.</body></html>";

	private static final MatteBorder lineBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY);
	private static final MatteBorder lineBorderBotton = BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY);
	private static final Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
	private static final Border emptyBorderBottom = BorderFactory.createEmptyBorder(0, 0, 0, 0);

	public int savedTileSize = 256;
	int savedPortNumber;
	boolean savedAutostart;

	JSpinner portNumber;
	public static final String portNumberChangeLock = "portNumberChanged";

	JCheckBox autoshare;
	JCheckBox checkForUpdates;
	JButton check_new;

	JRadioButton _256;
	JRadioButton _384;
	JRadioButton _512;

	private Dimension panelSizes = new Dimension(400, 70);
	private static AppPreferences prefs;

	public static AppPreferences getPreferences() {
		if (prefs == null) {
			prefs = new AppPreferences();
		}
		return prefs;
	}

	private AppPreferences() {
		super();
		this.setLayout(new GridLayout(0, 1));

		JPanel pixels = new JPanel();

		pixels.setLayout(new GridLayout(1, 0));
		ButtonGroup choices = new ButtonGroup();

		_256 = new JRadioButton(new PixelSizeAction(256, "retina iPhone, iPod touch and non-retina iPad"));
		_384 = new JRadioButton(new PixelSizeAction(384, "retina iPad, retina iPhone & iPod Touch"));
		_512 = new JRadioButton(new PixelSizeAction(512, "retina iPad"));

		choices.add(_256);
		choices.add(_384);
		choices.add(_512);

		int inset_radio = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(inset_radio, 0, inset_radio, 0);
		pixels.add(_256, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(inset_radio, 0, inset_radio, 0);

		pixels.add(_384, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.insets = new Insets(inset_radio, 0, inset_radio, 0);
		pixels.add(_512, c);
		pixels.setPreferredSize(panelSizes);
		pixels.setSize(panelSizes);
		pixels.setMaximumSize(panelSizes);

		// ////////////////////////////////////////////////
		JPanel sharing = new JPanel();
		sharing.setLayout(new GridBagLayout());
		// sharing.setBorder(BorderFactory.createTitledBorder("Sharing details"));
		portNumber = new JSpinner(new SpinnerNumberModel(1025, 1025, 65536, 1));
		JLabel legend = new JLabel("Sharing port:");
		autoshare = new JCheckBox("Activate on startup");

		int inset_port = 20;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(inset_port, 0, inset_port, 0);
		sharing.add(legend, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(inset_port, 0, inset_port, 0);
		sharing.add(portNumber, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(inset_port, 0, inset_port, 0);
		sharing.add(autoshare, c);
		// sharing.add(help);
		sharing.setSize(panelSizes);
		sharing.setPreferredSize(panelSizes);
		sharing.setMaximumSize(panelSizes);

		// ////////////////////////////////////////
		JPanel update = new JPanel();
		update.setLayout(new GridBagLayout());
		checkForUpdates = new JCheckBox("Check for update on startup");

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.insets = new Insets(inset_port, 0, inset_port, 0);
		update.add(checkForUpdates, c);

		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(inset_port, 0, inset_port, 0);

		check_new = new JButton("Check now");
		check_new.setToolTipText("You have: " + UpdateChecker.BASE_VERSION);
		check_new.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UpdateChecker.checkForUpdate((JComponent) e.getSource(), true, true);
			}
		});
		update.add(check_new, c);

		// sharing.add(help);
		sharing.setSize(panelSizes);
		sharing.setPreferredSize(panelSizes);
		sharing.setMaximumSize(panelSizes);

		// ////////////////////////////////////////

		Border borderSharing = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(lineBorder, " Sharing Details "), emptyBorder);
		sharing.setBorder(borderSharing);
		Border borderPixel = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(lineBorder, " Tile Size "), emptyBorder);
		pixels.setBorder(borderPixel);
		Border borderUpdate = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(lineBorderBotton, " Updates "), emptyBorderBottom);
		update.setBorder(borderUpdate);

		this.add(sharing);
		this.add(pixels);
		this.add(update);

		readPrefs();
	}

	public int getPort() {
		return savedPortNumber;
	}

	public int getTileSize() {
		return savedTileSize;
	}

	public boolean getAutostart() {
		return savedAutostart;
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
			AppPreferences.this.savedTileSize = size;
			System.out.println("set the tilesize to" + AppPreferences.this.savedTileSize);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {}

		public Object getValue(String key) {
			return values.get(key);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void putValue(String key, Object value) {
			values.put(key, value);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {}

		public void setEnabled(boolean b) {
			this.enabled = b;
		}
	}

	private void savePrefs() {
		Ref.setDefaultFileSize(savedTileSize);
		if (savedAutostart != autoshare.isSelected()) {
			savedAutostart = autoshare.isSelected();
			Ref.setAutostart(savedAutostart);
		}
		if (savedPortNumber != ((Integer) portNumber.getValue()).intValue()) {
			savedPortNumber = ((Integer) portNumber.getValue()).intValue();
			Ref.setDefaultPort(savedPortNumber);

			synchronized (portNumberChangeLock) {
				portNumberChangeLock.notifyAll();
			}
		}
		Ref.setCheckForUpdates(checkForUpdates.isSelected());

	}

	public JButton getPreferencesButton() {
		JButton showPrefs = FontLoader.getButton(FontLoader.iconPrefs);
		showPrefs.addActionListener(new AppPreferencesAction());
		showPrefs.setToolTipText(SHOWPREFS_TOOLTIP);
		return showPrefs;
	}

	private void readPrefs() {

		savedPortNumber = Ref.getDefaultPort();
		portNumber.setValue(savedPortNumber);

		savedAutostart = Ref.getAutostart();
		autoshare.setSelected(savedAutostart);

		checkForUpdates.setSelected(Ref.getCheckForUpdates());

		switch (Ref.getDefaultTileSize()) {
		case 256:
			_256.setSelected(true);
			break;
		case 384:
			_384.setSelected(true);
			break;
		case 512:
			_512.setSelected(true);
			break;
		}
	}

	public class AppPreferencesAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComponent parentComponent = (JComponent) e.getSource();
			int selected = JOptionPane.showOptionDialog(parentComponent, getPreferences(), "Preferences", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, null, null);
			System.out.println("selected:" + selected);
			if (selected == JOptionPane.OK_OPTION) {
				savePrefs();
			} else {
				readPrefs();
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Preferences");
		frame.setContentPane(new JPanel());
		AppPreferencesAction action = getPreferences().new AppPreferencesAction();
		action.actionPerformed(new ActionEvent(frame.getContentPane(), 0, "whatever"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
