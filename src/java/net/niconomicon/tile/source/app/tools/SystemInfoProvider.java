package net.niconomicon.tile.source.app.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import net.niconomicon.tile.source.app.DisplayableCreatorApp;

public class SystemInfoProvider {

	public static String BASE_VERSION = "2.0.0";
	public static final String latestVersionLocation = "http://www.displayator.com/DisplayableCreator/latest";
	public static final String url_jnlp = "http://www.displayator.com/DisplayableCreator/DisplayableCreator.jnlp";
	public static final String url_pom = "META-INF/maven/net.niconomicon/displayable-creator/pom.properties";

	public static String getSystemProperties() {
		Properties props = System.getProperties();
		String[] needed = new String[] { "java.runtime.version", "sun.arch.data.model", "com.ibm.vm.bitmode", "os.name", "os.version", "os.arch" };
		String sys_infos = "";
		for (String key : needed) {
			sys_infos += props.getProperty(key) + "|";
		}
		sys_infos = sys_infos.substring(0, sys_infos.length() - 1);
		return sys_infos;
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

	
	public static String[] getLocalInfos() {

		String current_version = BASE_VERSION;
		try {
			URL pom_props_url = DisplayableCreatorApp.class.getClassLoader().getResource(url_pom);
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
		String sys_infos = SystemInfoProvider.getSystemProperties();
		sys_infos += "|" + current_version;
		return new String[] { sys_infos, current_version };
	}
	
}
