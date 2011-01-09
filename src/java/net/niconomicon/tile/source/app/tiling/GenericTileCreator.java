/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import net.niconomicon.tile.source.app.Ref;

/**
 * @author niko
 * 
 */
public class GenericTileCreator {

	public static final int defaultTileSize = 192;
	public static final String defaultTileType = "png";

	SQliteTileCreatorMultithreaded creator;

	public GenericTileCreator() {
		creator = new SQliteTileCreatorMultithreaded();
	}

	public void createTileSource(String sourcePath, String destFile, String title) throws Exception {

		System.out.println("Processing " + creator.title);
		creator.title = title;
		creator.calculateTiles(destFile, sourcePath, defaultTileSize, defaultTileType, null, 4);// new JProgressBar());
		creator.finalizeFile();

	}

	public static void createTiles(String pathToSource, String pathToDestination, int tileSize, String tileType) throws IOException {
		System.out.println("calculating tiles...");
		long mapID = 0;

		if (pathToSource == null || pathToDestination == null) { return; }
		// the pathTo file includes the fileName.
		File originalFile = new File(pathToSource);
		String fileSansExt = Ref.fileSansDot(pathToSource);

		System.out.println("Opening the image");
		ImageInputStream inStream = ImageIO.createImageInputStream(originalFile);
		System.out.println("Reading the image.");
		BufferedImage img = ImageIO.read(inStream);

		byte[] miniBytes = getMiniatureBytes(img, 320, 480, tileType);
		System.out.println("writing the miniature");

		// FileOutputStream miniOut = new FileOutputStream(new File("mini.png"));
		// miniOut.write(miniBytes);
		// miniOut.close();
		return;
	}

	public static byte[] getMiniatureBytes(BufferedImage sourceImage, int miniMaxWidth, int miniMaxHeight, String pictureType) throws IOException {
		System.out.println("Creating the miniature for size" + miniMaxWidth + "x" + miniMaxHeight);
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		double scaleX = miniMaxWidth / (double) width;
		double scaleY = miniMaxHeight / (double) height;
		double scaleFactor = Math.min(scaleX, scaleY);

		int scaledWidth = (int) (width * scaleFactor);
		int scaledHeight = (int) (height * scaleFactor);

		Image tmp = sourceImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
		BufferedImage scaled = new BufferedImage(miniMaxWidth, miniMaxHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = scaled.getGraphics();

		double x = 0;
		double y = 0;
		x = miniMaxWidth - tmp.getWidth(null);
		y = miniMaxHeight - tmp.getHeight(null);

		x = Math.floor(x / 2);
		y = Math.floor(y / 2);

		g.drawImage(tmp, (int) x, (int) y, null);
		g.dispose();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ImageIO.write(scaled, pictureType, outStream);
		return outStream.toByteArray();
	}

	public static void main(String[] args) throws IOException {
		try {
			createTiles("/Users/niko/tileSources/testMeyrin450.png", "testMeyrin450.png", 192, "png");
			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
