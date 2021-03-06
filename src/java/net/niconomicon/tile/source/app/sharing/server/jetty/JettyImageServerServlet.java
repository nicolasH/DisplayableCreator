/**
 * 
 */
package net.niconomicon.tile.source.app.sharing.server.jetty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.sharing.server.DisplayableSharingServiceAnnouncer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Nicolas Hoibian The servlet that is given to the Jetty Server
 *         instance to serve the Displayables, their thumbnails and miniatures,
 *         as well as the JSON and HTML description of the shared Displayables.
 */
public class JettyImageServerServlet extends HttpServlet {

	Map<String, String> imaginaryMap;
	Set<String> knownImages;

	public JettyImageServerServlet() {
		knownImages = new HashSet<String>();
		// System.out.println("CSS : " + cssContent);

	}

	public void setSharedDisplayables(Collection<String> documents) {
		knownImages.clear();
		knownImages.addAll(documents);

		Map<String, String> refs = Ref.generateIndexFromFileNames(knownImages);
		// for caching
		Ref.extractThumbsAndMiniToTmpFile(refs);
		imaginaryMap = refs;
	}

	public Map<String, String> getMappings() {
		return imaginaryMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String request = req.getRequestURI();
		// System.out.println("URI : " + request);
		request = URLDecoder.decode(request, "UTF-8");
		// System.out.println("Decoded URI : " + request);
		String key = request;
		// to work around a bug in the displayator app where the JSON part does
		// not handle relative uris correctly.
		if (key.startsWith("//")) {
			key = key.substring(1);
		}
		if (key.startsWith("/")) {
			key = key.substring(1);
		}
		// if (imaginaryMap.containsKey(key)){//.substring(1))) {
		// key = key.substring(1);
		// }
		// if (key.equals(Ref.sharing_cssRef)) {
		// try {
		// sendRessource(Ref.sharing_cssLocation, resp);
		// return;
		// } catch (Exception ex) {
		// resp.sendError(500,
		// "The server encountered an error while trying to send the content for request ["
		// + request + "]");
		// return;
		// }
		// }

		if (key.equals("")) {
			key = Ref.sharing_htmlRef;
		}
		if (imaginaryMap.containsKey(key)) {
			String val = imaginaryMap.get(key);
			System.out.println("["+key+"]{"+val+"}");
			try {
				File f = new File(val);
				if (f.exists()) {
					sendFile(f, resp);
					return;
				} else {
					sendString(val, resp);
					return;
				}
			} catch (Exception ex) {
				resp.sendError(500, "The server encountered an error while trying to send the content for request [" + request + "]");
				return;
			}
		}
		System.out.println("Could not find ["+key+"] amongst:");
		for (String k : imaginaryMap.keySet()){
			System.out.println("{"+k+"}");
		}
		
		resp.sendError(404, "The server could not find or get access to [" + request + "]");
		if (true) {
			System.out.println("Sending 404 for " + request);
			return;
		}
		if (request.equals("/" + Ref.sharing_jsonRef) || request.equals(Ref.URI_jsonRef)) {
			String k = "/" + Ref.sharing_jsonRef;
			// System.out.println("should be returning the Displayable Feed [" +
			// imaginaryMap.get(k).length() + "]");
			try {
				sendString(imaginaryMap.get(k), resp);
				return;
			} catch (Exception ex) {
				resp.sendError(500, "The server encountered an error while trying to send the content for request [" + request + "]");
				return;
			}
		}

		if (request.equals("/") || request.equals(Ref.URI_htmlRef)) {
			request = Ref.URI_htmlRef;
			// System.out.println("should be returning the html list [" +
			// imaginaryMap.get(request).length() + "]");
			try {
				String resolvedAddressItem = Ref.app_handle_item + req.getScheme() + "://" + req.getLocalAddr() + ":" + req.getLocalPort();
				String resolvedAddressList = Ref.app_handle_list + req.getScheme() + "://" + req.getLocalAddr() + ":" + req.getLocalPort();
				System.out.println("resolved Address item : " + resolvedAddressItem);
				System.out.println("resolved Address list : " + resolvedAddressList);
				String htmlListing = imaginaryMap.get(request).replaceAll(Ref.app_handle_item, resolvedAddressItem);
				htmlListing = htmlListing.replaceAll(Ref.app_handle_list, resolvedAddressList);
				sendString(htmlListing, resp);
				return;
			} catch (Exception ex) {
				resp.sendError(500, "The server encountered an error while trying to send the requested content for request [" + request + "]");
				return;
			}
		}

		if (request.startsWith("/")) {
			request = request.substring(1);
		}
		if (null == imaginaryMap || !imaginaryMap.containsKey(request) || imaginaryMap.get(request) == null) {
			resp.sendError(404, "The server could not find or get access to [" + request + "]");
			System.out.println("404");
			return;
		}

		String string = imaginaryMap.get(request);
		System.out.println("String from the imaginary map : [" + string + "]");
		File f = new File(string);
		if (f.exists()) {
			try {
				sendFile(f, resp);
			} catch (Exception ex) {
				resp.sendError(500, "The server encountered an error while trying to send the requested file [ " + f.getName() + "] for request ["
						+ request + "]");
				return;
			}
		} else {
			resp.sendError(404, "The server could not find or get access to [" + f.getName() + "]");
			return;
		}
	}

	public static void sendString(String s, HttpServletResponse response) throws Exception {
		byte[] bytes = s.getBytes();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentLength(bytes.length);
		response.getOutputStream().write(bytes);
		response.flushBuffer();
	}

	public static void sendFile(File f, HttpServletResponse response) throws Exception {
		long len = f.length();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentLength((int) len);
		int bufferSize = response.getBufferSize();
		byte[] buff = new byte[bufferSize];
		InputStream in;
		in = new FileInputStream(f);
		int nread;
		while ((nread = in.read(buff)) > 0) {
			response.getOutputStream().write(buff, 0, nread);
		}
		in.close();
		response.getOutputStream().flush();
	}

	public void sendRessource(String location, HttpServletResponse response) throws Exception {
		URL url = this.getClass().getClassLoader().getResource(location);
		String toSend;
		try {
			BufferedReader dis = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
			String buffer;
			StringBuffer sb = new StringBuffer();
			while ((buffer = dis.readLine()) != null) {
				sb.append(buffer);
			}
			toSend = sb.toString();
			sendString(toSend, response);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		int port = 8889;

		JettyImageServerServlet service = new JettyImageServerServlet();
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(new ServletHolder(service), "/*");
		Collection<String> sharedDisplayables = new ArrayList<String>();
		String base = "/Users/niko/TileSources/displayables/";
		String[] disps = new String[] { "tpg-plan-centre-9-decembre-12-4-1.disp", "tpg-plan-peripherique-9-decembre-12-2.disp",
				"tpg-plan-schematique-9-decembre-12-3.disp" };
		for (String d : disps) {
			sharedDisplayables.add(base + d);
		}
		service.setSharedDisplayables(sharedDisplayables);
		Server server = new Server(port);
		server.setHandler(context);
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
}
