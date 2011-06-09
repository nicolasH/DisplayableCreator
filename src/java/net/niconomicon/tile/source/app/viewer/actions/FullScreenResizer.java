/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import net.niconomicon.tile.source.app.viewer.icons.IconsLoader;

/**
 * @author Nicolas Hoibian
 * 
 */
public class FullScreenResizer implements ActionListener {
	Dimension framePreFullDim;
	Point framePreFullPos;
	boolean fullScreen = false;
	JFrame frame;
	JButton button;
	JToolBar toolbar;

	public FullScreenResizer(JFrame frame, JButton button, JToolBar toolBar) {
		this.frame = frame;
		this.button = button;
		this.toolbar = toolBar;
	}

	public void actionPerformed(ActionEvent e) {
		ImageIcon icon;
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Point pos = new Point(0, 0);
String tooltip="";
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		if (fullScreen) {
			fullScreen = false;
			icon = IconsLoader.getIconsLoader().ic_wExp_24;
			tooltip = "Make the Displayable view fullscreen";
			pos = framePreFullPos;
			dim = framePreFullDim;

		} else {
			fullScreen = true;
			icon = IconsLoader.getIconsLoader().ic_wCon_24;
			tooltip = "Return the Displayable view to its previous size";
			framePreFullDim = frame.getSize();
			framePreFullPos = frame.getLocation();
		}

		frame.dispose();
		// // Can only be changed while not displayable ;-)
		// // but cause problems in mac os x : window appear bellow the toolbar.
		frame.setUndecorated(fullScreen);
		frame.pack();
		frame.setVisible(false);
		button.setIcon(icon);
		button.setToolTipText(tooltip);
		// Can only affect the window if it is displayable
		frame.setSize(dim);
		frame.setLocation(pos);

		frame.setVisible(true);

		if (fullScreen) {
			for (GraphicsDevice gd : gs) {
				for (GraphicsConfiguration conf : gd.getConfigurations()) {
					if (conf.getBounds().intersects(frame.getBounds())) {
						gd.setFullScreenWindow(frame);
						break;
					}
				}
			}
		} else {
			for (GraphicsDevice gd : gs) {
				if (gd.getFullScreenWindow() != null) {
					gd.setFullScreenWindow(null);
				}
			}
		}
	}
}
