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

	public URL help_url;

	static final String helpLocation = "net/niconomicon/tile/source/app/help/";
	static final String imagesLocation = helpLocation+"img/";
	private static HelpLoader icons;

	public HelpLoader() {
		URL url;

		url = this.getClass().getClassLoader().getResource(imagesLocation + "main.png");
		ic_main = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(imagesLocation + "list.png");
		ic_list = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(imagesLocation + "prefs.png");
		ic_prefs = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(imagesLocation + "view.png");
		ic_view = new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(imagesLocation + "save.png");
		ic_save = new ImageIcon(url);

		help_url = this.getClass().getClassLoader().getResource(helpLocation + "index.html");

	}

//	public String readURL(URL url) {
//		String outputLine = "";
//		try {
//			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//
//			String inputLine;
//			while ((inputLine = in.readLine()) != null) {
//				System.out.println(inputLine);
//				outputLine += inputLine + "\n";
//			}
//			in.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return outputLine;
//	}
}