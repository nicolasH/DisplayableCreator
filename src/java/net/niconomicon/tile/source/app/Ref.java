/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.io.File;
import java.io.FileOutputStream;
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

	public static int sharing_port = 8080;
	public static final String sharing_serviceName = "MapSharingService";

	public static final String sharing_xmlRef = "maps.xml";
	public static final String sharing_htmlRef = "maps.html";

	public static final String ext_db = ".mdb";
	public static final String ext_thumb = ".thumb";
	public static final String ext_mini =  ".mini";

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

	
	public static void extractThumbsAndMiniToTmpFile(Map<String, String> maps) {
		for (String key : maps.keySet()) {
			String file = maps.get(key);
			System.out.println("Extracting for Key : ["+key+"]");// + " value : "+ maps.get(key));
			if (key.endsWith(Ref.ext_db) || key.endsWith(Ref.sharing_xmlRef) || key.endsWith(Ref.sharing_htmlRef)) {
				continue;
			}
			System.out.println("Really trying to open (k=["+key + "]) => "+file);
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
					System.out.println("Wrote "+key+" into " + temp.getAbsolutePath());
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
	public static Map<String, String> generateXMLFromMapFileNames(Collection<String> maps) {
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
				System.out.println("trying to open the map :" + mapFileName+ " To generate the XML.");
				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + mapFileName);
				connection.setReadOnly(true);
				xml.append(generateXMLForConnection(connection, size, mapFileName, urlToFile));
				html.append(generateHTMLForConnection(connection,size,mapFileName));
				if (connection != null) connection.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		xml.append("</maps>");
		urlToFile.put("/" + sharing_xmlRef, xml.toString());
		
		html.append("</body></html>");
		urlToFile.put("/" + sharing_htmlRef, html.toString());
//		System.out.println(html.toString());
		return urlToFile;
	}

	public static String generateXMLForConnection(Connection mapDB, long weight, String fileName, Map<String, String> urlToFile) {
		String ret = "";
		try {
			// create a database connection

			Statement statement = mapDB.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select * from infos");
			while (rs.next()) {
				// read the result set
				String name = fileName.contains(File.separator)? "/"+fileName.substring(fileName.lastIndexOf(File.separator)+1):fileName;
				name = name.replace(' ', '_');
				String mini = name + Ref.ext_mini;
				String thumb = name + Ref.ext_thumb;
				//name = name + Ref.ext_db;
				
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
				s += "<description>" + rs.getString(Ref.infos_description)+"</description>\n\t";
				s += "</map>\n";
				ret += s;
			}
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}

		// /////////////////////////////

		return ret;
	}
	public static String generateHTMLForConnection(Connection mapDB, long weight, String fileName) {
		String ret ="";
		try {
			// create a database connection

			Statement statement = mapDB.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select * from infos");
			while (rs.next()) {
				// read the result set
				String name = fileName.contains(File.separator)? "/"+fileName.substring(fileName.lastIndexOf(File.separator)+1):fileName;
				name = name.replace(' ', '_');
				String mini = name + Ref.ext_mini;
				String thumb = name + Ref.ext_thumb;
				//name = name + Ref.ext_db;
	
				String s = "<div><b>" + rs.getString(Ref.infos_title) + "</title>\n\t";
				s += "<a href=\"" + name + "\" >"+name+"</a>\n\t";
				s += "<a href=\"" + thumb + "\" ><img src=\""+thumb+"\"></a>\n\t";
				s += "<a href=\"" + mini + "\" ><img src=\""+mini+"\"></a>\n\t";
				s += "size : " + weight;
				s += "bytes.  width : " + rs.getLong(Ref.infos_width);
				s += "pixels height : " + rs.getLong(Ref.infos_height);
				s += "pixels.<br/> description:" + rs.getString(Ref.infos_description)+"\n\t";
				s += "</div>\n";
				ret += s;
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
