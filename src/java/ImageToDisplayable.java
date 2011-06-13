import java.io.File;
import java.io.IOError;
import java.io.IOException;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.tiling.GenericTileCreator;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class ImageToDisplayable {

	public static final String swSrc = "-i";
	public static final String swDst = "-d";
	public static final String swThr = "-t";
	public static final String swTSize = "-s";
	public static final String swName = "-n";

	public static void printOptions() {
		String name = ImageToDisplayable.class.getName();

		System.out.println("usage : " + name + " -i [srcimage]");
		System.out
				.println("      : " + name + " " + swSrc + " [srcimage] " + swDst + " [destFile] " + swThr + " [nThreads] " + swTSize + " [tileSize]" + swName + " [Disp name/title]");
		System.out.println("");
		System.out.println("options :");
		System.out.println("        : " + swSrc + " : path to the image to transform into a displayable. Mandatory.");
		System.out.println("        All other options are dispensable.");
		System.out
				.println("        : " + swDst + " : the file to which the Displayable should be saved. Defaults to the [srcImage] with a different (" + Ref.ext_db + ") extension.");
		System.out.println("        : " + swThr + " : The number of threads to use to serialize the tiles. Defaults to 4.");
		System.out.println("        : " + swTSize + " : The size of the tile. Default to 192.");
		System.out.println("        : " + swName + " : The name / title of the Displayable.");
		System.out.println("");
		System.out.println("example usage :");
		System.out.println("java -Xmx1024m -Xms128m -jar displayable-creator.jar " + name + " -i /some/path/to/an/image.jpg");
		System.out.println("# will create a Displayable and save it as /some/path/to/an/image" + Ref.ext_db);
	}

	public static int getNumberOrBail(String val) {
		int ret = -1;
		try {
			ret = Integer.parseInt(val);
		} catch (Exception ex) {
			printOptions();
			ex.printStackTrace();
			System.exit(0);
		}
		return ret;
	}

	/**
	 * -i [source image] -d [displayable name(should not exist)] -t [nb of threads to use] -s [size of the tile] -n
	 * [name / title of the displayable]
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length < 2) {
			printOptions();
			return;
		}
		int nThreads = 1;
		int tileSize = GenericTileCreator.defaultTileSize;
		String src = null;
		String dst = null;

		for (int i = 0; i < args.length - 1; i++) {
			String sw = args[i];
			String val = args[i + 1];
			if (sw.equals(swSrc)) {
				src = val;
			}
			if (sw.equals(swDst)) {
				dst = val;
			}
			if (sw.equals(swThr)) {
				nThreads = getNumberOrBail(val);
			}
			if (sw.equals(swTSize)) {
				tileSize = getNumberOrBail(val);
			}
		}

		if (null == src) {
			System.out.println("The image to transform into a Displayable is missing. Please give one.");
			printOptions();
			return;
		}
		nThreads = Math.max(nThreads, 1);

		File fopen = null;
		File fWrite = null;

		try {
			fopen = new File(src);
			if (!fopen.exists()) {
				System.out.println("Could not find this file : [" + src + "]");
				printOptions();
				System.exit(0);
			}
			if (!fopen.canRead()) {
				System.out.println("The program doesn't have the rights to read this file : [" + src + "]");
				printOptions();
				System.exit(0);
			}
		} catch (Exception ex) {
			printOptions();
			ex.printStackTrace();
			System.exit(0);
		}

		if (dst == null) {
			dst = Ref.pathSansFile(src) + Ref.fileSansDot(src) + Ref.ext_db;
			System.out.println("No output file provided. Going to write to " + dst);
		}
		try {
			fWrite = new File(dst);
			if (fWrite.exists()) {
				System.out.println("This file : [" + dst + "] already exists. Please remove it before running this program.");
				printOptions();
				System.exit(0);
			}
			if (!fWrite.createNewFile()) {
				System.out.println("The program could not create this file: [" + dst + "] Please ensure the place is writable.");
				printOptions();
				System.exit(0);
			}
		} catch (Exception ex) {
			printOptions();
			ex.printStackTrace();
			System.exit(0);
		}

		// default behavior:
		// manbus : 64 ~> 40 seconds on second iteration.
		// globcover : 20 ~> 14 seconds on second iteration.
		// String destDir = "/Users/niko/tileSources/bench/";
		// String src = "/Users/niko/tileSources/";
		String title = Ref.fileSansDot(fopen.getAbsolutePath());
		SQliteTileCreatorMultithreaded.loadLib();
		SQliteTileCreatorMultithreaded creator = new SQliteTileCreatorMultithreaded();
		creator.title = title;
		long start, stop;
		System.out.println("Processing " + title + " using " + nThreads + " threads.");
		start = System.nanoTime();
		creator.calculateTiles(fWrite.getAbsolutePath(), fopen.getAbsolutePath(), tileSize, "png", null, nThreads, true, null);
		creator.finalizeFile();
		stop = System.nanoTime();
		System.out.println("### [" + title + "] total_time: " + ((double) (stop - start) / 1000000) + " ms");
	}
}
