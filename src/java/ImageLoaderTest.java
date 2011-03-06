import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import net.niconomicon.tile.source.app.tiling.FastClipper;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class ImageLoaderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// JFrame jf = new JFrame("Jeff");
		// JPanel p = new JPanel();

		String image;
		image = "";
		image = "/Users/niko/2010_0928_Cable_BaseMap.jpg";//
		image = "/Users/niko/UBahnPlan.gif";
		image = "/Users/niko/tileSources/full_set_labels2.png";
		image = "/Users/niko/tileSources/netflixDataSetClassification.png";
		System.out.println("opening");
		BufferedImage rimage = readImage(new FileInputStream(image));
		System.out.println("writing");

		Rectangle clip = new Rectangle(0, 0, 256, 256);
		BufferedImage otherBuffer = FastClipper.fastClip(rimage, clip, false);

		ImageIO.write(otherBuffer, "png", new File("/Users/niko/one.png"));
		System.out.println("done");
		// jf.setContentPane(p);
		// jf.pack();
		// jf.setVisible(true);

	}

	private static BufferedImage readImage(InputStream picture) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte[] b = new byte[10240];
		int l = 0;
		while ((l = picture.read(b)) >= 0) {
			buf.write(b, 0, l);
		}
		buf.close();
		byte[] picturedata = buf.toByteArray();
		buf = null;
		try {
			return ImageIO.read(new ByteArrayInputStream(picturedata));
		} catch (IIOException e) {
			System.out.println("Could not just return the image");
			ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(picturedata));
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			ImageReader reader = null;
			while (readers.hasNext()) {
				reader = (ImageReader) readers.next();
				if (reader.canReadRaster()) break;
			}

			if (reader == null) throw new IOException("no reader found");
			// Set the input.
			reader.setInput(input);
			int w = reader.getWidth(0);
			int h = reader.getHeight(0);
			BufferedImage image;
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Image intImage = Toolkit.getDefaultToolkit().createImage(picturedata);
			new ImageIcon(intImage);
			Graphics2D g = image.createGraphics();
			g.drawImage(intImage, 0, 0, null);
			g.dispose();
			return image;
		}
	}
}
