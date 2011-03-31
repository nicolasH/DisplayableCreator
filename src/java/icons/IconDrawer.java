package icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class IconDrawer extends ImageIcon {

	public class MissingIcon implements Icon {

		private int width = 32;
		private int height = 32;

		private BasicStroke stroke = new BasicStroke(4);

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();

			g2d.setColor(Color.WHITE);
			g2d.fillRect(x + 1, y + 1, width - 2, height - 2);

			g2d.setColor(Color.BLACK);
			g2d.drawRect(x + 1, y + 1, width - 2, height - 2);

			g2d.setColor(Color.RED);

			g2d.setStroke(stroke);
			g2d.drawLine(x + 10, y + 10, x + width - 10, y + height - 10);
			g2d.drawLine(x + 10, y + height - 10, x + width - 10, y + 10);

			g2d.dispose();
		}

		public int getIconWidth() {
			return width;
		}

		public int getIconHeight() {
			return height;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IconDrawer drawer = new IconDrawer();
		JPanel g = new JPanel(new GridLayout(4, 4));
		IconsLoader loader = IconsLoader.getIconsLoader();
		g.add(new JLabel(drawer.new MissingIcon()));
		g.add(new JLabel(loader.ic_edit_16));
		g.add(new JLabel(loader.ic_save_16));
		g.add(new JLabel(loader.ic_zoom_16));
		g.add(new JLabel(loader.ic_zoomIn_24));
		g.add(new JLabel(loader.ic_zoomIn_24));
		g.add(new JLabel(loader.ic_zoomIn_24));
		g.add(new JLabel(loader.ic_zoomIn_24));

		JFrame framce = new JFrame("Icon Drawer");
		framce.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		framce.setContentPane(g);
		framce.pack();
		framce.setVisible(true);

	}
}
