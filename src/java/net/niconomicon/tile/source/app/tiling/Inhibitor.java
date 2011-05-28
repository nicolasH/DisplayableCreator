/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;

/**
 * @author Nicolas Hoibian
 * This class is used to stop the creation of a displayable on user request, as it is a lengthy process.
 */
public class Inhibitor implements ActionListener {

	boolean shouldStillRun = true;
	JProgressBar progressBar;

	public Inhibitor(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void requestRunInhibition() {
		if (null != progressBar) {
			this.progressBar.setString("Cancelling ...");
			this.progressBar.setIndeterminate(true);
		}
		shouldStillRun = false;
	}

	public boolean hasRunInhibitionBeenRequested() {
		return !shouldStillRun;
	}

	public void reset() {
		shouldStillRun = true;
	}

	public void actionPerformed(ActionEvent e) {
		requestRunInhibition();
	}

}
