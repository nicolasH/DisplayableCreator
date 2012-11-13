package net.niconomicon.tile.source.app.fonts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JButton;
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

	public FontLoader() throws FontFormatException, IOException {
		URL url = this.getClass().getClassLoader().getResource(fontLocation + "icomoon.ttf");
		icomoon = Font.createFont(Font.TRUETYPE_FONT, url.openStream());

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		FontLoader.loader = new FontLoader();

		JPanel p = new JPanel();
		GridLayout layout = new GridLayout(0, 10);// , BoxLayout.PAGE_AXIS);
		p.setLayout(layout);
		
		JLabel j = new JLabel("");
		p.add(j);
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
			p.add(character,BorderLayout.NORTH);

			JTextArea explanation = new JTextArea();
			explanation.setBorder(null);
			explanation.setEditable(false);
			explanation.setBackground(parent.getBackground());
			explanation.setWrapStyleWord(true);
			explanation.setLineWrap(true);
			explanation.setColumns(30);
			explanation.setText(message);
			p.add(explanation,BorderLayout.CENTER);
			JOptionPane.showMessageDialog(parent, p);
		}
	}
}
