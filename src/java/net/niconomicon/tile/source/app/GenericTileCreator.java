/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * @author niko
 *
 */
public class GenericTileCreator {


	public static void createTiles(String pathToSource, String pathToDestination, int tileSize, String tileType) throws IOException {
		System.out.println("calculating tiles...");
		long mapID = 0;

		if (pathToSource == null || pathToDestination == null) { return; }
		// the pathTo file includes the fileName.
		File originalFile = new File(pathToSource);
		String fileSansExt = pathToSource.substring(pathToSource.lastIndexOf(File.separator) + 1, pathToSource.lastIndexOf("."));
		
		System.out.println("Opening the image");
		ImageInputStream inStream = ImageIO.createImageInputStream(originalFile);
		System.out.println("Reading the image.");
		BufferedImage img = ImageIO.read(inStream);

		byte[] miniBytes = getMiniatureBytes(img, 320, 480, tileType);
		System.out.println("writing the miniature");
		
		FileOutputStream miniOut = new FileOutputStream(new File("mini.png"));
		miniOut.write(miniBytes);
		miniOut.close();
		return;
	}

	public static byte[] getMiniatureBytes(BufferedImage sourceImage, int miniMaxWidth, int miniMaxHeight, String pictureType) throws IOException {
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		double scaleX = miniMaxWidth / (double) width;
		double scaleY = miniMaxHeight / (double) height;
		double scaleFactor = Math.min(scaleX, scaleY);

		System.out.println("scaleX=" + scaleX + " scaleY=" + scaleY + " scale factor = " + scaleFactor);
		int scaledWidth = (int) (width * scaleFactor);
		int scaledHeight = (int) (height * scaleFactor);

		System.out.println("Scaling the image");
		Image tmp = sourceImage.getScaledInstance(scaledWidth,scaledHeight, Image.SCALE_SMOOTH);
//		BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		BufferedImage scaled = new BufferedImage(miniMaxWidth, miniMaxHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = scaled.getGraphics();
		System.out.println("Painting the scaled image");
		double x = 0;
		double y = 0;
		x = miniMaxWidth - tmp.getWidth(null);
		y = miniMaxHeight - tmp.getHeight(null);
		
		x = Math.floor(x/2);
		y = Math.floor(y/2);
		
		g.drawImage(tmp, (int)x, (int)y, null);
		g.dispose();
		System.out.println("creating the byte array");
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
