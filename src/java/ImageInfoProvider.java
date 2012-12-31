import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import net.niconomicon.tile.source.app.Ref;

public class ImageInfoProvider {

	public static Dimension getImageDim(final String path) {
		Dimension result = null;
		String suffix = path.substring(path.lastIndexOf('.') + 1);
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(new File(path));
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				result = new Dimension(width, height);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} finally {
				reader.dispose();
			}
		} else {
			System.err.println("No image reader found");
		}
		return result;
	}

	public static void main(String[] args) {
		String[] paths = new String[] { "/Users/niko/Pictures/IMG_0077.JPG", "/Users/niko/Pictures/IMG_0143.JPG",
				"/Users/niko/Pictures/IMG_0168.JPG", "/Users/niko/Pictures/IMG_2150.JPG", "/Users/niko/Pictures/IMG_2151.JPG",
				"/Users/niko/Pictures/IMG_2188.JPG", "/Users/niko/Pictures/IMG_2192.JPG", "/Users/niko/Pictures/IMG_2193.JPG",
				"/Users/niko/Pictures/IMG_2194.JPG", "/Users/niko/Pictures/IMG_2195.JPG", "/Users/niko/Pictures/IMG_2196.JPG",
				"/Users/niko/Pictures/IMG_2197.JPG", "/Users/niko/Pictures/IMG_2198.JPG", "/Users/niko/Pictures/IMG_2199.JPG",
				"/Users/niko/Pictures/IMG_2200.JPG", "/Users/niko/Pictures/IMG_2204.JPG", "/Users/niko/Pictures/IMG_2208.JPG",
				"/Users/niko/Pictures/IMG_2209.JPG", "/Users/niko/Pictures/IMG_2305.JPG", "/Users/niko/Pictures/IMG_2306.JPG",
				"/Users/niko/Pictures/IMG_2307.JPG", "/Users/niko/Pictures/IMG_2308.JPG", "/Users/niko/Pictures/IMG_2309.JPG",
				"/Users/niko/Pictures/IMG_2310.JPG", "/Users/niko/Pictures/IMG_2311.JPG", "/Users/niko/Pictures/IMG_2315.JPG",
				"/Users/niko/Pictures/IMG_2360.JPG", "/Users/niko/Pictures/IMG_2463.JPG", "/Users/niko/Pictures/IMG_2464.JPG",
				"/Users/niko/Pictures/IMG_2465.JPG", "/Users/niko/Pictures/IMG_2466.JPG", "/Users/niko/Pictures/IMG_2467.JPG",
				"/Users/niko/Pictures/IMG_2468.JPG", "/Users/niko/Pictures/IMG_2469.JPG", "/Users/niko/Pictures/IMG_2470.JPG",
				"/Users/niko/Pictures/IMG_2471.JPG", "/Users/niko/Pictures/IMG_2472.JPG", "/Users/niko/Pictures/IMG_2473.JPG",
				"/Users/niko/Pictures/IMG_2508.JPG", "/Users/niko/Pictures/IMG_2596.JPG", "/Users/niko/Pictures/IMG_2755.JPG",
				"/Users/niko/Pictures/IMG_2756.JPG", "/Users/niko/Pictures/IMG_2757.JPG", "/Users/niko/Pictures/IMG_2758.JPG",
				"/Users/niko/Pictures/IMG_2759.JPG", "/Users/niko/Pictures/IMG_2761.JPG", "/Users/niko/Pictures/IMG_2763.JPG",
				"/Users/niko/Pictures/IMG_2764.JPG", "/Users/niko/Pictures/IMG_2765.JPG", "/Users/niko/Pictures/IMG_2766.JPG",
				"/Users/niko/Pictures/IMG_2767.JPG", "/Users/niko/Pictures/IMG_2768.JPG", "/Users/niko/Pictures/Jean-Giraud-aka-Moebius-Hunter.jpg",
				"/Users/niko/Pictures/PICT4401.JPG", "/Users/niko/Pictures/fiction-infographic.jpg",
				"/Users/niko/Pictures/fisainfographic3_nofoot.jpg", "/Users/niko/Pictures/tumblr_lu0j02prfd1qz9b3k.jpg",
				"/Users/niko/Pictures/Big pictures/srtm_ramp2.world.21600x10800.jpg",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.A1.png",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.A2.png",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.B1.png",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.B2.png",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.C1.png",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.C2.png",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.D1.png",
				"/Users/niko/Pictures/Big pictures/world.topo.bathy.200407.3x21600x21600.panels.png/world.topo.bathy.200407.3x21600x21600.D2.png" };
		long start = System.currentTimeMillis();
		for (String path : paths) {
			System.out.println(Ref.fileSansDot(path) + " -> " + getImageDim(path));
		}
		long stop = System.currentTimeMillis();
		System.out.println("It took " + (stop - start) + " milliseconds to get informations about " + paths.length + " images");
	}
}
