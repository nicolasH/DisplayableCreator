package net.niconomicon.tile.source.app.help;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import net.niconomicon.tile.source.app.fonts.FontLoader;

public class HelpWidget extends JButton {

	static final int columns = 20;
	static final String HELP_TOOLTIP = "Shows the help for Displayable Creator";

	private JFrame helpFrame;
	private static HelpWidget wi;
	private static final String HELP_TITLE = "Displayable Creator Help";

	private final Dimension helpPanelMinDim = new Dimension(600, 500);
	private final Dimension helpPanelPrefDim = new Dimension(800, 700);
	private final Dimension helpPanelMaxDim = new Dimension(800, 2000);

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


		HelpLoader hl = new HelpLoader();
		JScrollPane pane = new JScrollPane();
		JEditorPane editor = new JEditorPane();
		try {
			editor.setPage(hl.help_url);
			editor.setEditable(false);
//			HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
			editor.setAutoscrolls(true);
			editor.setMinimumSize(helpPanelMinDim);
			editor.setMaximumSize(helpPanelMaxDim);
			editor.setPreferredSize(helpPanelMaxDim);

			System.out.println("Editor:"+editor.getPreferredScrollableViewportSize());
			System.out.println("editor.width"+editor.getWidth());
			pane = new JScrollPane(editor);
			pane.setMaximumSize(helpPanelMaxDim);			
			System.out.println("Editor:"+editor.getPreferredScrollableViewportSize());
			System.out.println("editor.width"+editor.getWidth());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pane;
	}

	public void showHelp() {
		helpFrame.setVisible(true);
	}

	public static void main(String[] args) {
		HelpWidget widget = HelpWidget.createHelpWidget();
		widget.showHelp();
		widget.helpFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}