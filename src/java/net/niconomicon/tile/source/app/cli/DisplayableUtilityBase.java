package net.niconomicon.tile.source.app.cli;

import java.io.File;

import net.niconomicon.tile.source.app.Ref;

public abstract class DisplayableUtilityBase {

	public String actionError;
	public String command;

	public void printOptions() {}

	public boolean isTheCommand(String command) {
		return this.command.equals(command);
	}

	public int getNumberOrBail(String val) {
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

	public File checkReadOrDie(String sourcePath) {
		File fopen = null;
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
		return fopen;
	}

	public File checkWriteOrDie(String destinationPath) {
		File fWrite = null;
		try {
			fWrite = new File(destinationPath);
			if (fWrite.exists()) {
				System.out.println(actionError);
				printOptions();
				System.out.println("!!!! This file : [" + destinationPath + "] already exists. Please remove it before running this program.");
				System.exit(0);
			}
			if (!fWrite.createNewFile()) {
				System.out.println(actionError);
				printOptions();
				System.out.println("!!!! The program could not create this file: [" + destinationPath + "] Please ensure the place is writable.");
				System.exit(0);
			}
		} catch (Exception ex) {
			printOptions();
			ex.printStackTrace();
			System.exit(0);
		}
		return fWrite;
	}

}
