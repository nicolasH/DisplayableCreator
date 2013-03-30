package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class UpdateChecker {

	public static String BASE_VERSION = "2.0.0";
	public static final String latestVersionLocation = "http://www.displayator.com/DisplayableCreator/latest";
	public static final String url_jnlp = "http://www.displayator.com/DisplayableCreator/DisplayableCreator.jnlp";

	private static String[] getLocalInfos() {
		Properties props = System.getProperties();
		String[] needed = new String[] { "java.version", "os.name", "os.version" };
		String sys_infos = "";
		for (String key : needed) {
			sys_infos += props.getProperty(key) + "|";
		}
		sys_infos = sys_infos.substring(0, sys_infos.length() - 1);
		String current_version = BASE_VERSION;
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
		return new String[] { sys_infos, current_version };
	}

	public static String getNewestVersion(String sys_infos) {
		String new_version = "";
		try {
			String link = latestVersionLocation + "?" + sys_infos;
			URL url = new URL(link);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine = in.readLine();
			if (inputLine != null) {
				new_version = inputLine;
			}
			// System.out.println("Got hold of the new version infos");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new_version;
	}

	public static void checkForUpdate(JComponent parent, boolean popupIfNotNecessary, boolean ignoreIgnoredVersion) {
		String[] tmp = getLocalInfos();
		String sys_infos = tmp[0];
		DefaultArtifactVersion current_version = new DefaultArtifactVersion(tmp[1]);
		DefaultArtifactVersion new_version = new DefaultArtifactVersion(getNewestVersion(sys_infos));

		if (new_version.toString().equals("")) {
			if (popupIfNotNecessary) {
				String message = "Error while trying to get news of a newer version. Please try again later.";
				JOptionPane.showMessageDialog(parent, message, "No newer version available", JOptionPane.INFORMATION_MESSAGE);
			}
			return;
		}
		if (new_version.compareTo(current_version) > 0) {
			if (!new_version.toString().equals(Ref.getWarnAboutUpdates()) || ignoreIgnoredVersion) {
				String title = "New Version Available";
				String question = "<html><body>A new version of the Displayable Creator is available: " + new_version + " you have: "
						+ current_version + "<br>Start the new version now?</body></html>";

				String[] options;
				if (ignoreIgnoredVersion) {
					options = new String[] { "Start now", "No" };
				} else {
					options = new String[] { "Start now", "No", "Ignore " + new_version };
				}
				Icon icon = UIManager.getIcon("OptionPane.questionIcon");
				int option = JOptionPane.showOptionDialog(parent, question, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
						icon, options, null);

				if (option == JOptionPane.OK_OPTION) {
					Ref.setWarnAboutUpdates("");
					try {
						Desktop.getDesktop().browse(new URI(url_jnlp));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (option == JOptionPane.NO_OPTION) {
					Ref.setWarnAboutUpdates("");
					System.out.println("Option == NO");
					// do nothing
				}
				if (option == JOptionPane.CANCEL_OPTION) {
					System.out.println("Option == CANCEL");
					Ref.setWarnAboutUpdates(new_version.toString());
				}
			}
		} else {
			if (popupIfNotNecessary) {
				String message = "You have the latest version available: " + current_version;
				JOptionPane.showMessageDialog(parent, message, "No newer version available", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	// ////////////////////////////
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame f = new JFrame();
		JLabel l = new JLabel("Checking for update");
		f.setContentPane(new JPanel(new BorderLayout()));
		f.getContentPane().add(l);
		checkForUpdate(l, true, true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle("Update checker");
		f.pack();
		f.setVisible(true);

	}
}
