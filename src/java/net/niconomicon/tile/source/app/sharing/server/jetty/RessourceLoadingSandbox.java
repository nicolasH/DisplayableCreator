package net.niconomicon.tile.source.app.sharing.server.jetty;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class RessourceLoadingSandbox {

	public RessourceLoadingSandbox() {
		String s;
		URL url = null;
		try {
			s = "net/niconomicon/tile/source/app/sharing/server/jetty/index.css";
			url = this.getClass().getClassLoader().getResource(s);
			System.out.println("s = " + s + " url : " + url);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RessourceLoadingSandbox sbx = new RessourceLoadingSandbox();
	}
}
