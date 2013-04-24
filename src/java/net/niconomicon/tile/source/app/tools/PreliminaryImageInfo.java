package net.niconomicon.tile.source.app.tools;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class PreliminaryImageInfo {

	public static final float IMAGE_TO_DISP_FACTOR = 1.3f;
	Dimension dim;
	// String reason;
	String shortMessage;
	String longMessage;
	Exception ex;

	boolean openable = false;
	ImageTypeSpecifier type;
	String imgPath;
	long pixelbits = 0l;
	long rasterbits = 0l;

	public PreliminaryImageInfo(String absolutePath) {
		imgPath = absolutePath;
	}

	public void calculatePixelBits() {
		if (type != null) {
			int bands = type.getNumBands();
			pixelbits = 0;
			for (int band = 0; band < bands; band++) {
				pixelbits += (long) type.getBitsPerBand(band);
			}
			rasterbits = Math.abs(pixelbits) * (long) dim.width * (long) dim.height;
		}
	}

	public boolean checkOpenable() {
		String suffix = imgPath.substring(imgPath.lastIndexOf('.') + 1);
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		openable = true;
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(new File(imgPath));
				reader.setInput(stream);
				Iterator<ImageTypeSpecifier> types = reader.getImageTypes(0);
				while (types.hasNext()) {
					type = types.next();
					// System.out.println(types.next());
				}
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);
				dim = new Dimension(width, height);
				shortMessage = null;
				longMessage = null;
				openable = true;
			} catch (IOException e) {
				// reason = e.getMessage();
				ex = e;
				dim = null;
				openable = false;
				System.err.println(e.getMessage());
			} finally {
				reader.dispose();
			}
			if (openable == false) { return openable; }
		} else {
			dim = null;
			shortMessage = "No image reader found for " + suffix;
			longMessage = shortMessage + ". Supported image formats: ";
			String[] formats = ImageIO.getReaderFormatNames();
			for (String formatName : formats) {
				longMessage += formatName + ", ";
			}
			longMessage.substring(0, longMessage.length() - 2);
			longMessage += ".";

			openable = false;
			return openable;
		}
		if (type == null) {
			shortMessage = "Cannot open this particular image. Could not read properly its actual type.";
			longMessage = shortMessage + " Supported image formats: ";
			String[] formats = ImageIO.getReaderFormatNames();
			for (String formatName : formats) {
				longMessage += formatName + ", ";
			}
			longMessage.substring(0, longMessage.length() - 2);
			longMessage += ".";

			openable = false;
			return openable;
		}
		calculatePixelBits();
		long maxbytes = Runtime.getRuntime().maxMemory();

		if ((rasterbits / 8) * IMAGE_TO_DISP_FACTOR >= maxbytes) {
			openable = false;
			float needMB = ((rasterbits / 8) * IMAGE_TO_DISP_FACTOR) / 1024 / 1024;
			float haveMB = maxbytes / 1024 / 1024;
			needMB = needMB * 10;
			haveMB = haveMB * 10;
			long need = (long) needMB;
			long have = (long) haveMB;

			shortMessage = "Not enough memory to open the image and transform it into a Displayable.";
			longMessage = shortMessage + " The app needs at least" + need / 10.0f + "MB of memory. It can currently only use up to " + have / 10.0f+ "MB";
			return openable;
		}
		return openable;
	}

	public String getLongMessage() {
		return longMessage;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public Exception getException() {
		return ex;
	}

	public String toString() {
		if (dim == null) { return "Unopenable: " + shortMessage; }
		float mpx = dim.width * dim.height;
		long l = Math.round(mpx / 100000);
		mpx = l / 10.0f;

		// System.out.println("dim:" + dim.width + "x" + dim.height);
		// System.out.println("type:" + type);
		long maxbytes = Runtime.getRuntime().maxMemory();

		if (type != null) {
			System.out.println("PIXELBITS:" + pixelbits + " RASTERBITS:" + rasterbits);
			long pixelMB = rasterbits / (8 * 1024 * 1024);
			String megaBytes = "pixel MB:" + pixelMB + "pMB) ram MB: " + " (" + maxbytes / 1024 / 1024 + " MB)";

			if (openable == false) {
				return "Unopenable: " + shortMessage + " size:" + dim.width + "x" + dim.height + " (=" + mpx + "MPX ) " + megaBytes;
			} else {

				long raster_weight = rasterbits / 8;
				return "Openable. Size:" + dim.width + "x" + dim.height + " (=" + mpx + "MPX / )" + " type: " + type.getBufferedImageType()
						+ " weight:" + raster_weight + " / " + raster_weight / 1024.0f / 1024.0f + " " + megaBytes;
			}
		}
		return "Openable. Size:" + dim.width + "x" + dim.height;
	}
}
