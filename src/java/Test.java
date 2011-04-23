import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class Test {

	public static void main(String[] args) {
		try {
			System.out.println("Available processors : " + Runtime.getRuntime().availableProcessors());
			int[] types = new int[] { BufferedImage.TYPE_CUSTOM, // 0
			BufferedImage.TYPE_INT_RGB, // 1
			BufferedImage.TYPE_INT_ARGB, // 2
			BufferedImage.TYPE_INT_ARGB_PRE,// 3
			BufferedImage.TYPE_INT_BGR, // 4
			BufferedImage.TYPE_3BYTE_BGR, // 5
			BufferedImage.TYPE_4BYTE_ABGR, // 6
			BufferedImage.TYPE_4BYTE_ABGR_PRE,// 7
			BufferedImage.TYPE_USHORT_565_RGB, // 8
			BufferedImage.TYPE_USHORT_555_RGB, // 9
			BufferedImage.TYPE_BYTE_GRAY, // 10
			BufferedImage.TYPE_USHORT_GRAY,// 11
			BufferedImage.TYPE_BYTE_BINARY,// 12
			BufferedImage.TYPE_BYTE_INDEXED }; // 13

			String[] typeNames = new String[] { "BufferedImage.TYPE_CUSTOM",// 0
			"BufferedImage.TYPE_INT_RGB",// 1
			"BufferedImage.TYPE_INT_ARGB",// 2
			"BufferedImage.TYPE_INT_ARGB_PRE",// 3
			"BufferedImage.TYPE_INT_BGR",// 4
			"BufferedImage.TYPE_3BYTE_BGR",// 5
			"BufferedImage.TYPE_4BYTE_ABGR",// 6
			"BufferedImage.TYPE_4BYTE_ABGR_PRE",// 7
			"BufferedImage.TYPE_USHORT_565_RGB",// 8
			"BufferedImage.TYPE_USHORT_555_RGB",// 9
			"BufferedImage.TYPE_BYTE_GRAY",// 10
			"BufferedImage.TYPE_USHORT_GRAY",// 11
			"BufferedImage.TYPE_BYTE_BINARY",// 12
			"BufferedImage.TYPE_BYTE_INDEXED" }; // 13
			for (int i = 0; i < types.length; i++) {
				System.out.println("Type : " + types[i] + "=" + typeNames[i]);
			}
			BufferedImage img = ImageIO.read(new File("/Users/niko/tileSources/full_set_labels2k.png"));
			int scaledH = img.getHeight() / 2;
			int scaledW = img.getWidth() / 2;
			Image xxx = img.getScaledInstance(img.getWidth() / 2, img.getHeight() / 2, Image.SCALE_SMOOTH);

			img = new BufferedImage(scaledW, scaledH, img.getType());
			System.out.println("Image typ : " + img.getType());
			Graphics g0 = img.createGraphics();
			g0.drawImage(xxx, 0, 0, scaledW, scaledH, 0, 0, scaledW, scaledH, null);
			g0.dispose();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
