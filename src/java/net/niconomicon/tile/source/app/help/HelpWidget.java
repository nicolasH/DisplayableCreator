package net.niconomicon.tile.source.app.help;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLDocument.HTMLReader;

import net.niconomicon.tile.source.app.fonts.FontLoader;

public class HelpWidget extends JButton {

	static final int columns = 20;
	private JFrame helpFrame;
	private static HelpWidget wi;
	private static final String HELP_TITLE = "Displayable Creator Help";

	private final Dimension helpPrefDim = new Dimension(800, 600);
	private final Dimension helpPanelMinDim = new Dimension(800, 2000);
	private final Dimension helpPanelPrefDim = new Dimension(800, 2000);
	private final Dimension helpPanelMaxDim = new Dimension(800, 2000);

	public static HelpWidget createHelpWidget() {
		if (wi == null) {
			wi = new HelpWidget();
		}
		return wi;
	}

	private HelpWidget() {
		super();
		FontLoader.iconifyButton(this, FontLoader.iconHelp);
		helpFrame = new JFrame(HELP_TITLE);
		helpFrame.setSize(helpPrefDim);
		helpFrame.setContentPane(getHelpPanel());
		helpFrame.pack();

		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (helpFrame.isVisible()) {
					helpFrame.requestFocus();
				} else {
					helpFrame.pack();
					helpFrame.setVisible(true);
				}
			}
		});
	}

	private JPanel getHelpPanel() {
		JPanel help = new JPanel();
		//help.setLayout(new BoxLayout(help, BoxLayout.Y_AXIS));
		help.setLayout(new GridLayout(0, 1));
		
		help.setMinimumSize(helpPanelMinDim);
		help.setMaximumSize(helpPanelMaxDim);
		help.setPreferredSize(helpPanelPrefDim);
		
		
		HelpLoader hl = new HelpLoader();
		
		help.add(FontLoader.getBoringTextArea(help, columns, hl.text_top));// top
		help.add(FontLoader.getBoringTextArea(help, columns, hl.text_main));// main
		help.add(new JLabel(hl.ic_main));
		help.add(FontLoader.getBoringTextArea(help, columns, hl.text_list));// list
		help.add(new JLabel(hl.ic_list));
		help.add(FontLoader.getBoringTextArea(help, columns, hl.text_prefs));// prefs
		help.add(new JLabel(hl.ic_prefs));
		help.add(FontLoader.getBoringTextArea(help, columns, hl.text_view));// view
		help.add(new JLabel(hl.ic_view));
		help.setOpaque(true);
//		help.setBackground(Color.GREEN);
		
		
		JPanel p = new JPanel(new BorderLayout());
		JScrollPane sp = new JScrollPane(help);
//		sp.setMaximumSize(helpPrefDim);
		
		p.add(sp, BorderLayout.CENTER);
//		p.setMaximumSize(helpPrefDim);
		
		return p;
	}
}
