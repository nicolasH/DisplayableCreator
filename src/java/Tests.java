import java.io.File;

import javax.swing.JProgressBar;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.SQliteTileCreator;
import net.niconomicon.tile.source.app.SQliteTileCreatorMultithreaded;

/**
 * 
 */

/**
 * @author niko
 * 
 */
public class Tests {

	public static void printOptions() {
		System.out.println("usage : tests [nThreads] [nIters] [srcimage] [destFile]");
		System.out.println("[nThread] will be the number of threads used to serialize the tiles. " );
		System.out.println("          The main thread will open,resize the image and write the tiles to the storage. If <= 1 will be replaced by 1.");
		System.out.println("[nIters] will be the number of times the image will be open, tiled and serialized." );
		System.out.println("          if <=1 will be replaced by 1. If >1 the iteration will be added to the destination file name.");
		
		System.out.println("[srcImage] the image you want to tile.");
		System.out.println("[srcImage] where the tiles image will be stored.");
		System.out.println("example usage :");
		System.out.println("java -Xmx1024m -jar bla.jar Tile 1 1 ~niko/Big.jpg /tmp/test.tdb");
		
		System.out.println("java -Xmx1024m -jar bla.jar Tile 1 1 ~niko/Big.jpg /tmp/test.tdb");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args.length<4){
			printOptions();
		}
		int nThreads = 1;
		int nIters = 1;
		try{
		nThreads = Integer.parseInt(args[0]);
		nIters = Integer.parseInt(args[1]);
		}catch (Exception ex) {
			printOptions();
			ex.printStackTrace();
		}
		String[] files;
		files = new String[] { "globcover_MOSAIC_H.png" };
		files = new String[] { "manbus.pdf" };
		// default behavior:
		// manbus : 64 ~> 40 seconds on second iteration.
		// globcover : 20 ~> 14 seconds on second iteration.
		String destDir = "/Users/niko/tileSources/bench/";
		String src = "/Users/niko/tileSources/";

		SQliteTileCreatorMultithreaded creator = new SQliteTileCreatorMultithreaded();
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
				creator.calculateTiles(destDir + creator.title + Ref.ext_db, src + file, 192, "png", new JProgressBar(),nThreads);
				creator.finalizeFile();
				stop = System.nanoTime();
				System.out.println("total_time: " + ((double) (stop - start) / 1000000) + " ms");
			}
		}

	}
}
