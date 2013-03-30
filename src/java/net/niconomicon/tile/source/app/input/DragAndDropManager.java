/**
 * 
 */
package net.niconomicon.tile.source.app.input;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.SwingUtilities;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DragAndDropManager {

	Queue<File> files = new LinkedList<File>();
	DisplayableCreatorInputPanel pCrea;

	public DragAndDropManager(DisplayableCreatorInputPanel pCrea) {
		this.pCrea = pCrea;
		//System.out.println("Crea " + pCrea);

		Thread threadImages = new Thread(new FileProcessor(files));

		threadImages.start();
	}

	public void addFile(File f) {
		synchronized (files) {
			files.add(f);
		}
	}

	public void wakeProcessors() {
		synchronized (files) {
			files.notify();
		}
	}

	private class addFileToQueue implements Runnable {
		File file;

		public addFileToQueue(File f) {
			this.file = f;
		}

		public void run() {
			pCrea.addFile(file);
		}
	}

	private class FileProcessor implements Runnable {
		Queue queue;

		public FileProcessor(Queue l) {
			queue = l;
		}

		public void run() {
			while (true) {
				synchronized (queue) {
					boolean hasElements = queue.size() > 0;
					while (hasElements) {
						File f = (File) queue.poll();
						if (f != null) {
							SwingUtilities.invokeLater(new addFileToQueue(f));
						} else {
							hasElements = false;
						}
					}
					try {
						queue.wait();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}
}
