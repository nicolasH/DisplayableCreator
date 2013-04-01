package net.niconomicon.tile.source.app.input;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.niconomicon.tile.source.app.DisplayablesSource;
import net.niconomicon.tile.source.app.fonts.FontLoader;
import net.niconomicon.tile.source.app.sharing.exporter.DirectoryExporter;
import net.niconomicon.tile.source.app.viewer.DisplayableViewer;

public class QueueListView extends JPanel implements DisplayablesSource {

	Queue<QueueListItem> itemsToTransformQueue;
	List<QueueListItem> allItemsList;
	Object displayablesLock;
	DisplayableViewer viewer;
	SaveDialog saveDialog;
	JButton lastItem;

	// JLabel lastItem;

	public QueueListView(DisplayableViewer viewer) {
		super();
		this.viewer = viewer;
		init();
	}

	public DisplayableViewer getViewer() {
		return viewer;
	}

	public SaveDialog getSaver() {
		return saveDialog;
	}

	private void init() {
		itemsToTransformQueue = new ConcurrentLinkedQueue<QueueListItem>();
		allItemsList = new Vector<QueueListItem>();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//this.setLayout(new GridBagLayout());
		
		displayablesLock = new Object();
		saveDialog = new SaveDialog();
		this.setSize(QueueListItem.minWidth, 400);
		this.setMinimumSize(new Dimension(QueueListItem.minWidth, QueueListItem.minHeight));
		
		lastItem = FontLoader.getButtonFlatSmall(FontLoader.iconDash +" " +  FontLoader.iconExport+" "+FontLoader.iconDash);
		lastItem.setToolTipText("Export the Displayables as a directory with HTML & JSON indexes");
		lastItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DirectoryExporter.showDialog(lastItem, getDisplayables());
			}
		});
		lastItem.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
		lastItem.setMinimumSize(new Dimension(350, 40));
		lastItem.setPreferredSize(new Dimension(350, 40));
		
		this.add(lastItem, getConstraintForY(0));
	}

	private void makeLastItemLast() {
		this.remove(lastItem);
		this.add(lastItem, getConstraintForY(allItemsList.size()));
	}

	/**
	 * This is designed that way so that any intermediary need not know of the
	 * difference between an image and a displayable.
	 **/
	public void addItem(QueueListItem item) {
		switch (item.status) {
		case QUEUED:
			addImageItem(item);
			return;
		case TILING:
			addProcessingItem(item);
			return;
		case DISPLAYABLE:
			addDisplayableItem(item);
			return;
		}
		this.revalidate();
		this.doLayout();
	}

	private void addImageItem(QueueListItem item) {
		allItemsList.add(0, item);
		itemsToTransformQueue.add(item);
		this.add(item, getConstraintForY(0));
		makeLastItemLast();
		synchronized (itemsToTransformQueue) {
			itemsToTransformQueue.notifyAll();
		}
	}

	public QueueListItem getNextItem() {
		return itemsToTransformQueue.poll();
	}

	private void addDisplayableItem(QueueListItem sp) {
		sp.arrangeDisplayablePanel(null);
		int nextReady = 0;
		for (QueueListItem p : allItemsList) {
			switch (p.status) {
			case DISPLAYABLE:
				break;
			case QUEUED:
				nextReady++;
				break;
			case TILING:
				nextReady++;
				break;
			}
		}
		allItemsList.add(nextReady, sp);
		this.add(sp, getConstraintForY(nextReady));
		makeLastItemLast();
		synchronized (displayablesLock) {
			System.out.println("Adding displayable. notifying all displayables lock");
			displayablesLock.notifyAll();
		}

	}

	private void addProcessingItem(QueueListItem sp) {
		sp.arrangeStatusPanel();
		int nextReady = 0;
		for (QueueListItem p : allItemsList) {
			switch (p.status) {
			case DISPLAYABLE:
				break;
			case QUEUED:
				nextReady++;
			case TILING:
				nextReady++;
			}
		}
		allItemsList.add(nextReady, sp);
		this.add(sp, getConstraintForY(nextReady));
	}

	public Object getDisplayablesLock() {
		return displayablesLock;
	}

	public List<String> getDisplayables() {
		List<String> displayables = new LinkedList<String>();
		for (QueueListItem p : allItemsList) {
			switch (p.status) {
			case DISPLAYABLE:
				displayables.add(p.getFullPath());
			}
		}
		//System.out.println("displayables");
		System.out.println(displayables);
		return displayables;
	}

	public static GridBagConstraints getConstraintForY(int y) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = y;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		return c;
	}

	class RemovePanel implements ActionListener {
		private QueueListItem panelToRemove;

		public RemovePanel(QueueListItem toRemove) {
			panelToRemove = toRemove;
		}

		public void actionPerformed(ActionEvent arg0) {
			QueueListView.this.itemsToTransformQueue.remove(panelToRemove);
			QueueListView.this.allItemsList.remove(panelToRemove);
			QueueListView.this.remove(panelToRemove);
			QueueListView.this.doLayout();
			QueueListView.this.getParent().validate();
			QueueListView.this.revalidate();
			panelToRemove.requestRunInhibition();
			panelToRemove = null;
		}
	}

//	 public static void main(String[] args) {
//	 JFrame frame = new JFrame();
//	 JPanel main = new JPanel(new BorderLayout());
//	 JLabel top = new JLabel("Drop images and displayables here");
//	 top.setMinimumSize(new Dimension(150, 100));
//	 top.setPreferredSize(new Dimension(150, 100));
//	 top.setMaximumSize(new Dimension(150, 100));
//	
//	 main.add(top, BorderLayout.NORTH);
//	 main.add(new JScrollPane(new QueueListView(null)), BorderLayout.CENTER);
//	 main.add(new JLabel("Fancy sharing stuff here"), BorderLayout.SOUTH);
//	 frame.setContentPane(main);
//	 frame.pack();
//	 frame.setSize(340, 600);
//	 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	 frame.setVisible(true);
//	 }
}
