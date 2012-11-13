package net.niconomicon.tile.source.app.input;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.filter.DisplayableFilter;
import net.niconomicon.tile.source.app.fonts.FontLoader;
import net.niconomicon.tile.source.app.sharing.ResultStruct;
import net.niconomicon.tile.source.app.tiling.Inhibitor;

public class QueueListItem extends JPanel implements Inhibitor {

	private File file = null;
	public IS status;

	public static int minWidth = 300;
	public static int minHeight = 40;
	JLabel titleLabel;
	QueueListView container;
	private Boolean shouldInhibit = false;

	JButton editButton;
	JButton removeButton;
	Color defaultButtonBackground;

	public QueueListItem(File f, QueueListView container) {
		super(new GridBagLayout());
		defaultButtonBackground = this.getBackground();
		file = f;
		this.container = container;
		if (DisplayableFilter.isProbablyDisplayableFile(f)) {
			arrangeDisplayablePanel(f);
		} else {
			init();
		}
	}

	public String getFullPath() {
		return file.getAbsolutePath();
	}

	private void init() {
		this.status = IS.QUEUED;
		GridBagConstraints c;
		int y = 0;
		int x = 0;
		titleLabel = new JLabel(this.getName());

		c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = 5;
		c.weightx = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		this.add(titleLabel, c);

		x = 6;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;
		removeButton = FontLoader.getButtonSmall(FontLoader.iconTrash);
		removeButton.setToolTipText("Remove this image from the queue");
		this.add(removeButton, c);
		removeButton.addActionListener(container.new RemovePanel(this));

		finishPanel(minHeight);
	}

	/**
	 * 
	 * @param displayable
	 *            the displayable to replace the inner file or null if the inner
	 *            file is already a displayable
	 */
	public void arrangeDisplayablePanel(File displayable) {
		if (displayable != null) {
			this.file = displayable;
		}
		this.status = IS.DISPLAYABLE;
		this.removeAll();
		GridBagConstraints c;
		int y = 0;
		int x = 0;

		titleLabel = new JLabel(this.getName());
		c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = 5;
		c.weightx = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		this.add(this.titleLabel, c);

		x = 6;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;

		editButton = FontLoader.getButtonSmall(FontLoader.iconSave);
		adjustEditButton();
		this.add(editButton, c);
		editButton.addActionListener(new EditAction());
		defaultButtonBackground = editButton.getBackground();
		x = 7;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;
		JButton view = FontLoader.getButtonSmall(FontLoader.iconView);
		this.add(view, c);
		view.addActionListener(new ViewAction());

		x = 8;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;

		JLabel spacer = new JLabel("    ");
		this.add(spacer, c);

		x = 9;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;

		removeButton = FontLoader.getButtonSmall(FontLoader.iconTrash);
		removeButton.setToolTipText("Remove this Displayable from the list");
		this.add(removeButton, c);
		removeButton.addActionListener(container.new RemovePanel(this));

		System.out.println("Insets for edit:" + editButton.getInsets());
		System.out.println("Border for edit:" + editButton.getBorder());

		finishPanel(minHeight);
	}

	public void arrangeStatusPanel() {
		this.status = IS.TILING;
		GridBagConstraints c;
		int y = 0;
		int x = 0;

		this.removeAll();

		JLabel subStatus = new JLabel("Creating displayable");

		c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = 3;
		c.weightx = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;
		this.add(this.titleLabel, c);

		x = 4;
		y++;
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.gridwidth = 1;
		c.anchor = c.LINE_END;
		JButton cancel = FontLoader.getButtonSmall(FontLoader.iconTrash);
		cancel.setToolTipText("Stop the displayable creation and remove this item from the list");
		this.add(cancel, c);

		cancel.addActionListener(container.new RemovePanel(this));

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;

		JProgressBar progressIndicator = new JProgressBar(0, 100);
		this.add(progressIndicator, c);
		progressIndicator.setIndeterminate(true);

		finishPanel(60);

	}

	private void finishPanel(int myHeight) {
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setAlignmentY(TOP_ALIGNMENT);
		this.setMaximumSize(new Dimension(Short.MAX_VALUE, myHeight));
		this.setMinimumSize(new Dimension(minWidth, myHeight));

		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

		this.doLayout();
		this.revalidate();
		this.repaint();

	}

	public enum IS {
		QUEUED, TILING, DISPLAYABLE
	}

	public String getName() {
		if (file == null) { return "?????????"; }
		if (status == IS.DISPLAYABLE) {
			List<String> names = Ref.getDisplayableTitles(file.getAbsolutePath());
			if (names != null && names.size() > 0) {
				return names.get(0);
			} else {
				return file.getName();
			}
		} else {
			return file.getName();
		}
	}

	public boolean hasRunInhibitionBeenRequested() {
		return shouldInhibit;
	}

	public void requestRunInhibition() {
		synchronized (shouldInhibit) {
			shouldInhibit = true;
		}
	}

	public void changeTitle(String newTitle) {
		this.titleLabel.setText(newTitle);
	}

	public void changeLocation(File newPath) {
		this.file = newPath;
		adjustEditButton();
	}

	public void adjustEditButton() {
		if (Ref.isInTmpLocation(this.file.getAbsolutePath())) {
			editButton.setText(FontLoader.iconSave);
			editButton.setToolTipText("Save this new Displayable & change its details");
			editButton.setBackground(Color.orange);
		} else {
			editButton.setText(FontLoader.iconEdit);
			editButton.setToolTipText("Change this Displayable's details");
			editButton.setBackground(defaultButtonBackground);
		}
	}

	public class ViewAction implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			container.getViewer().setDisplayable(getFullPath());
		}
	}

	public class EditAction implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			SaveDialog dialog = QueueListItem.this.container.getSaver();
			ResultStruct newInfos = dialog.showDialog(container, getFullPath());
			if (newInfos.movedDispFile != null) {
				QueueListItem.this.changeLocation(newInfos.movedDispFile);
			}
			if (newInfos.newTitle != null) {
				titleLabel.setText(newInfos.newTitle);
			}
			if (newInfos.movedDispFile != null || newInfos.newTitle != null) {
				QueueListItem.this.revalidate();
			}
		}
	}
}
