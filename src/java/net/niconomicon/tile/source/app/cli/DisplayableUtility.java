package net.niconomicon.tile.source.app.cli;

public class DisplayableUtility extends DisplayableUtilityBase {

	TileExtractor dt;
	ImageToDisplayable toDisp;

	public void printOptions() {
		System.out.println("options: ");
		System.out.println("    either java -jar [jar] " + dt.command + " ...");
		System.out.println("        or java -jar [jar] " + toDisp.command + " ...");
	}

	public DisplayableUtility() {
		// TODO Auto-generated constructor stub
		dt = new TileExtractor();
		toDisp = new ImageToDisplayable();
	}

	public static void main(String[] args) {
		DisplayableUtility utility = new DisplayableUtility();
		if (args.length < 1) {
			utility.printOptions();
			System.exit(0);
		}
		String command = args[0];
		// TODO Auto-generated method stub
		String[] newArgs = new String[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			newArgs[i - 1] = args[i];
		}
		try {
			if (utility.dt.isTheCommand(command)) {
				utility.dt.extractTiles(newArgs);
				return;
			}
			if (utility.toDisp.isTheCommand(command)) {
				utility.toDisp.toDisplayable(newArgs);
				return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		utility.printOptions();
	}
}
