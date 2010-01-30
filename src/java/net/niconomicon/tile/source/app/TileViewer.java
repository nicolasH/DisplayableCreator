/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * @author niko
 * 
 */
public class TileViewer {

	JSpinner spinXa;
	JSpinner spinXb;
	JSpinner spinYa;
	JSpinner spinYb;
	JComboBox spinZoom;

	JPanel topRow;
	JPanel tileViewer;

	Connection mapDB;
	public static final int tileSize = 192;

	public static final String getTilesInRange = "select * from tiles_0_0 where x >= ? and x <= ? and y >=? and y <=? and z=?";

	PreparedStatement tilesInRange;

	public Map<String, ImageIcon> cache;
	int maxX = 0;
	int maxY = 0;

	// int zoom = 0;

	public TileViewer() {
		try {
			Class.forName("org.sqlite.JDBC");
			// System.out.println("Loaded the sqliteJDBC Driver class ?");
			// mapDB = DriverManager.getConnection("jdbc:sqlite:memory");
			// mapDB.close();
			// this.setDoubleBuffered(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		setupComponents();
	}

	public void setupComponents() {
		spinXa = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
		spinYa = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
		spinXb = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
		spinYb = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
		spinZoom = new JComboBox(new DefaultComboBoxModel());
		topRow = new JPanel(new GridLayout(1, 0));
		topRow.add(new JLabel("Zoom : "));
		topRow.add(spinZoom);
		topRow.add(new JLabel("Xa"));
		topRow.add(spinXa);
		topRow.add(new JLabel("Ya"));
		topRow.add(spinYa);
		topRow.add(new JLabel("Xb"));
		topRow.add(spinXb);
		topRow.add(new JLabel("Yb"));
		topRow.add(spinYb);

		JButton repaint = new JButton("Repaint");
		repaint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaintTiles();
			}
		});
		topRow.add(repaint);
		tileViewer = new JPanel(new GridLayout(0, 4));
		tileViewer.add(new JLabel("Please choose the bloody tiles you want to see"));
	}

	public void repaintTiles() {
		int xA = ((Integer) spinXa.getValue()).intValue();
		int xB = ((Integer) spinXb.getValue()).intValue();
		int yA = ((Integer) spinYa.getValue()).intValue();
		int yB = ((Integer) spinYb.getValue()).intValue();
		tileViewer.removeAll();
		int zoom = spinZoom.getSelectedIndex();
		for (int x = xA; x < xB; x++) {
			for (int y = yA; y < yB; y++) {
				ImageIcon ic = cache.get(x + "_" + y + "_" + zoom);
				JLabel l = new JLabel("x=" + x + "y=" + y, ic, SwingConstants.RIGHT);
				System.out.println(l.getText());
				tileViewer.add(l);
			}
		}
	}

	public void setupSpinners(int zoom) {
		try {
			Statement statement = mapDB.createStatement();
			ResultSet rs = statement.executeQuery("select * from " + Ref.layers_infos_table_name + " where zoom=" + zoom);

			while (rs.next()) {
				// int width = rs.getInt("width");
				// int height = rs.getInt("height");
				maxX = rs.getInt("tiles_x");
				maxY = rs.getInt("tiles_y");

				SpinnerNumberModel model = (SpinnerNumberModel) spinXa.getModel();
				model.setMaximum(new Integer(maxX));

				model = (SpinnerNumberModel) spinXb.getModel();
				model.setMaximum(new Integer(maxX));
				model.setValue(new Integer(maxX));

				model = (SpinnerNumberModel) spinYa.getModel();
				model.setMaximum(new Integer(maxY));
				model = (SpinnerNumberModel) spinYb.getModel();
				model.setMaximum(new Integer(maxY));
				model.setValue(new Integer(maxY));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void setTileSource(String tileSourcePath) {
		try {
			System.out.println("trying to open the map : " + tileSourcePath);
			mapDB = DriverManager.getConnection("jdbc:sqlite:" + tileSourcePath);
			mapDB.setReadOnly(true);
			tilesInRange = mapDB.prepareStatement(getTilesInRange);
			tilesInRange.clearParameters();
			Statement statement = mapDB.createStatement();
			ResultSet rs = statement.executeQuery("select * from " + Ref.layers_infos_table_name + " order by zoom");

			DefaultComboBoxModel mod = (DefaultComboBoxModel) spinZoom.getModel();
			while (rs.next()) {
				int z = rs.getInt("zoom");
				mod.insertElementAt(new Integer(z), z);
			}

			setupSpinners(0);

			cache = new HashMap<String, ImageIcon>();
			setupCacheForTiles(0);

		} catch (Exception ex) {
			System.err.println("ex for map : " + tileSourcePath);
			ex.printStackTrace();
		}
	}

	public void setupCacheForTiles(int zoom) throws Exception {
		System.out.println("setting up the cache....");
		tilesInRange.setInt(1, 0);
		tilesInRange.setInt(2, maxX);
		tilesInRange.setInt(3, 0);
		tilesInRange.setInt(4, maxY + 1);
		tilesInRange.setInt(5, zoom);
		// BufferedInputStream
		ResultSet rs = tilesInRange.executeQuery();
		while (rs.next()) {
			int x = rs.getInt(1);
			int y = rs.getInt(2);
			int z = rs.getInt(3);
			// System.out.println("found a tile for " + x + " " + y + " " + z);
			byte[] data = rs.getBytes(4);
			ImageIcon ic = new ImageIcon(data);
			cache.put(x + "_" + y + "_" + z, ic);
		}
		System.out.println("cache is set up");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JPanel ctPane = new JPanel(new BorderLayout());
		TileViewer viewer = new TileViewer();
		String file = "test.mdb";
		String dir = "/Users/niko/tileSources/";
		// file = "globcover_MOSAIC_H.png.mdb";
		viewer.setTileSource(file);

		ctPane.add(viewer.topRow, BorderLayout.NORTH);
		ctPane.add(viewer.tileViewer, BorderLayout.CENTER);
		JFrame frame = new JFrame("Tile viewer");
		frame.setContentPane(ctPane);
		frame.pack();
		frame.setVisible(true);
	}

}
