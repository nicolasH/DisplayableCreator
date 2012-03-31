/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * This class provides static methods to extract a rectangular part of a given
 * image.
 * 
 * @author Nicolas Hoibian
 * 
 */
public class FastClipper {

	/** Kept for historical/cut-and-paste purpose : */
	private static BufferedImage slowClip(BufferedImage image, Rectangle clip) {
		Raster ras = image.getData(clip);
		BufferedImage ret = new BufferedImage(clip.width, clip.height, image.getType());
		ret.getRaster().setRect(-clip.x, -clip.y, ras);
		return ret;
	}

	/**
	 * inspired from :
	 * http://stackoverflow.com/questions/2825837/java-how-to-do-
	 * fast-copy-of-a-bufferedimages-pixels-unit-test-included Does an arraycopy
	 * of the rasters .
	 * 
	 * @param src
	 *            source image.
	 * @param clip
	 *            the part of the image that you want.
	 * @return an image which is identical to the part of the image of src in
	 *         the area described by clip.
	 */
	public static BufferedImage fastClip(final BufferedImage src, Rectangle clip) {
		return fastClip(src, clip, false);
	}

	public static BufferedImage fastClip(final BufferedImage src, Rectangle clip, boolean flipVertically) {
		return fastClip(src, clip, flipVertically, src.getType());
	}

	/**
	 * @param src
	 *            source image.
	 * @param clip
	 *            the part of the image that you want.
	 * @param flipVertically
	 *            should the image be flipped vertically ?
	 * @return an independent copy of the part of the image described by clip,
	 *         flipped vertically according to 'flipVertically'.
	 */
	public static BufferedImage fastClip(final BufferedImage src, Rectangle clip, boolean flipVertically, int type) {
		// System.out.println("Flipping image of type " + src.getType() +
		// " given type : " + type +
		// " and transparency : " + src.getTransparency());
		if (src.getType() != BufferedImage.TYPE_CUSTOM) {
			type = src.getType();
		}
		BufferedImage dst = new BufferedImage(clip.width, clip.height, type);

		Object srcbuf = null;
		Object dstbuf = null;

		int mpx = src.getWidth() * src.getHeight();
		int factor = 1;
		DataBuffer buff = src.getRaster().getDataBuffer();
		/**
		 * Handles transparency correctly for some GIFs
		 */
		if (src.getColorModel() instanceof IndexColorModel) {
			dst = new BufferedImage(clip.width, clip.height, src.getType(), (IndexColorModel) src.getColorModel());
		}

		InspectionResult ir = inspectSrc(buff, dst, mpx);
		factor = ir.factor;
		srcbuf = ir.srcbuf;
		dstbuf = ir.dstbuf;

		int srcOffset, dstOffset = 0;

		int dstLineOffset, srcLineOffset = 0;

		final int copyLength = clip.width * factor;

		if (flipVertically) {
			// reading backward
			srcOffset = src.getWidth() * (clip.y + clip.height - 1) * factor + clip.x * factor;
			dstLineOffset = clip.width * factor;
			srcLineOffset = -src.getWidth() * factor;

		} else {
			// reading forward
			srcOffset = src.getWidth() * clip.y * factor + clip.x * factor;

			dstLineOffset = clip.width * factor;
			srcLineOffset = src.getWidth() * factor;
		}
		for (int y = 0; y < clip.height; y++) {
			System.arraycopy(srcbuf, srcOffset, dstbuf, dstOffset, copyLength);

			srcOffset += srcLineOffset;
			dstOffset += dstLineOffset;
		}

		return dst;
	}

	public static BufferedImage fastPaste(final BufferedImage src, final BufferedImage dst, int xOffset, int yOffset, boolean isFlippedVertically) {

		Object srcbuf = null;
		Object dstbuf = null;

		int mpx = src.getWidth() * src.getHeight();
		int factor = 1;
		DataBuffer buff = src.getRaster().getDataBuffer();

		InspectionResult ir = inspectSrc(buff, dst, mpx);
		factor = ir.factor;
		srcbuf = ir.srcbuf;
		dstbuf = ir.dstbuf;

		int srcOffset, dstOffset = 0;

		int dstLineOffset, srcLineOffset = 0;

		final int copyLength = src.getWidth() * factor;

		dstOffset = dst.getWidth() * factor * yOffset + xOffset * factor;
		dstLineOffset = dst.getWidth() * factor;

		if (isFlippedVertically) {
			// read the source in reverse order and past it in the correct order
			srcOffset = src.getHeight() * src.getWidth() * factor; // -1
			srcLineOffset = -(src.getWidth() * factor);
			srcOffset += srcLineOffset;
		} else {
			// read and paste the source in the same order
			srcOffset = 0;
			srcLineOffset = src.getWidth() * factor;
		}
//		System.out.println("Source of size "+ ((byte[])srcbuf).length);
//		System.out.println("copylength:" + copyLength + " srcLineOffset" + srcLineOffset + " dstLineOffset" + dstLineOffset);
		for (int y = 0; y < src.getHeight(); y++) {
//			System.out.println("Y=" + y + " srcOffset:" + srcOffset + " dstOffset:" + dstOffset + " srcSize");
			System.arraycopy(srcbuf, srcOffset, dstbuf, dstOffset, copyLength);

			srcOffset += srcLineOffset;
			dstOffset += dstLineOffset;
		}

		return dst;
	}

	public static InspectionResult inspectSrc(DataBuffer buff, BufferedImage dst, int mpx) {
		InspectionResult ir = new InspectionResult();
		/**
		 * Different type of image have different type of underlying buffer.
		 * Each type has a different number of cells dedicated to a single
		 * pixel.
		 */
		if (buff instanceof DataBufferShort) {
			ir.srcbuf = ((DataBufferShort) buff).getData();
			ir.dstbuf = ((DataBufferShort) dst.getRaster().getDataBuffer()).getData();
			ir.factor = ((DataBufferShort) buff).getData().length / mpx;
		}
		if (buff instanceof DataBufferByte) {
			ir.srcbuf = ((DataBufferByte) buff).getData();
			ir.dstbuf = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
			ir.factor = ((DataBufferByte) buff).getData().length / mpx;
		}
		if (buff instanceof DataBufferUShort) {
			ir.srcbuf = ((DataBufferUShort) buff).getData();
			ir.dstbuf = ((DataBufferUShort) dst.getRaster().getDataBuffer()).getData();
			ir.factor = ((DataBufferUShort) buff).getData().length / mpx;
		}
		if (buff instanceof DataBufferInt) {
			ir.srcbuf = ((DataBufferInt) buff).getData();
			ir.dstbuf = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
			ir.factor = ((DataBufferInt) buff).getData().length / mpx;
		}
		if (buff instanceof DataBufferFloat) {
			ir.srcbuf = ((DataBufferFloat) buff).getData();
			ir.dstbuf = ((DataBufferFloat) dst.getRaster().getDataBuffer()).getData();
			ir.factor = ((DataBufferFloat) buff).getData().length / mpx;
		}
		if (buff instanceof DataBufferDouble) {
			ir.srcbuf = ((DataBufferDouble) buff).getData();
			ir.dstbuf = ((DataBufferDouble) dst.getRaster().getDataBuffer()).getData();
			ir.factor = ((DataBufferDouble) buff).getData().length / mpx;
		}
		return ir;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		BufferedImage src = ImageIO.read(file);
		int clipWidth = 192;
		int clipHeight = 192;
		Rectangle clip = new Rectangle((src.getWidth() - clipWidth) / 2, (src.getHeight() - clipHeight) / 2, clipWidth, clipHeight);
		System.out.println("Args length:" + args.length);
		if (args.length > 1) {
			// showClips(src, clipWidth);
			showPaste(src, clipWidth);
			return;
		}
		long start, stop;
		int m = 10;
		int n = 1000;
		for (int k = 0; k < m; k++) {
			start = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				FastClipper.fastClip(src, clip);
			}
			stop = System.currentTimeMillis();
			System.out.println("fastClip :" + n + " times = " + (stop - start) + " ms");
		}
		for (int k = 0; k < m; k++) {
			start = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				FastClipper.fastClip(src, clip, true);
			}
			stop = System.currentTimeMillis();
			System.out.println("flipClip :" + n + " times = " + (stop - start) + " ms");
		}

		for (int k = 0; k < m; k++) {
			start = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				FastClipper.slowClip(src, clip);
			}
			stop = System.currentTimeMillis();
			System.out.println("slowClip :" + n + " times = " + (stop - start) + " ms");
		}
	}

	public static void showClips(BufferedImage src, int tileSize) {
		int n = 4;

		JPanel fastClip = new JPanel();
		JPanel slowClip = new JPanel();
		
		JFrame frame = setupPanels(fastClip, slowClip, n, tileSize, true);
		long start, stop;
		start = System.currentTimeMillis();
		Map<Point, Rectangle> clips = getClips(src.getWidth(), src.getHeight(), tileSize, true);

		List<Point> points = new ArrayList<Point>();
		for (int y = 0; y < Math.sqrt(n); y++) {
			for (int x = 0; x < Math.sqrt(n); x++) {
				points.add(new Point(x, y));
			}
		}
		for (Point p : points) {
			Rectangle clip = clips.get(p);
			JLabel l = new JLabel(new ImageIcon(FastClipper.fastClip(src, clip, false)));
			fastClip.add(l);
		}
		stop = System.currentTimeMillis();
		System.out.println("fast : stop - start = " + (stop - start) + " ms");
		start = System.currentTimeMillis();
		for (Point p : points) {
			Rectangle clip = clips.get(p);
			JLabel l = new JLabel(new ImageIcon(FastClipper.fastClip(src, clip, true)));
			slowClip.add(l);
		}
		stop = System.currentTimeMillis();
		System.out.println("slow : stop - start = " + (stop - start) + " ms");

		frame.pack();
		frame.setVisible(true);
	}

	public static void showPaste(BufferedImage src, int tileSize) {
		int n = 4;
		int aX = 4;
		int aY = 3;
		JPanel fastClip = new JPanel();
		JPanel rightPanel = new JPanel();

		JFrame frame = setupPanels(fastClip, rightPanel, n, tileSize, false);

		List<Point> points = new ArrayList<Point>();
		for (int y = aY; y < aY + Math.sqrt(n); y++) {
			for (int x = aX; x < aX+Math.sqrt(n); x++) {
				points.add(new Point(x, y));
			}
		}

		Map<Point, Rectangle> clips = getClips(src.getWidth(), src.getHeight(), tileSize, true);
		BufferedImage dst = new BufferedImage(tileSize * 2, tileSize * 2, src.getType());

		for (Point p : points) {
			Rectangle clip = clips.get(p);
			BufferedImage tile = FastClipper.fastClip(src, clip, true);
			JLabel l = new JLabel(new ImageIcon(tile));
			fastClip.add(l);
			//System.out.println(p.x + " - "+aX + " " + p.y+"-"+aY);
			fastPaste(tile, dst, (p.x - aX) * tileSize, (p.y-aY)  * tileSize, true);
		}
		JLabel l = new JLabel(new ImageIcon(dst));
		rightPanel.add(l);
		frame.pack();
		frame.setVisible(true);

	}

	public static JFrame setupPanels(JPanel leftP, JPanel rightP, int tileCount, int tileSize, boolean symetrical) {
		int rowLength = (int) Math.sqrt(tileCount);
		leftP.setLayout(new GridLayout(0, rowLength));
		if (symetrical) {
			rightP.setLayout(new GridLayout(0, rowLength));
		} else {
			rightP.setLayout(new GridLayout(0, 1));
		}
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JScrollPane left = new JScrollPane(leftP);
		JScrollPane right = new JScrollPane(rightP);
		Dimension min = new Dimension(rowLength * tileSize, tileSize * (tileCount / rowLength));
		rightP.setMinimumSize(min);
		rightP.setPreferredSize(min);
		leftP.setMinimumSize(min);
		leftP.setPreferredSize(min);
		split.add(left);
		split.add(right);
		JFrame frame = new JFrame();
		frame.setContentPane(split);
		return frame;
	}

	public static Map<Point, Rectangle> getClips(int width, int height, int tileSize, boolean originalLayer) {
		int nbX = (int) Math.ceil((double) width / tileSize);
		int nbY = (int) Math.ceil((double) height / tileSize);
		int fillX = ((nbX * tileSize) - width);
		int fillY = ((nbY * tileSize) - height);
		Map<Point, Rectangle> clips = new HashMap<Point, Rectangle>();
		for (int y = 0; y < nbY; y++) {
			for (int x = 0; x < nbX; x++) {
				int copyX = x * tileSize;
				int copyY = y * tileSize;
				int copyWidth = tileSize;
				int copyHeight = tileSize;

				// first column
				if (x == 0) {
					copyX = 0;
					copyWidth = tileSize;
				}
				// last column
				if (x == nbX - 1) {
					copyX = x * tileSize;
					copyWidth = tileSize - fillX;
				}

				// last line
				if (y == nbY - 1) {
					copyY = y * tileSize;
					copyHeight = tileSize - fillY;
				}
				if (!originalLayer) {
					copyX = copyY = 0;
				}
				Rectangle clip = new Rectangle(copyX, copyY, copyWidth, copyHeight);
				clips.put(new Point(x, y), clip);
			}
		}
		return clips;
	}
}
