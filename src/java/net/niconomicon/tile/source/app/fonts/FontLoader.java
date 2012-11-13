package net.niconomicon.tile.source.app.fonts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;

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

	public static String iconAction = "\ue047";
	public static String iconWait = "\ue008";

	public static String iconList = "\ue046";
	public static String iconPrefs = "\ue005";
	public static String iconExport = "\ue007";

	public static String iconView = "\ue00a";
	public static String iconEdit = "\ue000";
	public static String iconSave = "\ue037";
	public static String iconTrash = "\ue00d";

	public static String iconZoomIn = "\ue03d";
	public static String iconZoomOut = "\ue03c";
	public static String iconExpand = "\ue03f";
	public static String iconContract = "\ue041";

	public static final Dimension btnSize = new Dimension(40, 40);
	public static final Dimension btnSizeSmall = new Dimension(30, 30);
	
	public static JButton getButton(String string) {
		JButton b = new JButton(string);
		b.setFont(getFontLoader().icomoon.deriveFont(24.0f));
		b.setMaximumSize(btnSize);
		b.setMinimumSize(btnSize);
		b.setSize(btnSize);
		b.setPreferredSize(btnSize);
		return b;
	}

	public static JButton getButtonSmall(String string) {
		JButton b = new JButton(string);
		b.setFont(getFontLoader().icomoon.deriveFont(16.0f));
		b.setMaximumSize(btnSizeSmall);
		b.setMinimumSize(btnSizeSmall);
		b.setSize(btnSizeSmall);
		b.setPreferredSize(btnSizeSmall);
		return b;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		FontLoader.loader = new FontLoader();

		JPanel p = new JPanel();
		GridLayout layout = new GridLayout(0, 10);// , BoxLayout.PAGE_AXIS);
		p.setLayout(layout);

		JButton actionGreen = getButton(FontLoader.iconAction);
		actionGreen.setForeground(Color.GREEN);

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
		p.add(new JLabel(""));

		p.add(actionOrange);
		p.add(new JLabel(""));

		p.add(getButton(FontLoader.iconWait));
		p.add(new JLabel(""));

		p.add(getButton(FontLoader.iconList));
		p.add(getButton(FontLoader.iconPrefs));
		p.add(getButton(FontLoader.iconExport));
		p.add(new JLabel(""));
		p.add(getButton(FontLoader.iconEdit));
		p.add(getButton(FontLoader.iconSave));
		p.add(getButton(FontLoader.iconView));
		p.add(getButton(FontLoader.iconTrash));
		p.add(new JLabel(""));
		p.add(getButton(FontLoader.iconZoomIn));
		p.add(getButton(FontLoader.iconZoomOut));
		p.add(getButton(FontLoader.iconExpand));
		p.add(getButton(FontLoader.iconContract));

		p.add(new JLabel(""));
		p.add(new JLabel(""));
		p.add(new JLabel(""));
		for (int i = 0; i < 100; i++) {
			int codePoint = i + 57343;
			String s = new String(Character.toChars(codePoint));
			JButton b = new JButton(s);
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
