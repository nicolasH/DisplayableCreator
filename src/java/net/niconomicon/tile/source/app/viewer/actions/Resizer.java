/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.actions;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

/**
 * @author Nicolas Hoibian
 * 
 */
public class Resizer implements ActionListener {
	JFrame frame;
	Dimension dim;

	public Resizer(JFrame frame, Dimension dim) {
		this.dim = dim;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent arg0) {
		frame.setSize(dim);
	}
}
