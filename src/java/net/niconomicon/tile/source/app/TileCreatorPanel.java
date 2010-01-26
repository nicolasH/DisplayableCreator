package net.niconomicon.tile.source.app;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.niconomicon.tile.source.app.filter.ImageAndPDFFileFilter;
import net.niconomicon.tile.source.app.sharing.MapSharingPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author niko
 * 
 */
public class TileCreatorPanel extends JPanel {

	public static final int TILE_SIZE = 192;
	public static final String TILE_TYPE = "png";

	protected JComboBox tileSize;

	protected JTextField where;
	protected JTextField outputFileName;
	protected JTextField from;

	protected JButton browseInput;
	protected JButton browseOutput;

	protected JLabel imageProperty;

	JProgressBar progressIndicator;

	protected String place;
	protected String name;

	JFileChooser chooser;
	ImageAndPDFFileFilter imageFilter;
	FileFilter archiveFilter;

	MapSharingPanel sharingPanel;
	TilingPreview preview;

	JTextArea description;
	JTextField author;
	JTextField title;
	JTextField source;

	JButton finalizeButton;
	SQliteTileCreator creator;

	File temp;

	// protected TileArchiveCreator tileCreator;

	public TileCreatorPanel() {

		creator = new SQliteTileCreator();

		// setTitle("Tile Creator");
		JPanel content = new JPanel(new BorderLayout());
		JPanel option;
		// option = new JPanel();
		// BoxLayout l = new BoxLayout(option, BoxLayout.Y_AXIS);
		// option.setLayout(l);
		JPanel arch = new JPanel();

		imageFilter = new ImageAndPDFFileFilter();
		chooser = new JFileChooser();
		chooser.setFileFilter(imageFilter);

		preview = new TilingPreview();

		from = new JTextField("", 20);
		browseInput = new JButton("Browse");
		browseInput.addActionListener(new InputActionListener());

		// tileSize = new JComboBox(new String[] { "64", "128", "192", "256" });
		// tileSize.setSelectedIndex(2);
		tileSize = new JComboBox(new String[] { "" + TILE_SIZE });
		tileSize.setSelectedIndex(0);

		title = new JTextField("", 20);
		description = new JTextArea("", 5, 30);

		// could also load from the user's preferences
		source = new JTextField("", 20);

		// could also load from the user's preferences
		author = new JTextField(System.getProperty("user.name"), 20);

		// load from file name
		outputFileName = new JTextField("", 10);
		where = new JTextField("", 20);

		browseOutput = new JButton("Browse");
		browseOutput.addActionListener(new OutputActionListener());

		// //////////////////
		// Using the form layout.
		FormLayout layout = new FormLayout(
		// columns
		"right:pref, 5dlu, left:pref, 5dlu, left:pref",
		// rows
		"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu, p, 9dlu, p");
		// 15
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		// Obtain a reusable constraints object to place components in the grid.
		CellConstraints cc = new CellConstraints();

		// Fill the grid with components; the builder can create
		// frequently used components, e.g. separators and labels.

		// Add a titled separator to cell (1, 1) that spans 7 columns.
		int y = 1;
		builder.addLabel("Source image", cc.xy(1, y));
		builder.add(from, cc.xy(3, y));
		builder.add(browseInput, cc.xy(5, y));

		y++;
		y++;
		builder.addLabel("Title : ", cc.xy(1, y));
		builder.add(title, cc.xy(3, y));

		// y++;
		// y++;
		// builder.addLabel("Author : ", cc.xy(1, y));
		// builder.add(author, cc.xy(3, y));

		// y++;
		// y++;
		// builder.addLabel("Source : ", cc.xy(1, y));
		// builder.add(source, cc.xy(3, y));

		// y++;
		// y++;
		// builder.addLabel("Description :", cc.xy(1, y));
		// builder.add(description, cc.xyw(3, y, 3));
		//
		// y++;
		// y++;
		// builder.addLabel("tile side size", cc.xy(1, y));
		// builder.add(tileSize, cc.xy(3, y));

		// //////////////
		y++;
		y++;
		builder.addLabel("save as", cc.xy(1, y));
		builder.add(outputFileName, cc.xyw(3, y, 3));

		y++;
		y++;
		builder.addLabel("in directory", cc.xy(1, y));
		builder.add(where, cc.xy(3, y));
		builder.add(browseOutput, cc.xy(5, y));

		y++;
		y++;
		progressIndicator = new JProgressBar(0, 100);
		builder.addLabel("current action", cc.xy(1, y));
		builder.add(progressIndicator, cc.xyw(3, y, 3));

		y++;
		y++;
		finalizeButton = new JButton("Finalize Tiles DB");
		finalizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				finalizeTilesDB();
			}
		});
		finalizeButton.setEnabled(false);
		builder.add(finalizeButton, cc.xy(3, y));

		// The builder holds the layout container that we now return.
		option = builder.getPanel();
		// return builder.getPanel();
		content.add(option, BorderLayout.CENTER);
		// content.add(new JLabel("Image goes here"), BorderLayout.CENTER);

		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.NORTH);
		/*
		JPanel all = new JPanel(new BorderLayout());
		all.add(content, BorderLayout.NORTH);
		//TilingPreview preview = new TilingPreview();
		// this.setContentPane(content);
		 		this.setContentPane(all);
				this.pack();
				this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				this.setVisible(true);*/
	}

	protected String currentSourcePath;

	public void preTile(String sourcePath) {
		try {
			// Create temp file.
			if (temp == null) {
				temp = File.createTempFile("tempMap", ".mdb");
			}
			// Delete temp file when program exits.
			temp.deleteOnExit();
			currentSourcePath = sourcePath;

			Thread t = new Thread() {
				public void run() {
					try {
						// progressIndicator.setIndeterminate(true);
						progressIndicator.setStringPainted(true);
						progressIndicator.setString("Opening file ...");
						progressIndicator.setValue(1);
						long start = System.currentTimeMillis();
						Communicator comm = new Communicator(preview);
						creator.calculateTiles(temp.getAbsolutePath(), currentSourcePath, TILE_SIZE, TILE_TYPE, progressIndicator);
						long end = System.currentTimeMillis();
						System.out.println("creation time : " + (end - start) + " ms. == " + ((end - start) / 1000) + "s " + ((end - start) / 1000 / 60) + "min");
						finalizeButton.setEnabled(true);
						// progressIndicator.setIndeterminate(false);
						progressIndicator.setValue(100);
						progressIndicator.setString("100%");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void finalizeTilesDB() {
		System.err.println("maybe i should create the tiles");
		if (outputFileName.getText() != null && outputFileName.getText().length() > 0 && where.getText() != null && where.getText().length() > 0) {

			name = outputFileName.getText();
			place = where.getText();

			creator.author = author.getText();
			creator.description = description.getText();
			creator.source = source.getText();
			creator.title = title.getText();

			if (!place.endsWith(File.pathSeparator)) {
				place += File.separator;
			}
			finalizeButton.setEnabled(false);

			try {
				Thread t = new Thread() {
					public void run() {
						try {
							while (!creator.doneCalculating) {
								Thread.sleep(150);
							}
							// JOptionPane.showInputDialog(new ImageIcon(creator.mini));
							// JOptionPane.showInputDialog(new ImageIcon(creator.thumb));
							// // Communicator comm = new Communicator(preview);
							// creator.calculateTiles(place + name,
							// from.getText(), tSize, tType);

							creator.finalizeFile();
							temp.renameTo(new File(place, name));
							sharingPanel.setRootDir(place);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				t.start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void setSharingService(MapSharingPanel sharingPanel) {
		this.sharingPanel = sharingPanel;
	}

	public void setRootDir(String rootDir) {
		if (sharingPanel != null) {
			sharingPanel.setRootDir(rootDir);
		}
	}

	private class InputActionListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			String s = " some file";
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
				s = chooser.getSelectedFile().getName();
				try {
					from.setText(chooser.getSelectedFile().getCanonicalPath());
					String fileName = chooser.getSelectedFile().getName();
					String fileSansDot = fileName.substring(0, fileName.lastIndexOf("."));
					title.setText(fileSansDot);
					if (null == where.getText() || 0 == where.getText().length()) {
						where.setText(chooser.getSelectedFile().getParent());
					}
					String dot = ".mdb";
					outputFileName.setText(fileName + dot);
					preTile(chooser.getSelectedFile().getAbsolutePath());
				} catch (Exception e) {
					from.setText("cannot Open File");
					e.printStackTrace();
				}
			}
		}
	}

	private class OutputActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			String s = " some file";
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Thread t = new Thread(new Runnable() {
					public void run() {
						System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
						String s = chooser.getSelectedFile().getName();
						try {
							String path = chooser.getSelectedFile().getCanonicalPath();
							if (!chooser.getSelectedFile().isDirectory()) {
								outputFileName.setText(chooser.getSelectedFile().getName());
								chooser.getSelectedFile().getName();
								path = path.replaceAll(s, "");
							}
							where.setText(path);
							setRootDir(chooser.getSelectedFile().getAbsolutePath());
						} catch (Exception e) {
							outputFileName.setText("cannot Open File");
							where.setText("cannot open file");
							e.printStackTrace();
						}
					}
				});
				t.start();
			}
		}
	}

	// /**
	// * @param args
	// */
	// public static void main(String[] args) {
	// TileCreatorPanel f = new TileCreatorPanel();
	// }

}
