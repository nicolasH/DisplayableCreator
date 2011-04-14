import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
import net.niconomicon.tile.source.app.viewer.actions.SingleTileLoader;

/**
 * 
 */

/**
 * @author Nicolas Hoibian
 * 
 */
public class OtherTest {

	public static void main(String[] args) throws Exception {
		SQliteTileCreatorMultithreaded.loadLib();
		String tileSourcePath = "/Users/niko/tileSources/serving/full_set_labels2.mdb";
		System.out.println("trying to open the map : " + tileSourcePath);
		File f = new File(tileSourcePath);
		System.out.println("File exists : " + f.exists());
		Connection mapDB = DriverManager.getConnection("jdbc:sqlite:" + tileSourcePath);
		mapDB.setReadOnly(true);
		SingleTileLoader.getPossibleType(mapDB);
	}
}
