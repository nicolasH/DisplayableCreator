/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		if (fullScreen) {
			frame.setLocation(framePreFullPos);
			frame.setSize(framePreFullDim);
			fullScreen = false;
			button.setIcon(IconsLoader.getIconsLoader().ic_zoomOut_24);
			// toolbar.setFloatable(true);
			return;
		}
		framePreFullDim = frame.getSize();
		framePreFullPos = frame.getLocation();
		button.setIcon(IconsLoader.getIconsLoader().ic_zoomIn_24);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(0, 0);
		frame.setSize(d);
		fullScreen = true;
	}
}
