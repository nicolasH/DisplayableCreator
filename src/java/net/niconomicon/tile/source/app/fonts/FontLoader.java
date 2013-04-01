package net.niconomicon.tile.source.app.fonts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class FontLoader {

	static final String fontLocation = "net/niconomicon/tile/source/app/fonts/";

	Font icomoon;

	static FontLoader loader;

	public static FontLoader getFontLoader() {
		if (loader == null) {
			try {
				loader = new FontLoader();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return loader;
	}

	private FontLoader() throws FontFormatException, IOException {
		URL url = this.getClass().getClassLoader().getResource(fontLocation + "icomoon.ttf");
		icomoon = Font.createFont(Font.TRUETYPE_FONT, url.openStream());
	}

	public Font getFont() {
		return icomoon;
	}

	public static String iconAction = "\ue000";
	public static String iconWait = "\ue001";

	public static String iconList = "\ue002";
	public static String iconPrefs = "\ue003";
	public static String iconHelp = "\ue004";

	public static String iconView = "\ue005";
	public static String iconEdit = "\ue006";
	public static String iconSave = "\ue007";
	public static String iconRemove = "\ue008";
	public static String iconError = "\ue009";
	public static String iconExport = "\ue00a";
//	public static String iconTrash = "\ue00b";

	public static String iconZoomIn = "\ue00b";
	public static String iconZoomOut = "\ue00c";
	public static String iconExpand = "\ue00d";
	public static String iconContract = "\ue00e";

	public static String iconDash = "\ue00f";

	public static final Dimension btnSize = new Dimension(40, 40);
	public static final Dimension btnMaxSize = new Dimension(50, 50);

	public static final Dimension btnSizeSmall = new Dimension(30, 30);

	private static final Color defaultBackground = new JLabel().getBackground();

	public static JTextArea getBoringTextArea(JComponent backgroundSource, int columns, String text) {
		JTextArea area = new JTextArea();
		area.setBorder(null);
		area.setEditable(false);
		area.setForeground(Color.black);
		area.setBackground(backgroundSource.getBackground());
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		area.setColumns(columns);
		area.setText(text);
		return area;
	}

	public static JButton getButton(String icon) {
		JButton b = new JButton();
		iconifyButton(b, icon);
		return b;
	}

	public static void iconifyButton(JButton b, String icon) {
		b.setText(icon);
		b.setFont(getFontLoader().icomoon.deriveFont(24.0f));
		b.setMinimumSize(btnSize);
		b.setSize(btnSize);
		b.setPreferredSize(btnSize);
		b.setMaximumSize(btnMaxSize);
		b.setMargin(new Insets(2, 2, 2, 2));
		b.setForeground(Color.DARK_GRAY);
	}

	public static JButton getButtonFlatSmall(String string) {
		JButton b = new JButton(string);
		adjustButtonFlatSmall(b);
		return b;
	}

	public static void adjustButtonFlatSmall(JButton b) {
		b.setFont(getFontLoader().icomoon.deriveFont(16.0f));
		b.setMaximumSize(btnSizeSmall);
		b.setMinimumSize(btnSizeSmall);
		b.setSize(btnSizeSmall);
		b.setPreferredSize(btnSizeSmall);
		b.setForeground(Color.DARK_GRAY);
		b.setBorder(null);
		b.setBackground(defaultBackground);
		b.setOpaque(false);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

		FontLoader.loader = new FontLoader();

		JPanel p = new JPanel();
		GridLayout layout = new GridLayout(0, 10);// , BoxLayout.PAGE_AXIS);
		p.setLayout(layout);

		JButton actionGreen = getButton(FontLoader.iconAction);
		actionGreen.setForeground(Color.GREEN);

		JCheckBox box = new JCheckBox(FontLoader.iconAction);
		box.setFont(actionGreen.getFont());

		p.add(box);

		JButton actionGray = getButton(FontLoader.iconAction);
		actionGray.setForeground(Color.GRAY);

		JButton action = getButton(FontLoader.iconAction);
		action.setEnabled(false);

		JButton actionOrange = getButton(FontLoader.iconAction);
		actionOrange.setForeground(Color.ORANGE);

		JButton actionWait = getButton(FontLoader.iconWait);
		actionWait.setEnabled(false);

		p.add(actionGreen);
		p.add(actionWait);
		p.add(actionGray);
		p.add(actionOrange);
		p.add(new JLabel(""));
		p.add(getButton(FontLoader.iconList));
		p.add(getButton(FontLoader.iconPrefs));
		p.add(getButton(FontLoader.iconExport));
		p.add(new JLabel(""));
		p.add(new JLabel(""));

		p.add(getButtonFlatSmall(FontLoader.iconEdit));
		p.add(getButtonFlatSmall(FontLoader.iconSave));
		p.add(getButtonFlatSmall(FontLoader.iconView));
		p.add(getButtonFlatSmall(FontLoader.iconRemove));
		p.add(new JLabel(""));

		p.add(getButtonFlatSmall(FontLoader.iconZoomIn));
		p.add(getButtonFlatSmall(FontLoader.iconZoomOut));
		p.add(getButtonFlatSmall(FontLoader.iconExpand));
		p.add(getButtonFlatSmall(FontLoader.iconContract));
		p.add(new JLabel(""));
		p.add(getButtonFlatSmall(FontLoader.iconError));
		p.add(new JLabel(""));
		p.add(new JLabel(""));
		p.add(new JLabel(""));
		p.add(new JLabel(""));

		for (int i = 0; i < 100; i++) {
			int codePoint = i + 57343;
			String s = new String(Character.toChars(codePoint));
			JButton b = getButtonFlatSmall(s);
			b.setFont(loader.icomoon);
			b.setFont(b.getFont().deriveFont(24.0f));
			b.setSize(new Dimension(40, 40));
			b.setMaximumSize(new Dimension(60, 60));
			b.addActionListener(loader.new IconFontAction(codePoint));
			p.add(b);

		}
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(p);
		frame.pack();
		frame.setVisible(true);
	}

	public class IconFontAction implements ActionListener {
		int codePoint;

		public IconFontAction(int codepoint) {
			this.codePoint = codepoint;
		}

		public void actionPerformed(ActionEvent e) {
			JComponent parent = (JComponent) e.getSource();
			String message = "Codepoint: " + codePoint + " Hex:\\u" + Integer.toHexString(codePoint);
			JPanel p = new JPanel(new BorderLayout());

			String s = new String(Character.toChars(codePoint));
			JLabel character = new JLabel(s);
			character.setFont(loader.icomoon.deriveFont(24.0f));
			p.add(character, BorderLayout.NORTH);

			JTextArea explanation = new JTextArea();
			explanation.setBorder(null);
			explanation.setEditable(false);
			explanation.setBackground(parent.getBackground());
			explanation.setWrapStyleWord(true);
			explanation.setLineWrap(true);
			explanation.setColumns(30);
			explanation.setText(message);
			p.add(explanation, BorderLayout.CENTER);
			JOptionPane.showMessageDialog(parent, p);
		}
	}
}
