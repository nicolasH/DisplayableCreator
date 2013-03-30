package net.niconomicon.tile.source.app.help;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.niconomicon.tile.source.app.fonts.FontLoader;

public class HelpWidget extends JButton {

	static final String helpLocation = "net/niconomicon/tile/source/app/help/html/";
	static final int columns = 20;
	static final String HELP_TOOLTIP = "Shows the help for Displayable Creator";

	private JFrame helpFrame;
	private static HelpWidget wi;
	private static final String HELP_TITLE = "Displayable Creator Help";

	private final Dimension helpPanelMinDim = new Dimension(600, 500);
	private final Dimension helpPanelPrefDim = new Dimension(700, 700);
	private final Dimension helpPanelMaxDim = new Dimension(700, 2000);

	public static HelpWidget createHelpWidget() {
		if (wi == null) {
			wi = new HelpWidget();
		}
		return wi;
	}

	private HelpWidget() {
		super();
		this.setToolTipText(HELP_TOOLTIP);
		FontLoader.iconifyButton(this, FontLoader.iconHelp);
		helpFrame = new JFrame(HELP_TITLE);
		helpFrame.setSize(helpPanelPrefDim);
		helpFrame.setContentPane(getHelpPanel());
		helpFrame.pack();
		helpFrame.setSize(helpPanelPrefDim);
		helpFrame.setMinimumSize(helpPanelMinDim);

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

	private Container getHelpPanel() {
		try {
			Enumeration<URL> elements = this.getClass().getClassLoader().getResources("index.html");
			while (elements.hasMoreElements()) {
				System.out.println("----"+elements.nextElement());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		URL help_url = this.getClass().getClassLoader().getResource(helpLocation+"index.html");
		System.out.println(help_url);
		JScrollPane pane = new JScrollPane();
		JEditorPane editor = new JEditorPane();
		try {
			editor.setPage(help_url);
			editor.setEditable(false);
			editor.setAutoscrolls(true);
			editor.setSize(helpPanelPrefDim);
			editor.setMinimumSize(helpPanelMinDim);
			editor.setMaximumSize(helpPanelMaxDim);
			editor.setPreferredSize(helpPanelMaxDim);

			for (Component c : editor.getComponents()) {
				System.out.println(c.getName() + ":" + c.getSize() + " -- " + c.getMaximumSize() + " -- " + c.getMinimumSize() + " -- "
						+ c.getPreferredSize());
			}

			pane = new JScrollPane(editor);
			pane.setMaximumSize(helpPanelMaxDim);
			pane.setSize(helpPanelPrefDim);
			pane.setPreferredSize(helpPanelPrefDim);
			pane.setMinimumSize(helpPanelMinDim);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pane;
	}

	public void showHelp() {
		helpFrame.setVisible(true);
	}

	public static void main(String[] args) {
		String index = helpLocation + "index.html";
		System.out.println(index);
		HelpWidget wi = new HelpWidget();
		try {
			URL help_url = wi.getClass().getClassLoader().getResource(index);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		HelpWidget widget = HelpWidget.createHelpWidget();
		widget.showHelp();
		widget.helpFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
