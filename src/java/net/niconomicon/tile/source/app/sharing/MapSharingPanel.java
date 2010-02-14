/**
 * 
 */
package net.niconomicon.tile.source.app.sharing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author niko
 * 
 */
public class MapSharingPanel extends JPanel {

	boolean currentlySharing = false;
	SharingManager sharingManager;
	CheckBoxMapTable mapList;
	JTextField portNumber;
	JLabel sharingStatus;
	String rootDir = "/Users/niko/Sites/testApp/mapRepository";

	/**
	 * stand alone main
	 * 
	 */
	public static void main(String[] args) {
		MapSharingPanel service = new MapSharingPanel();
		JFrame frame = new JFrame("Map Sharing Service");
		frame.setContentPane(service);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				((MapSharingPanel) ((JFrame) e.getSource()).getContentPane()).stopSharing();
				super.windowClosing(e);
			}
		});
		frame.setVisible(true);
	}

	public MapSharingPanel() {
		init();
	}

	public void init() {
		sharingManager = new SharingManager();
		mapList = new CheckBoxMapTable();

		this.setLayout(new BorderLayout());

		// shared files
		// //////////////////////////////////////////
		this.add(new JScrollPane(mapList), BorderLayout.CENTER);
		JPanel options = new JPanel(new GridLayout(0, 1));
		// //////////////////////////////////////////
		// port number
		JPanel p = new JPanel(new GridLayout(0, 2));
		p.add(new JLabel("Map sharing port : "));
		portNumber = new JTextField("" + Ref.sharing_port, 6);
		p.add(portNumber);
		options.add(p);
		// start sharing
		sharingStatus = new JLabel("Map Sharing status : [not running]");
		options.add(sharingStatus);
		JButton shareButton = new JButton("Start map sharing");
		shareButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentlySharing = !currentlySharing;
				JButton b = (JButton) e.getSource();
				if (currentlySharing) {
					sharingStatus.setText("Map Sharing status : [starting ...]");
					startSharing();
					sharingStatus.setText("Map Sharing status : [running]");
					b.setText("Stop map sharing");
				} else {
					sharingStatus.setText("Map Sharing status : [stopping ...]");
					stopSharing();
					sharingStatus.setText("Map Sharing status : [not running]");
					b.setText("Start mapsharing");
				}
			}
		});
		options.add(shareButton);
		this.add(options, BorderLayout.SOUTH);
	}

	public void setRootDir(String rDir) {
		if (rDir != null) {
			this.rootDir = rDir;
		}
		File dir = new File(rootDir);
		String[] children = dir.list();

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(Ref.ext_db);
			}
		};
		children = dir.list(filter);

		Map<String, String> mapsMap = getMapList(rootDir, children);
		mapList.setData(mapsMap);
		sharingManager.setSharingList(mapList.getSelectedMapFiles());
	}

	public void startSharing() {
		// HashSet<String> sharedDB = new HashSet<String>();
		Collection<String> sharedMaps = mapList.getSelectedMapFiles();
		System.out.println("should start sharing the maps");
		// generate the xml;
		sharingManager.setSharingList(sharedMaps);
		sharingManager.startSharing();
		System.out.println("shared maps :");
		// System.out.println(mapFeed);
	}

	public void stopSharing() {
		sharingManager.stopSharing();
	}

	public Map<String, String> getMapList(String rootDir, String[] maps) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Map<String, String> fileToName = new HashMap<String, String>();
		if (!rootDir.endsWith(File.separator)) {
			rootDir += File.separator;
		}
		for (String string : maps) {
			try {
				String fileName = rootDir + string;
				System.out.println("trying to open the map : " + fileName);
				Connection mapDB = DriverManager.getConnection("jdbc:sqlite:" + fileName);
				mapDB.setReadOnly(true);
				Statement statement = mapDB.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.
				ResultSet rs = statement.executeQuery("select " + Ref.infos_title + " from infos");
				while (rs.next()) {
					String name = rs.getString(Ref.infos_title);
					System.out.println("name : " + name);
					fileToName.put(fileName, name);
				}
				if (mapDB != null) mapDB.close();
			} catch (Exception ex) {
				System.err.println("ex for map : " + string);
				ex.printStackTrace();
			}
		}
		return fileToName;
	}
}
