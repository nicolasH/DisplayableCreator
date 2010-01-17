/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.SQliteTileCreator;

/**
 * @author niko
 * 
 */
public class WebRequestHandler implements Runnable {
	private static final Pattern REQUEST_PATTERN = Pattern.compile("^GET (/.*) HTTP/1.[01]$");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	// private final File documentDir;
	Map<String, String> imaginaryMap;
	private final Socket socket;
	private final boolean log;

	public WebRequestHandler(Socket socket, Map<String, String> nameToSourceFile, boolean log) {
		this.socket = socket;
		this.imaginaryMap = nameToSourceFile;
		this.log = log;
	}

	private String readRequestPath() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		String firstLine = reader.readLine();
		if (firstLine == null) { return null; }
		Matcher matcher = REQUEST_PATTERN.matcher(firstLine);
		return matcher.matches() ? matcher.group(1) : null;
	}

	private OutputStream sendResponseHeaders(int status, String message, long len) throws IOException {
		StringBuffer response = new StringBuffer();
		response.append("HTTP/1.0 ");
		response.append(status).append(' ').append(message).append("\r\n");
		response.append("Content-Length: ").append(len).append("\r\n\r\n");
		OutputStream out = this.socket.getOutputStream();
		out.write(response.toString().getBytes());
		out.flush();
		return out;
	}

	private int sendErrorResponse(int status, String message) throws IOException {
		OutputStream out = sendResponseHeaders(status, message, message.length());
		out.write(message.getBytes());
		out.flush();
		return status;
	}

	private long sendFile(File file) throws IOException {
		long len = file.length();
		OutputStream out = sendResponseHeaders(200, "OK", len);
		InputStream in = new FileInputStream(file);
		try {
			byte[] buffer = new byte[1024];
			int nread = 0;
			while ((nread = in.read(buffer)) > 0) {
				out.write(buffer, 0, nread);
			}
		} finally {
			in.close();
		}
		out.flush();
		return len;
	}

	private long sendBytes(byte[] bytes) throws IOException {
		long len = bytes.length;
		OutputStream out = sendResponseHeaders(200, "OK", len);
		// InputStream in = new FileInputStream(file);
		out.write(bytes);
		out.flush();
		return len;
	}

	// this is the main entry point into this handler
	public void run() {
		// initialize logging information
		long time = System.currentTimeMillis();
		int status = 200;
		long len = 0;
		String host = this.socket.getInetAddress().getHostName();
		String path = null;
		// handle request
		try {
			path = readRequestPath();
			System.out.println("Request path : [" + path + "] ");
			if (path == null) {
				status = sendErrorResponse(400, "Bad Request");
			} else {
				System.out.println("handlable requests : " + Arrays.toString(imaginaryMap.keySet().toArray()));
				if (!imaginaryMap.containsKey(path)) {
					status = sendErrorResponse(404, "Not Found");
				} else {
					len = sendContent(path);
				}
			}
		} catch (IOException e) {
			System.err.println("Error while serving request for [" + path + "] from [" + host + "]: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				this.socket.close();
			} catch (IOException e) {
				System.err.println("Error while closing socket to " + host + ": " + e.getMessage());
			}
		}
		if (this.log) {
			StringBuffer sb = new StringBuffer();
			sb.append(DATE_FORMAT.format(new Date(time))).append(' ');
			sb.append(host).append(' ');
			sb.append(path == null ? "" : path).append(' ');
			sb.append(status).append(' ');
			sb.append(len).append(' ');
			sb.append(System.currentTimeMillis() - time);
			System.out.println(sb);
		}
	}

	public long sendContent(String request) throws IOException {
		long len = 0;
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String string = imaginaryMap.get(request);
		if (request.compareTo("/" + Ref.sharing_xmlRef) == 0) {
			System.out.println("should be returning the mapFeed [" + imaginaryMap.get(request).length() + "]");
			return sendBytes(imaginaryMap.get(request).getBytes());
		}
		File f = new File(string);
		System.out.println("String from the imaginary map : [" + string + "]");
		if (request.endsWith(Ref.ext_db)) { return sendFile(f); }
		try {
			System.out.println("trying to open the map :" + string);
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + string);
			connection.setReadOnly(true);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			if (request.endsWith(Ref.ext_mini)) {
				ResultSet rs = statement.executeQuery("select " + Ref.infos_miniature + " from infos");
				while (rs.next()) {
					len = sendBytes(rs.getBytes(Ref.infos_miniature));
					break;
				}
			}
			if (request.endsWith(Ref.ext_thumb)) {
				ResultSet rs = statement.executeQuery("select " + Ref.infos_thumb + " from infos");
				while (rs.next()) {
					len = sendBytes(rs.getBytes(Ref.infos_thumb));
					break;
				}
			}
			if (connection != null) connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return len;
	}
}
