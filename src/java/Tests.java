import java.io.File;

import javax.swing.JProgressBar;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.SQliteTileCreator;

/**
 * 
 */

/**
 * @author niko
 * 
 */
public class Tests {

	public static void printOptions() {
		System.out.println("usage : tests [nExtraThreads] [nIters] ");
		System.out.println("if nExtraThreads == 0 then the single threaded version of the tile creator will be used");
		System.out.println("if nExtraThreads < 0 then both the single version and a multithreaded version of the tile creator will be used with (-nExtraThreads as number of threads).");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		printOptions();
		int nThreads = Integer.parseInt(args[0]);
		int nIters = Integer.parseInt(args[1]);
		String[] files;
		files = new String[] { "globcover_MOSAIC_H.png" };
		files = new String[] { "manbus.pdf" };
		// default behavior:
		// manbus : 64 ~> 40 seconds on second iteration.
		// globcover : 20 ~> 14 seconds on second iteration.
		String destDir = "/Users/niko/tileSources/bench/";
		String src = "/Users/niko/tileSources/";

		SQliteTileCreator creator = new SQliteTileCreator();
		long start, stop;
		int count = 10;
		for (int i = 0; i < count; i++) {
			for (String file : files) {
				creator.title = file.substring(0, file.lastIndexOf("."));
				System.out.println("Processing " + creator.title);
				String dstFile = destDir + creator.title + Ref.ext_db;
				File f = new File(dstFile);
				if (f.exists()) {
					System.out.println("removing this file : " + dstFile);
					f.delete();
				}
				start = System.nanoTime();
				creator.calculateTiles(destDir + creator.title + Ref.ext_db, src + file, 192, "png", new JProgressBar());
				creator.finalizeFile();
				stop = System.nanoTime();
				System.out.println("total_time: " + ((double) (stop - start) / 1000000) + " ms");
			}
		}

	}
}
