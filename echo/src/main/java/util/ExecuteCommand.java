package util;

import java.io.File;
import java.util.Scanner;

/**
 * Execute command.
 * @author echo
 */
public class ExecuteCommand {
	/**
	 * Execute a global command or a command in a specific directory.
	 * If global command is executed, cmdDir is null.
	 * If a command in given directory is executed, cmdDir gives the directory; cmd gives the command file.
	 * 
	 * opt gives the options of the command being executed.
	 */
	public static String exec(String cmdDir, String cmd, String... opts) {
		File cmdDirFile = null;
		String completeCMD = null;
		if(cmdDir == null) {
			completeCMD = cmd;
		} else {
			cmdDirFile = new File(cmdDir);
			assert cmdDirFile.isDirectory();
			File cmdFile = new File(String.join(File.separator, cmdDir, cmd));
			assert cmdFile.isFile();
			completeCMD = cmdFile.getAbsolutePath();
		}

		// Construct the complete command
		String[] newCommands = new String[opts.length + 1];
		newCommands[0] = completeCMD;
		for(int i = 0; i < opts.length; i++)
			newCommands[i + 1] = opts[i];

		// Run the command
		ProcessBuilder builder = new ProcessBuilder(newCommands);
		if(cmdDir == null)
			builder.directory(null);
		else
			builder.directory(cmdDirFile.getAbsoluteFile());
		Process p = null;
		try {
			p = builder.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// Prepare the results
		Scanner scanner = new Scanner(p.getInputStream());
		StringBuilder stringBuilder = new StringBuilder();
		while(scanner.hasNextLine()) {
			stringBuilder.append(scanner.nextLine());
			stringBuilder.append("\n");
		}
		scanner.close();
		return stringBuilder.toString();
	}
}
