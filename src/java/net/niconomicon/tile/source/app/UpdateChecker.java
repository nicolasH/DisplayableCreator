package net.niconomicon.tile.source.app;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class UpdateChecker {

	public static String BASE_VERSION = "2.0.0";
	public static final String latestVersionLocation = "http://127.0.0.1/~niko/displayatorSite/DisplayableCreator/latest";
	public static final String url_jnlp = "http://www.disp.loc/DisplayableCreator/DisplayableCreator.jnlp";

	private String current_version = "";

	private String getLocalInfos() {
		Properties props = System.getProperties();
		String[] needed = new String[] { "java.version", "os.name", "os.version" };
		String sys_infos = "";
		for (String key : needed) {
			sys_infos += props.getProperty(key) + "|";
		}
		sys_infos = sys_infos.substring(0, sys_infos.length() - 1);
		current_version = BASE_VERSION;
		try {
			URL pom_props_url = DisplayableCreatorApp.class.getClassLoader().getResource(
					"META-INF/maven/net.niconomicon/displayable-creator/pom.properties");
			BufferedReader in = new BufferedReader(new InputStreamReader(pom_props_url.openStream()));
			String inputLine;
			String v = "version=";
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.startsWith(v)) {
					current_version = inputLine.substring(v.length());
					System.out.println("Found current version: " + current_version);
				}
			}

		} catch (Exception e) {
			System.out.println("Can't find the current version");
			current_version = BASE_VERSION;
		}
		sys_infos += "|" + current_version;
		return sys_infos;
	}

	public void checkForUpdate(JComponent parent, boolean popupIfNecessary) {
		String new_version = BASE_VERSION;
		String sys_infos = getLocalInfos();
		try {
			System.out.println("[" + sys_infos + "]");
			String link = "http://127.0.0.1/~niko/displayatorSite/DisplayableCreator/latest?" + sys_infos;
			URL url = new URL(link);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine = in.readLine();
			if (inputLine != null) {
				new_version = inputLine;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!new_version.equalsIgnoreCase(current_version)) {
			System.out.println("A new version of the Displayable Creator is available: " + new_version + " you have: " + current_version);
			String question = "<html><body>A new version of the Displayable Creator is available: " + new_version + " you have: " + current_version
					+ "<br>Start the new version now?</body></html>";
			int option = JOptionPane.showConfirmDialog(parent, question, "Update Available", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				try {
					Desktop.getDesktop().browse(new URI(url_jnlp));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ////////////////////////////
	// //////////////////////////////

	private static void compare(String v1, String v2) {
		String s1 = normalisedVersion(v1);
		String s2 = normalisedVersion(v2);
		int cmp = s1.compareTo(s2);
		String cmpStr = cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
		System.out.printf("'%s' %s '%s'%n", v1, cmpStr, v2);
	}

	public static String normalisedVersion(String version) {
		return normalisedVersion(version, ".", 4);
	}

	public static String normalisedVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}

	// //////////////////////////////
	// //////////////////////////////
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame f = new JFrame();
		compare("1.1.2", "2.0.0");
		compare("1.1.2", "2.0.0beta2");
		compare("2.0.0", "2.0.0beta2");
		JLabel l = new JLabel("Checking for update");
		f.setContentPane(new JPanel(new BorderLayout()));
		f.getContentPane().add(l);
		UpdateChecker checker = new UpdateChecker();
		checker.checkForUpdate(l, true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle("Update checker");
		f.pack();
		f.setVisible(true);

	}
}
