/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author niko
 * 
 */
public class MapViewer extends JPanel {
	Connection mapDB;
	public static final int tileSize = 192;

	public static final String getTilesInRange = "select * from tiles_0_0 where x >= ? and x <= ? and y >=? and y <=? and z=?";

	PreparedStatement tilesInRange;

	int maxX = 0;
	int maxY = 0;

	public MapViewer() {
		super();
		try {
			Class.forName("org.sqlite.JDBC");

			mapDB = DriverManager.getConnection("jdbc:sqlite:memory:");
			mapDB.close();
			this.setDoubleBuffered(true);
		} catch (Exception ex) {
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
			String infos0 = "select * from layers_infos where zoom = 0";
			Statement statement = mapDB.createStatement();
			ResultSet rs = statement.executeQuery("select * from " + Ref.layers_infos_table_name + " where zoom=0");
			// layers_infos (layerName STRING, mapKey LONG, zindex LONG, zoom LONG, width LONG,height LONG, tiles_x
			// LONG,tiles_y LONG, offset_x LONG, offset_y LONG)");

			while (rs.next()) {
				int width = rs.getInt("width");
				int height = rs.getInt("height");
				maxX = rs.getInt("tiles_x");
				maxY = rs.getInt("tiles_y");

				this.setSize(width, height);
				this.setMinimumSize(new Dimension(width, height));
				this.setPreferredSize(new Dimension(width, height));
			}

		} catch (Exception ex) {
			System.err.println("ex for map : " + tileSourcePath);
			ex.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
//		System.out.println("paintComponent");
		int zoom = 0;
		Graphics2D g2 = (Graphics2D) g;
		Rectangle r = g2.getClipBounds();
		int tileXa = r.x / tileSize;
		int tileXb = tileXa + r.width / tileSize;
		int tileYa = r.y / tileSize;
		int tileYb = tileYa + r.height / tileSize;
		try {
			int macYb = (maxY -1 - tileYa);
			int macYa = (maxY - 1 - tileYb);

//			System.out.println("getting the tiles x between " + tileXa + " and " + tileXb + " and y between " + (maxY - tileYa) + " and " + (maxY - tileYb) + " .");
			tilesInRange.setInt(1, tileXa);
			tilesInRange.setInt(2, tileXb);
			tilesInRange.setInt(3, macYa);
			tilesInRange.setInt(4, macYb);
			tilesInRange.setInt(5, zoom);
			// BufferedInputStream
			ResultSet rs = tilesInRange.executeQuery();
			while (rs.next()) {
				int x = rs.getInt(1);
				int y = rs.getInt(2);
				int z = rs.getInt(3);
//				System.out.println("found a tile for " + x + " " + y + " " + z);
				byte[] data = rs.getBytes(4);
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
				g2.drawImage(image, x * tileSize, (maxY -1 - y) * tileSize, null);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		g2.dispose();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dir = "/Users/niko/tileSources/mapRepository/";
		String file = "Beijingsubway2008.pdf.mdb";

		MapViewer mV = new MapViewer();
		mV.setTileSource(dir + file);
		JScrollPane p = new JScrollPane(mV);
		JFrame frame = new JFrame("Map Viewer");
		frame.setContentPane(p);
		// frame.setContentPane(new JPanel(new BorderLayout()));
		// frame.getContentPane().add(mV, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
