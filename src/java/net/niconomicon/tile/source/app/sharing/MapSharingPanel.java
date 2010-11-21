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
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.viewer.ImageTileSetViewer;

/**
 * @author niko
 * 
 */
public class MapSharingPanel extends JPanel implements TableModelListener {

	boolean currentlySharing = false;
	SharingManager sharingManager;
	CheckBoxMapTable mapList;
	JSpinner portNumber;
	JLabel sharingStatus;
	String rootDir = "/Users/niko/Sites/testApp/mapRepository";

	ImageTileSetViewer viewer;

	/**
	 * stand alone main
	 * 
	 */
	public static void main(String[] args) {
		MapSharingPanel service = new MapSharingPanel(null);
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

	public MapSharingPanel(ImageTileSetViewer viewer) {
		this.viewer = viewer;
		init();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		if (sharingManager.isSharing()) {
			sharingManager.setSharingList(mapList.getSelectedMapFiles());
			// update the list of shared documents
		} else {
			// don't care ;-)
			return;
		}
	}

	// { String tileSourceLocation = mapList.getFileLocation(arg0.getFirstIndex());
	// System.out.println("Setting the tile source location to " + tileSourceLocation);
	// viewer.setTileSet(tileSourceLocation);}
	public void init() {
		sharingManager = new SharingManager();
		mapList = new CheckBoxMapTable(viewer);
		mapList.getModel().addTableModelListener(this);
		mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// mapList.getSelectionModel().addListSelectionListener(this);
		this.setLayout(new BorderLayout());

		// shared files
		// //////////////////////////////////////////
		this.add(new JScrollPane(mapList), BorderLayout.CENTER);
		JPanel options = new JPanel(new GridLayout(0, 1));
		// //////////////////////////////////////////
		// port number
		JPanel p = new JPanel(new GridLayout(0, 2));
		p.add(new JLabel("Map sharing port : "));
		portNumber = new JSpinner(new SpinnerNumberModel(Ref.sharing_port, 1025, 65536, 1));
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
		String[] children = Ref.getDBFiles(rootDir);
		Map<String, String> mapsMap = getMapList(rootDir, children);
		mapList.setData(mapsMap);
		if (sharingManager.isSharing()) {
			sharingManager.setSharingList(mapList.getSelectedMapFiles());
		}
	}

	public void startSharing() {
		// HashSet<String> sharedDB = new HashSet<String>();
		Collection<String> sharedMaps = mapList.getSelectedMapFiles();
		System.out.println("should start sharing the maps");
		// generate the xml;
		try {
			sharingManager.setPort(((SpinnerNumberModel) portNumber.getModel()).getNumber().intValue());
			sharingManager.setSharingList(sharedMaps);
			sharingManager.startSharing();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stopSharing() {
		try {
			sharingManager.stopSharing();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
