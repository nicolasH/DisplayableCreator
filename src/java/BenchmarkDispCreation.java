import java.io.File;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class BenchmarkDispCreation {

	// 4 1 "/Users/niko/tileSources/bench/panorama_180_cervin.jpg" /Users/niko/tileSources/bench/pano.tdb
	// -i /Users/niko/tileSources/bench/panorama_180_cervin.jpg -t 4
	public static void main(String[] args) {
		String[] bench = new String[] { "-i", "", "-d", " ", "-t", "4" };
		String pathSrc = "/Users/niko/tileSources/bench/";
		String pathDst = "/Users/niko/tileSources/bench/disp/";
		String[] images = new String[] { //"45-ESTRECHO_GIBRA_MAR_DE_ALBORAN.jpg",
				// "Great_Wave_off_KanagawaBig.jpg",
				// "iapetus3_cassini_big.jpg", "online_communities_2_large.png",
		// "panorama_180_cervin.jpg"
		"" };
		for (String img : images) {
			bench[1] = pathSrc + img;
			bench[3] = pathDst + img;

			for (int j = 0; j < 4; j++) {
				try {
					File f = new File(bench[3]);
					if (f.exists()) {
						f.delete();
					}
//					ImageToDisplayable toDisp = new ImageToDisplayable();
//					toDisp.toDisplayable(bench);
					System.gc();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
