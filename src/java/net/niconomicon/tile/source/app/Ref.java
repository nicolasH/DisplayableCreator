/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import net.niconomicon.tile.source.app.sharing.exporter.DisplayableInfosForJSON;
import net.niconomicon.tile.source.app.viewer.structs.TileCoord;

import com.google.gson.Gson;

/**
 * @author Nicolas Hoibian This class contains the reference fields. The default
 *         port, displayable file extension, json displayable description uri
 *         and generation functions, displayable html description uri and
 *         generation functions etc ...
 */
public final class Ref {

	private static int default_sharing_port = 8889;
	private static int default_tile_size = 256;

	public static final String prefKey_storingDir = "DisplayableStoringDirectoryKey";
	public static final String prefKey_tileSize = "DisplayableTileSizeKey";
	public static final String prefKey_port = "DisplayablePortKey";

	public static final String sharing_serviceName = "DisplayableSharingService";

	public static final String sharing_jsonRef = "displayables.json";
	public static final String sharing_htmlRef = "index.html";

	public static final String sharing_cssRef = "displayableList.css";

	public static final String URI_jsonRef = "/displayables.json";
	public static final String URI_htmlRef = "/index.html";

	public static final String app_handle_item = "displayator-image:";
	public static final String app_handle_list = "displayator-list:";

	public static final String ext_db = ".disp";
	public static final String ext_thumb = ".thumb.png";
	public static final String ext_mini = ".mini.png";

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

	public static final String head = "<meta name=\"viewport\" content=\"width=500, user-scalable=yes\">" + "<link rel=\"stylesheet\" href=\""
			+ sharing_cssRef + "\" type=\"text/css\" />" + "<script type=\"text/javascript\">" + "function expandLinks(){"
			+ "var links = document.getElementsByTagName('a');\n" + "for (var i=0;\n i < links.length;\n i++) {"
			+ "    if(links[i].href.indexOf(\"displayator-list:\") == 0){"
			+ "        links[i].href = links[i].href.replace(\"displayator-list:\",\"displayator-list:\"+document.location);\n" + "    }"
			+ "    if(links[i].href.indexOf(\"displayator-image:\") == 0){"
			+ "       links[i].href = links[i].href.replace(\"displayator-image:\",\"displayator-image:\"+document.location);\n" + "       }" + " }"
			+ " }" + "</script>" + "</head>\n";

	public static File tmpFile;
	static {
		try {
			tmpFile = File.createTempFile("tmp", "tmp");
			tmpFile.deleteOnExit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static MessageDigest md;
	static {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
	}

	public static final boolean isInTmpLocation(String currentLocation) {
		if (currentLocation != null && tmpFile.getParent().compareTo(Ref.pathSansFileSansSep(currentLocation)) == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param fullPath
	 * @return the name of the file without its path and without its extension
	 *         (if any)
	 */
	public static final String fileSansDot(String fullPath) {
		int end = fullPath.lastIndexOf(".");
		if (end < 0) {
			end = fullPath.length();
		}
		return fullPath.substring(fullPath.lastIndexOf(File.separator) + 1, end);
	}

	/**
	 * 
	 * @param fullPath
	 * @return the name of the file without its path but with its extension (if
	 *         any)
	 */
	public static final String fileSansPath(String fullPath) {
		return fullPath.substring(fullPath.lastIndexOf(File.separator) + 1);
	}

	/**
	 * @param fullPath
	 * @return the files parent directory including the last file separator
	 */
	public static final String pathSansFile(String fullPath) {
		return fullPath.substring(0, fullPath.lastIndexOf(File.separator) + 1);
	}

	/**
	 * 
	 * @param string
	 * @return a safe string with all the accents removed and punctuation
	 *         characters replaced by '-'
	 */
	public static String cleanFilename(String string) {
		return Normalizer.normalize(string.toLowerCase(), Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("\\p{Punct}", "-")
				.replaceAll("\\p{Blank}", "-").replaceAll("\\p{Cntrl}", "-").replaceAll("-{2,}", "-");
	}

	/**
	 * 
	 * @param string
	 * @return a safe string with all the accents removed
	 */
	public static String cleanName(String string) {
		return Normalizer.normalize(string.toLowerCase(), Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	/**
	 * @param fullPath
	 * @return the files parent directory excluding the last file separator
	 */
	public static final String pathSansFileSansSep(String fullPath) {
		return fullPath.substring(0, fullPath.lastIndexOf(File.separator));
	}

	public static String getKey(long x, long y, long z) {
		return x + "_" + y + "_" + z;
	}

	public static String getKey(TileCoord coord) {
		return getKey(coord.x, coord.y, coord.z);
	}

	/**
	 * 
	 * @return The directory to which a Displayable was stored last.
	 */
	public static String getDefaultDir() {
		return Preferences.userNodeForPackage(Ref.class).get(Ref.prefKey_storingDir, null);
	}

	public static void setDefaultDir(String dir) {
		Preferences.userNodeForPackage(Ref.class).put(Ref.prefKey_storingDir, dir);
	}

	// /////////////////////////
	public static int getDefaultPort() {
		return (Integer.parseInt(Preferences.userNodeForPackage(Ref.class).get(Ref.prefKey_port, "" + default_sharing_port)));
	}

	public static void setDefaultPort(int port) {
		Preferences.userNodeForPackage(Ref.class).put(Ref.prefKey_port, Integer.toString(port));
	}

	// /////////////////////////
	public static int getDefaultTileSize() {
		return (Integer.parseInt(Preferences.userNodeForPackage(Ref.class).get(Ref.prefKey_tileSize, "" + default_tile_size)));
	}

	public static void setDefaultFileSize(int tileSize) {
		Preferences.userNodeForPackage(Ref.class).put(Ref.prefKey_tileSize, Integer.toString(tileSize));
	}

	public static final FilenameFilter ext_db_filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(ext_db);
		}
	};

	public static String[] getAbsolutePathOfDBFilesInDirectory(File dir) {
		String[] files = dir.list(Ref.ext_db_filter);
		for (int i = 0; i < files.length; i++) {
			files[i] = dir.getAbsolutePath() + (dir.getAbsolutePath().endsWith(File.separator) ? files[i] : File.separator + files[i]);
		}
		return files;
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
			// System.out.println("Extracting for Key : [" + key + "]");// +
			// " value : "+ maps.get(key));
			if (!key.endsWith(Ref.ext_mini) && !key.endsWith(Ref.ext_thumb)) {
				continue;
			}
			// System.out.println("Really trying to open (k=[" + key + "]) => "
			// + file);
			try {
				String query = "";
				String field = "";

				if (key.endsWith(Ref.ext_mini)) {
					// System.out.println("Extracting mini from the map :" +
					// file);
					query = "select " + Ref.infos_miniature + " from infos";
					field = Ref.infos_miniature;
				}
				if (key.endsWith(Ref.ext_thumb)) {
					// System.out.println("Extracting thumb from the map :" +
					// file);
					query = "select " + Ref.infos_thumb + " from infos";
					field = Ref.infos_thumb;
				}

				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
				connection.setReadOnly(true);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(5); // set timeout to 30 sec.

				// System.out.println("Query : " + query);
				ResultSet rs = statement.executeQuery(query);
				while (rs.next()) {
					File temp = File.createTempFile(key, "tmp");
					temp.deleteOnExit();
					FileOutputStream oStream = new FileOutputStream(temp);
					oStream.write(rs.getBytes(field));
					oStream.close();
					maps.put(key, temp.getAbsolutePath());
					// System.out.println("Wrote " + key + " into " +
					// temp.getAbsolutePath());
					break;
				}

				if (connection != null) connection.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// /////////////////////////////////////
	// /////////////////////////////////////
	// THIS! IS! JSON! //////////////////////
	// /////////////////////////////////////
	// /////////////////////////////////////
	/**
	 * Method to generate the Displayable's list.
	 */
	public static Map<String, String> generateIndexFromFileNames(Collection<String> maps) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Map<String, String> urlToFile = new HashMap<String, String>();
		StringBuffer json = new StringBuffer();
		StringBuffer html = new StringBuffer();
		json.append("[");
		html.append("<html>" + head + "<body>");
		html.append("<div class=\"feed\">Tap on the link to <a href=\"" + app_handle_list + URI_jsonRef
				+ "\">open this list with the Displayator app</a>.</div>");
		for (String mapFileName : maps) {
			try {
				File f = new File(mapFileName);
				long size = f.length();
				// System.out.println("trying to open the file :" + mapFileName
				// + " To generate the json and html.");
				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + mapFileName);
				connection.setReadOnly(true);
				String[] descriptions = generateDescriptionsForConnection(connection, size, mapFileName, urlToFile);
				json.append(descriptions[0]);
				html.append(descriptions[1]);
				if (connection != null) connection.close();
			} catch (Exception ex) {
				System.out.println("failed to get infos for [" + mapFileName + "]");
				ex.printStackTrace();
			}
		}

		if (json.lastIndexOf(",") > 0) {
			json.deleteCharAt(json.lastIndexOf(","));
		}
		json.append("]");
		urlToFile.put("/" + sharing_jsonRef, json.toString());
		html.append("<script type=\"text/javascript\">expandLinks();</script>");
		html.append("</body></html>");
		urlToFile.put(sharing_htmlRef, html.toString());
		// System.out.println(html.toString());
		return urlToFile;
	}

	public static String convertToHex(byte[] data, int count) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < Math.min(count, data.length); i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) buf.append((char) ('0' + halfbyte));
				else buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Generate the description for a given Displayable, and add thumbnail and
	 * preview images to the list of available URLs
	 * 
	 * @param mapDB
	 * @param weight
	 * @param fileName
	 * @param urlToFile
	 * @return an array of JSON and HTML description of the file as
	 *         String[]{JSON,HTML}.
	 */
	public static String[] generateDescriptionsForConnection(Connection mapDB, long weight, String fileName, Map<String, String> urlToFile) {
		String ret = "";
		String h = "";
		try {
			// create a database connection

			Statement statement = mapDB.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select * from infos");
			Gson serializer = new Gson();
			while (rs.next()) {
				// read the result set
				// String name =
				// fileName.contains(File.separator) ? fileName
				// .substring(fileName.lastIndexOf(File.separator) + 1) :
				// fileName;
				String name = fileName.replaceAll(File.separator, "_");
				if (md != null) {
					// name = new String(
					byte[] bytes = md.digest(fileName.getBytes());
					name = Ref.fileSansDot(fileName) + "_" + convertToHex(bytes, 6);
				}
				name = Ref.cleanFilename(name);

				String mini = name + Ref.ext_mini;
				String thumb = name + Ref.ext_thumb;
				name = name + Ref.ext_db;

				urlToFile.put(name, fileName);
				urlToFile.put(mini, fileName);
				urlToFile.put(thumb, fileName);

				// Extra / to work around a bug in the displayator app where the
				// JSON part does not handle relative uris
				// correctly.
				String title = rs.getString(Ref.infos_title);
				// title = cleanName(title);
				long width = rs.getLong(Ref.infos_width);
				long height = rs.getLong(Ref.infos_height);
				String description = rs.getString(Ref.infos_description);
				DisplayableInfosForJSON disp = new DisplayableInfosForJSON(title, name, thumb, mini, weight, width, height, description);
				String s = DisplayableInfosForJSON.toJSON(serializer, disp);
				s += ",\n";
				ret += s;

				String urlInfos = "title=" + title;
				urlInfos += "&updated=";
				urlInfos += "&thumb=" + thumb;
				urlInfos += "&preview=" + mini;
				urlInfos += "&weight=" + weight;
				urlInfos += "&width=" + width;
				urlInfos += "&height=" + height;
				urlInfos += "&description=" + description;

				String li = "\t\t\t<li>";
				String li_ = "</li>\n";
				String html = "<div class=\"item\"><table><tr><td><a href=\"" + mini + "\"><img src=\"" + thumb
						+ "\" align=\"right\"/></a></td><td><div class=\"content\"><b>" + title + "</b>";
				// html += "\n\t\t<a href=\"" + mini + "\"><img src=\"" + thumb
				// + "\"></a>\n\t\t";
				html += "<br><a href=\"" + mini + "\">See the miniature</a>";
				html += "<br/>Download and view <a href=\"" + app_handle_item + name + "?" + urlInfos + "\" >original size with displayator</a>";
				html += "  or download <a href=\"" + name + "\" >as a file</a>.<br/>";
				html += "\n\t<ul>\n";
				html += li + "Weight: " + ((float) Math.round(((double) weight) / 10000)) / 100 + " MB." + li_;
				html += li + "Size: " + rs.getLong(Ref.infos_width) + " x " + rs.getLong(Ref.infos_height) + "px." + li_;
				String dsc = rs.getString(Ref.infos_description);
				if (null != dsc && dsc.length() > 0 && !dsc.equalsIgnoreCase("no description")) {
					html += li + "Description: " + dsc + li_;
				}
				html += "</ul>\n";
				html += "</div></td></tr></table></div>\n";
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

	public static List<String> getDisplayableTitles(String absolutePathOfDisplayable) {
		List<String> titles = new ArrayList<String>();
		try {
			Connection mapDB = DriverManager.getConnection("jdbc:sqlite:" + absolutePathOfDisplayable);
			mapDB.setReadOnly(true);
			Statement statement = mapDB.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select " + Ref.infos_title + " from infos");
			while (rs.next()) {
				String name = rs.getString(Ref.infos_title);
				System.out.println("name : " + name);
				titles.add(name);
			}
			if (mapDB != null) mapDB.close();
		} catch (Exception ex) {
			System.err.println("ex for displayable at " + absolutePathOfDisplayable);
			ex.printStackTrace();
		}
		return titles;
	}
}
