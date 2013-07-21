package net.niconomicon.tile.source.app.cli;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.niconomicon.tile.source.app.Ref;
import net.niconomicon.tile.source.app.tiling.SQLiteDisplayableCreatorMoreParallel;
import net.niconomicon.tile.source.app.tools.DisplayableSourceBase;

public class TileExtractor extends DisplayableUtilityBase {

	public void printOptions() {
		System.out.println("-- Displayable Tile Extraction --");
		System.out.println("usage: java -jar [jar] "+command+" [x] [y] [z] [file.disp]");
	}

	public TileExtractor() {
		this.command = "extract";
	}

	public void extractTiles(String[] arguments) {
		TileExtractor tileDumper = new TileExtractor();
		// TODO Auto-generated method stub
		if (arguments.length != 4) {
			tileDumper.printOptions();
			System.exit(0);
		}
		File fWrite = null;
		File fOpen = null;
		int[] coords = new int[] { 0, 0, 0 };
		for (int n = 0; n < coords.length; n++) {
			coords[n] = tileDumper.getNumberOrBail(arguments[n]);
		}
		String sourcePath = arguments[3];
		fOpen = checkReadOrDie(sourcePath);

		int x, y, z = 0;
		x = coords[0];
		y = coords[1];
		z = coords[2];
		String destinationPath = Ref.pathSansFile(sourcePath) + Ref.fileSansDot(sourcePath) + "." + Ref.getKey(x, y, z) + ".png";
		System.out.println("No output file provided. Going to write to " + destinationPath);

		fWrite = checkWriteOrDie(destinationPath);

		SQLiteDisplayableCreatorMoreParallel.loadLib();
		DisplayableSourceBase source = new DisplayableSourceBase(fOpen.getAbsolutePath());
		BufferedImage img = source.getImage(x, y, z);
		try {
			ImageIO.write(img, "PNG", fWrite);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}
