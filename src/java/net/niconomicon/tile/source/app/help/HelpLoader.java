/**
 * 
 */
package net.niconomicon.tile.source.app.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

/**
 * @author Nicolas Hoibian
 * 
 */
public class HelpLoader {

	public ImageIcon ic_main;
	public ImageIcon ic_list;
	public ImageIcon ic_prefs;
	public ImageIcon ic_view;
	public ImageIcon ic_save;

	 public String text_top;
	public String text_main;
	public String text_list;
	public String text_prefs;
	public String text_view;
	public String text_save;

	static final String iconsLocation = "net/niconomicon/tile/source/app/help/";
	private static HelpLoader icons;

	public HelpLoader() {
		URL url;

		url = this.getClass().getClassLoader().getResource(iconsLocation + "main.png");
		ic_main = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "list.png");
		ic_list = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "prefs.png");
		ic_prefs = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "view.png");
		ic_view = new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(iconsLocation + "save.png");
		ic_save = new ImageIcon(url);

		text_top = readURL(this.getClass().getClassLoader().getResource(iconsLocation + "header.html"));
		text_main = readURL(this.getClass().getClassLoader().getResource(iconsLocation + "main.html"));
		text_list = readURL(this.getClass().getClassLoader().getResource(iconsLocation + "list.html"));
		text_prefs = readURL(this.getClass().getClassLoader().getResource(iconsLocation + "prefs.html"));
		text_view = readURL(this.getClass().getClassLoader().getResource(iconsLocation + "view.html"));
		text_save = readURL(this.getClass().getClassLoader().getResource(iconsLocation + "save.html"));

	}

	public String readURL(URL url) {
		String outputLine = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				outputLine += inputLine + "\n";
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputLine;
	}
}