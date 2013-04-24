package net.niconomicon.tile.source.app.tools;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class ImageInfoProvider {

	// Hopefully conservative magic_number

	// public static PreliminaryImageInfo checkCanOpenSize(PreliminaryImageInfo
	// preInfo) {
	// long current_size = preInfo.dim.width * preInfo.dim.height;
	// long max = Runtime.getRuntime().maxMemory();
	// long pixelBits = preInfo.pixelBits();
	//
	// if (max / 1024 / 1024 < 1600) {
	// // 1.5 gig of ram, likely 32 bits.
	// if (current_size > max_megapix_1500) {
	// PreliminaryImageInfo pre = new PreliminaryImageInfo();
	// pre.dim = preInfo.dim;
	// pre.openable = false;
	// pre.reason = "Image too big for the available memory.";
	// return pre;
	// } else {
	// return preInfo;
	// }
	// } else {
	// // likely 2 GB
	// if (current_size > max_megapix_2000) {
	// PreliminaryImageInfo pre = new PreliminaryImageInfo();
	// pre.dim = preInfo.dim;
	// pre.openable = false;
	// pre.reason = "Image too big for the available memory";
	// return pre;
	// } else {
	// return preInfo;
	// }
	// }
	// }
	//
	// public static PreliminaryImageInfo canOpenImage(String path) {
	// PreliminaryImageInfo preInfos = getImageSize(path);
	// if (preInfos.openable == false) { return preInfos; }
	// preInfos = checkCanOpenSize(preInfos);
	// if (preInfos.openable == false) { return preInfos; }
	// long max = Runtime.getRuntime().maxMemory();
	// System.out.println("Max mem:" + max / 1024 / 1024 + "mb " + max +
	// "bytes");
	//
	// return preInfos;
	// }

	public static void main(String[] args) {
		String basePath = "/Users/niko/TileSources/Source Images/";
		File file = new File(basePath);
		String[] dirs = file.list();
		int max = 20;
		int i = 0;
		System.out.println("maxlong:" + Long.MAX_VALUE);
		for (String f : dirs) {
			File tmp = new File(basePath + f);
			if (tmp.isFile()) {
				PreliminaryImageInfo info = new PreliminaryImageInfo(tmp.getAbsolutePath());
				info.checkOpenable();
				System.out.println(tmp.getName() + " -> " + info);
				// System.out.println(tmp.getName() + " -> " +
				// canOpenImage(tmp.getAbsolutePath()));//
				// getImageSize(tmp.getAbsolutePath()));
				i += 1;
				// }
			}
			if (i == max) { return; }
		}

		// long start = System.currentTimeMillis();
		// for (String path : paths) {
		// System.out.println(Ref.fileSansDot(path) + " -> " +
		// getImageDim(path));
		// }
		// long stop = System.currentTimeMillis();
		// System.out.println("It took " + (stop - start) +
		// " milliseconds to get informations about " + paths.length +
		// " images");
	}
}
