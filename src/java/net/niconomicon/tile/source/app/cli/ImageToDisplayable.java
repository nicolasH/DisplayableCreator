package net.niconomicon.tile.source.app.cli;

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
public class ImageToDisplayable extends DisplayableUtilityBase {

	public static final String switchSourcePath = "-i";
	public static final String switchDestinationPath = "-d";
	public static final String switchThreadCount = "-t";
	public static final String switchTileSize = "-s";
	public static final String switchName = "-t";
	public static final String switchAlgorithm = "-a";

	public static final String ALGO_PARALLEL = "p";
	public static final String ALGO_MULTITHREADED = "m";
	public static final int DEFAULT_NTHREADS = 4;
	public static final int DEFAULT_TILE_SIZE = GenericTileCreator.defaultTileSize;

	public ImageToDisplayable() {
		this.command = "toDisp";
		this.actionError = "Error creating displayable.";
	}

	public void printOptions() {

		System.out.println("-- Displayable Creation --");
		System.out.println("usage:");
		System.out.println("       java -jar [jar] " + command + " -i [image]");
		System.out.println("       java -jar [jar] " + command + " " + switchSourcePath + " [image] " + switchDestinationPath + " [disp] " + switchThreadCount
				+ " [nThreads] " + switchTileSize + " [tileSize] " + switchName + " [title]");
		System.out.println("");
		System.out.println("options:");
		System.out.println("       " + switchSourcePath + " [image]  : path to the image to transform into a displayable. Mandatory.");
		System.out.println();
		System.out.println("The following are flags optional and can be used to fine tune your displayable:");
		System.out.println("       " + switchDestinationPath
						+ " [disp]   : the file to which the Displayable should be saved. Defaults to the [image] with a different (" + Ref.ext_db
						+ ") extension.");
		System.out.println("       " + switchThreadCount + " [number] : the number of threads to use to serialize the tiles. Defaults to "
				+ DEFAULT_NTHREADS + ".");
		System.out.println("       " + switchTileSize + " [number] : the size of the tile. Default is " + DEFAULT_TILE_SIZE + ".");
		System.out.println("       " + switchAlgorithm + " [p|m]    : the used algorithm either [" + ALGO_PARALLEL + "]arallel (default) or ["
				+ ALGO_MULTITHREADED + "]ultithreaded.");
		System.out.println("       " + switchName + " [name]   : the title of the Displayable.");
		System.out.println("");
		System.out.println("example usage :");
		System.out.println("java -Xmx1024m -Xms128m -jar displayable-creator.jar " + command + " -i /some/path/to/an/image.jpg");
		System.out.println("# will create a Displayable and save it as /some/path/to/an/image" + Ref.ext_db);
	}

	/**
	 * -i [source image] -d [displayable name(should not exist)] -t [nb of
	 * threads to use] -s [size of the tile] -n [name / title of the
	 * displayable]
	 * 
	 * @param arguments
	 * @throws InterruptedException
	 */
	public void toDisplayable(String[] arguments) throws InterruptedException, IOException {
		ImageToDisplayable toDisp = new ImageToDisplayable();
		if (arguments.length < 2) {
			toDisp.printOptions();
			return;
		}
		int nThreads = DEFAULT_NTHREADS;
		int tileSize = DEFAULT_TILE_SIZE;
		String sourcePath = null;
		String destinationPath = null;
		String algo = ALGO_PARALLEL;
		for (int i = 0; i < arguments.length - 1; i++) {
			String sw = arguments[i];
			String val = arguments[i + 1];
			if (sw.equals(switchSourcePath)) {
				sourcePath = val;
			}
			if (sw.equals(switchDestinationPath)) {
				destinationPath = val;
			}
			if (sw.equals(switchThreadCount)) {
				nThreads = toDisp.getNumberOrBail(val);
			}
			if (sw.equals(switchTileSize)) {
				tileSize = toDisp.getNumberOrBail(val);
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
			toDisp.printOptions();
			return;
		}
		nThreads = Math.max(nThreads, 1);

		File fopen = null;
		File fWrite = null;

		fopen = checkReadOrDie(sourcePath);
		if (destinationPath == null) {
			destinationPath = Ref.pathSansFile(sourcePath) + Ref.fileSansDot(sourcePath) + Ref.ext_db;
			System.out.println("No output file provided. Going to write to " + destinationPath);
		}
		fWrite = checkWriteOrDie(destinationPath);
		
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