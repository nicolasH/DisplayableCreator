import java.io.File;
import java.io.IOException;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.tiling.GenericTileCreator;
import net.niconomicon.tile.source.app.tiling.SQLiteDisplayableCreatorMoreParallel;
import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class ImageToDisplayable {

	public static final String switchSourcePath = "-i";
	public static final String switchDestinationPath = "-d";
	public static final String switchThreadCount = "-t";
	public static final String switchTileSize = "-s";
	public static final String switchName = "-n";
	public static final String switchAlgorithm = "-a";

	public static final String ALGO_PARALLEL = "p";
	public static final String ALGO_MULTITHREADED = "m";
	public static final int DEFAULT_NTHREADS = 4;
	public static final int DEFAULT_TILE_SIZE = GenericTileCreator.defaultTileSize;

	public static void printOptions() {
		String name = ImageToDisplayable.class.getName();

		System.out.println("usage : " + name + " -i [srcimage]");
		System.out.println("      : " + name + " " + switchSourcePath + " [srcimage] " + switchDestinationPath + " [destFile] " + switchThreadCount
				+ " [nThreads] " + switchTileSize + " [tileSize]" + switchName + " [Disp name/title]");
		System.out.println("");
		System.out.println("options :");
		System.out.println("         " + switchSourcePath + " : path to the image to transform into a displayable. Mandatory.");
		System.out.println("        All other flags are optional.");
		System.out
				.println("          " + switchDestinationPath
						+ " : the file to which the Displayable should be saved. Defaults to the [srcImage] with a different (" + Ref.ext_db
						+ ") extension.");
		System.out.println("          " + switchThreadCount + " : The number of threads to use to serialize the tiles. Defaults to "
				+ DEFAULT_NTHREADS + ".");
		System.out.println("          " + switchTileSize + " : The size of the tile. Default to " + DEFAULT_TILE_SIZE + ".");
		System.out.println("          " + switchAlgorithm + " : The used algorithm either [" + ALGO_PARALLEL + "]arallel, default or ["
				+ ALGO_MULTITHREADED + "]ultithreaded.");
		System.out.println("          " + switchName + " : The name / title of the Displayable.");
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
	 * -i [source image] -d [displayable name(should not exist)] -t [nb of
	 * threads to use] -s [size of the tile] -n [name / title of the
	 * displayable]
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length < 2) {
			printOptions();
			return;
		}
		int nThreads = DEFAULT_NTHREADS;
		int tileSize = DEFAULT_TILE_SIZE;
		String sourcePath = null;
		String destinationPath = null;
		String algo = ALGO_PARALLEL;
		for (int i = 0; i < args.length - 1; i++) {
			String sw = args[i];
			String val = args[i + 1];
			if (sw.equals(switchSourcePath)) {
				sourcePath = val;
			}
			if (sw.equals(switchDestinationPath)) {
				destinationPath = val;
			}
			if (sw.equals(switchThreadCount)) {
				nThreads = getNumberOrBail(val);
			}
			if (sw.equals(switchTileSize)) {
				tileSize = getNumberOrBail(val);
			}
			if (sw.equals(switchAlgorithm)) {
				if (val.equals(ALGO_PARALLEL)) {
					algo = ALGO_PARALLEL;
				} else {
					if (val.equals(ALGO_MULTITHREADED)) {
						algo = ALGO_MULTITHREADED;
					}
				}
			}
		}
		System.out.println("Algo: " + algo);
		if (null == sourcePath) {
			System.out.println("The image to transform into a Displayable is missing. Please give one.");
			printOptions();
			return;
		}
		nThreads = Math.max(nThreads, 1);

		File fopen = null;
		File fWrite = null;

		try {
			fopen = new File(sourcePath);
			if (!fopen.exists()) {
				System.out.println("Could not find this file : [" + sourcePath + "]");
				printOptions();
				System.exit(0);
			}
			if (!fopen.canRead()) {
				System.out.println("The program doesn't have the rights to read this file : [" + sourcePath + "]");
				printOptions();
				System.exit(0);
			}
		} catch (Exception ex) {
			printOptions();
			ex.printStackTrace();
			System.exit(0);
		}

		if (destinationPath == null) {
			destinationPath = Ref.pathSansFile(sourcePath) + Ref.fileSansDot(sourcePath) + Ref.ext_db;
			System.out.println("No output file provided. Going to write to " + destinationPath);
		}
		try {
			fWrite = new File(destinationPath);
			if (fWrite.exists()) {
				System.out.println("Error creating displayable.");
				printOptions();
				System.out.println("!!!! This file : [" + destinationPath + "] already exists. Please remove it before running this program.");
				System.exit(0);
			}
			if (!fWrite.createNewFile()) {
				System.out.println("Error creating displayable.");
				printOptions();
				System.out.println("!!!! The program could not create this file: [" + destinationPath + "] Please ensure the place is writable.");
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
		// SQliteTileCreatorMultithreaded.loadLib();
		// SQliteTileCreatorMultithreaded creator = new
		// SQliteTileCreatorMultithreaded();
		long start, stop;
		start = System.nanoTime();
		if (algo == ALGO_PARALLEL) {
			SQLiteDisplayableCreatorMoreParallel.loadLib();
			SQLiteDisplayableCreatorMoreParallel creator = new SQLiteDisplayableCreatorMoreParallel();
			creator.title = title;
			System.out.println("Processing " + title + " using " + nThreads + " threads.");
			creator.calculateTiles(fWrite.getAbsolutePath(), fopen.getAbsolutePath(), tileSize, "png", null, nThreads, true, null);
			creator.finalizeFile();
		} else {
			SQliteTileCreatorMultithreaded.loadLib();
			SQliteTileCreatorMultithreaded creator = new SQliteTileCreatorMultithreaded();
			creator.title = title;
			System.out.println("Processing " + title + " using " + nThreads + " threads.");
			creator.calculateTiles(fWrite.getAbsolutePath(), fopen.getAbsolutePath(), tileSize, "png", null, nThreads, true, null);
			creator.finalizeFile();
		}
		stop = System.nanoTime();
		System.out.println("### [" + title + "] total_time: " + ((double) (stop - start) / 1000000) + " ms (algo: " + algo + " threads: " + nThreads
				+ " tile size: " + tileSize + " )");
	}
}