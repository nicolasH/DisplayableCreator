/**
 * 
 */
package net.niconomicon.tile.source.app.input;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.sharing.DisplayableSharingPanel;

/**
 * @author Nicolas Hoibian
 * 
 */
public class DragAndDropManager {

	Queue<File> images = new LinkedList<File>();
	Queue<File> displayables = new LinkedList<File>();
	DisplayableSharingPanel pDisp;
	DisplayableCreatorInputPanel pCrea;

	// Thread threadImages;
	// Thread threadDisp;
	// DisplayableProcessor processorDisp;
	// ImageProcessor processorImages;

	public DragAndDropManager(DisplayableSharingPanel pDisp,
			DisplayableCreatorInputPanel pCrea) {
		this.pDisp = pDisp;
		this.pCrea = pCrea;
		System.out.println("disp:" + pDisp + " - crea " + pCrea);
		Thread threadDisp = new Thread(new DisplayableProcessor(displayables));
		Thread threadImages = new Thread(new ImageProcessor(images));

		threadDisp.start();
		threadImages.start();
	}

	public void addImageToTile(File f) {
		synchronized (images) {
			images.add(f);
		}
	}

	public void wakeProcessors() {
		synchronized (images) {
			images.notify();
		}
		synchronized (displayables) {
			displayables.notify();
		}
	}

	public void addDisplayable(File f) {
		synchronized (displayables) {
			displayables.add(f);
		}
	}

	/*
	 * The rest is some smal threads and runnable
	 */

	private class DisplayableProcessor extends FileProcessor {
		public DisplayableProcessor(Queue queue) {
			super(queue);
		}

		public void processFile(File f) {
			System.out.println("processing " + f);
			List<String> titles = Ref.getDisplayableTitles(f.getAbsolutePath());
			if (titles.size() > 0) {
				SwingUtilities.invokeLater(new addDisplayableToGUI(titles
						.get(0), f.getAbsolutePath()));
			}
		}
	}

	private class ImageProcessor extends FileProcessor {
		public ImageProcessor(Queue queue) {
			super(queue);
		}

		public void processFile(File file) {
			SwingUtilities.invokeLater(new addImageToDisplayableCreationQueue(
					file));
		}
	}

	private class addImageToDisplayableCreationQueue implements Runnable {
		File file;

		public addImageToDisplayableCreationQueue(File f) {
			this.file = f;
		}

		public void run() {
			pCrea.queueImageForDisplayableCreation(file);
		}
	}

	private class addDisplayableToGUI implements Runnable {
		String title, path;

		public addDisplayableToGUI(String title, String path) {
			this.title = title;
			this.path = path;
		}

		public void run() {
			pDisp.addDisplayableToShare(path, title);
		}
	}

	private abstract class FileProcessor implements Runnable {
		Queue queue;

		public FileProcessor(Queue l) {
			queue = l;
		}

		public void run() {
			while (true) {
				synchronized (queue) {
					List tmpList = new ArrayList();
					boolean hasElements = queue.size() > 0;
					while (hasElements) {
						File f = (File) queue.poll();
						if (f != null) {
							processFile(f);
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

		public abstract void processFile(File f);
	}
}
