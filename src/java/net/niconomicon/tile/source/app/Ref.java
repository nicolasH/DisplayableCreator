/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author niko
 * 
 */
public final class Ref {

	public static int sharing_port = 8889;
	public static final String storingDirectoryKey = "TileSetStoringDirectoryKey";
	public static final String sharing_serviceName = "TiledImageSharingService";

	public static final String sharing_xmlRef = "TiledImages.xml";
	public static final String sharing_htmlRef = "TiledImages.html";

	public static final String app_handle = "displayator-image:";

	public static final String ext_db = ".mdb";
	public static final String ext_thumb = ".thumb";
	public static final String ext_mini = ".mini";

	public static final String layers_infos_table_name = "layers_infos";

	public static final String infos_table_name = "infos";
	public static final String infos_title = "title";
	public static final String infos_description = "description";
	public static final String infos_author = "author";
	public static final String infos_source = "source";
	public static final String infos_date = "date";
	public static final String infos_zindex = "zindex";
	public static final String infos_width = "width";
	public static final String infos_height = "height";
	public static final String infos_miniature = "miniature";
	public static final String infos_thumb = "thumb";

	public static final String fileSansDot(String fullPath) {
		return fullPath.substring(fullPath.lastIndexOf(File.separator) + 1, fullPath.lastIndexOf("."));
	}

	public static final String pathSansFile(String fullPath) {
		return fullPath.substring(0, fullPath.lastIndexOf(File.separator) + 1);
	}

	public static final String pathSansFileSansSep(String fullPath) {
		return fullPath.substring(0, fullPath.lastIndexOf(File.separator));
	}

	public static final FilenameFilter ext_db_filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(ext_db);
		}
	};

	public static String[] getDBFiles(String dirPath) {
		File dir = new File(dirPath);
		return dir.list(Ref.ext_db_filter);
	}

	public static String getDirOrTmpDir(String saveDirectory) {
		if (saveDirectory != null) {
			File f = new File(saveDirectory);
			if (!f.exists()) {
				System.out.println("file directory [" + saveDirectory + "] does not exists");
				if (!f.mkdir()) {
					System.out.println("Could not create directory [" + saveDirectory + "] Gonna return the tmp directory");
					saveDirectory = null;
				}
			}
			if (!f.isDirectory()) { // finer
				System.out.println("File is not a directory [" + saveDirectory + "] going to return the temporary directory.");
				saveDirectory = null;
			}
		}
		if (saveDirectory == null) {
			try {
				File f = File.createTempFile("yuk", ".remove");
				saveDirectory = f.getParent();
				f.delete();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!saveDirectory.endsWith(File.separator)) {
			saveDirectory = saveDirectory + File.separator;
		}
		return saveDirectory;
	}

	public static void extractThumbsAndMiniToTmpFile(Map<String, String> maps) {
		for (String key : maps.keySet()) {
			String file = maps.get(key);
			System.out.println("Extracting for Key : [" + key + "]");// + " value : "+ maps.get(key));
			if (key.endsWith(Ref.ext_db) || key.endsWith(Ref.sharing_xmlRef) || key.endsWith(Ref.sharing_htmlRef)) {
				continue;
			}
			System.out.println("Really trying to open (k=[" + key + "]) => " + file);
			try {
				String query = "";
				String field = "";

				if (key.endsWith(Ref.ext_mini)) {
					System.out.println("Extracting mini from the map :" + file);
					query = "select " + Ref.infos_miniature + " from infos";
					field = Ref.infos_miniature;
				}
				if (key.endsWith(Ref.ext_thumb)) {
					System.out.println("Extracting thumb from the map :" + file);
					query = "select " + Ref.infos_thumb + " from infos";
					field = Ref.infos_thumb;
				}

				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
				connection.setReadOnly(true);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(5); // set timeout to 30 sec.

				ResultSet rs = statement.executeQuery(query);
				while (rs.next()) {
					File temp = File.createTempFile(key, "tmp");
					temp.deleteOnExit();
					FileOutputStream oStream = new FileOutputStream(temp);
					oStream.write(rs.getBytes(field));
					oStream.close();
					maps.put(key, temp.getAbsolutePath());
					System.out.println("Wrote " + key + " into " + temp.getAbsolutePath());
					break;
				}

				if (connection != null) connection.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Method to generate the map xml list.
	 */
	public static Map<String, String> generateIndexFromFileNames(Collection<String> maps) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Map<String, String> urlToFile = new HashMap<String, String>();
		StringBuffer xml = new StringBuffer();
		StringBuffer html = new StringBuffer();
		xml.append("<maps>");
		html.append("<html><body>");
		for (String mapFileName : maps) {
			try {
				File f = new File(mapFileName);
				long size = f.length();
				System.out.println("trying to open the map :" + mapFileName + " To generate the XML.");
				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + mapFileName);
				connection.setReadOnly(true);
				String[] descriptions = generateDescriptionsForConnection(connection, size, mapFileName, urlToFile);
				xml.append(descriptions[0]);
				html.append(descriptions[1]);
				// html.append(generateHTMLForConnection(connection,size,mapFileName));
				if (connection != null) connection.close();
			} catch (Exception ex) {
				System.out.println("failed to get infos for [" + mapFileName + "]");
				ex.printStackTrace();
			}
		}
		xml.append("</maps>");
		urlToFile.put("/" + sharing_xmlRef, xml.toString());

		html.append("</body></html>");
		urlToFile.put("/" + sharing_htmlRef, html.toString());
		// System.out.println(html.toString());
		return urlToFile;
	}

	/**
	 * Generate the XML description for a given map, and add thumbnail and preview images to the list of available URLs
	 * 
	 * @param mapDB
	 * @param weight
	 * @param fileName
	 * @param urlToFile
	 * @return an array of XML and HTML description of the file as String[]{XML,HTML}.
	 */
	public static String[] generateDescriptionsForConnection(Connection mapDB, long weight, String fileName, Map<String, String> urlToFile) {
		String ret = "";
		String h = "";
		try {
			// create a database connection

			Statement statement = mapDB.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select * from infos");
			while (rs.next()) {
				// read the result set
				String name = fileName.contains(File.separator) ? "/" + fileName.substring(fileName.lastIndexOf(File.separator) + 1) : fileName;
				name = name.replace(' ', '_');
				String mini = name + Ref.ext_mini;
				String thumb = name + Ref.ext_thumb;
				// name = name + Ref.ext_db;

				urlToFile.put(name, fileName);
				urlToFile.put(mini, fileName);
				urlToFile.put(thumb, fileName);

				String s = "<map>\n\t";
				s += "<id>" + "</id>\n\t";
				s += "<title>" + rs.getString(Ref.infos_title) + "</title>\n\t";
				s += "<updated>" + "2009-03-27T14:51:00Z" + "</updated>\n\t";
				s += "<link rel=\"source\" type=\"application/sqlite\" href=\"" + name + "\" />\n\t";
				s += "<link rel=\"thumb\" type=\"image/png\" href=\"" + thumb + "\" />\n\t";
				s += "<link rel=\"preview\" type=\"image/png\" href=\"" + mini + "\" />\n\t";
				s += "<infos weight=\"" + weight;
				s += "\" width=\"" + rs.getLong(Ref.infos_width);
				s += "\" height=\"" + rs.getLong(Ref.infos_height) + "\"/>\n";
				s += "<description>" + rs.getString(Ref.infos_description) + "</description>\n\t";
				s += "</map>\n";
				ret += s;

				String urlInfos = "title=" + rs.getString(Ref.infos_title);
				urlInfos += "&updated=";
				urlInfos += "&thumb=" + thumb;
				urlInfos += "&preview=" + mini;
				urlInfos += "&weight=" + weight;
				urlInfos += "&width=" + rs.getLong(Ref.infos_width);
				urlInfos += "&height=" + rs.getLong(Ref.infos_height);
				urlInfos += "&description=" + rs.getString(Ref.infos_description);

				String li = "\t\t\t<li>";
				String li_ = "</li>\n";
				String html = "<div><b>" + rs.getString(Ref.infos_title) + "</b><br/>";
				html += "\n\t\t<a href=\"" + mini + "\">\n\t\t\t<img src=\"" + thumb + "\">\n\t\t</a>\n\t\t";
				html += "Download <a href=\"" + app_handle + name + "?" + urlInfos + "\" >and view with displayator</a>\n\t\t";
				html += "or <a href=\"" + name + "\" >as file</a>.";
				html += "\n\t<ul>";
				html += li + "Weight : " + ((double) weight) / 1000000 + " MB." + li_;
				html += li + "Size : " + rs.getLong(Ref.infos_width) + " x " + rs.getLong(Ref.infos_height) + "px." + li_;
				String dsc = rs.getString(Ref.infos_description);
				if (null != dsc && dsc.length() > 0) {
					html += li + "description:" + dsc + li_;
				}
				html += "</ul>\n";
				html += "</div>\n";
				h += html;

			}
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}

		// /////////////////////////////

		return new String[] { ret, h };
	}

	public static String generateHTMLForConnection(Connection mapDB, long weight, String fileName) {
		String ret = "";
		try {
			// create a database connection

			Statement statement = mapDB.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select * from infos");
			while (rs.next()) {
				// read the result set
				String name = fileName.contains(File.separator) ? "/" + fileName.substring(fileName.lastIndexOf(File.separator) + 1) : fileName;
				name = name.replace(' ', '_');
				String mini = name + Ref.ext_mini;
				String thumb = name + Ref.ext_thumb;
				// name = name + Ref.ext_db;

				String html = "<div><b>" + rs.getString(Ref.infos_title) + "</title>\n\t";
				html += "<a href=\"" + name + "\" >" + name + "</a>\n\t";
				html += "<a href=\"" + thumb + "\" ><img src=\"" + thumb + "\"></a>\n\t";
				html += "<a href=\"" + mini + "\" ><img src=\"" + mini + "\"></a>\n\t";
				html += "size : " + weight;
				html += "bytes.  width : " + rs.getLong(Ref.infos_width);
				html += "pixels height : " + rs.getLong(Ref.infos_height);
				html += "pixels.<br/> description:" + rs.getString(Ref.infos_description) + "\n\t";
				html += "</div>\n";
				ret += html;
			}
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}

		// /////////////////////////////

		return ret;
	}

}
