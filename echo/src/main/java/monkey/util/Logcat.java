package monkey.util;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.logging.LogEntry;

import util.Config;
import util.ExecuteCommand;

/**
 * This class interacts with logcat.
 * @author echo
 */
public class Logcat {
	// Initialize Logcat class.
	public static void init(AppInfoWrapper info, Env env) {
		Logcat.info = info;
		Logcat.env = env;
	}
	
	// Clean logcat output
	public static void clean() {
		getLogAsString();
	}
	
	// obtain logcat entries
	public static List<LogEntry> getLog() {
		assert env != null;
		return env.driver().manage().logs().get("logcat").getAll();
	}
	
	// obtain logcat entries as a string
	public static String getLogAsString() {
		return String.join("\n", getLog().stream().map(LogEntry::toString).collect(Collectors.toList()));
	}
	
	// whether there are exceptions raised
	// TODO
	// Better way to detect exception
	public static boolean isException(String log) {
		return log.toLowerCase().contains("exception");
	}
	
	private Logcat() {}
	private static AppInfoWrapper info;
	private static Env env;
	private static String cleanCmdDir = String.join(File.separator, Config.v().get(Config.SDK), "platform-tools");
}
