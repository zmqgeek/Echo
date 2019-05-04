package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolve the absolute path of Android app. 
 * The format of app name is id_appname.apk.
 * For convenience, the absolute path of app(s) which are going to be analyzed are resolved 
 * according to the given app id(s).  
 * 
 * @author echo
 */
public class AppPathResolver {
	private static final String APPID = "APPID";
	private static String appNameRegex = String.format("(?<%s>\\d+)_.+", APPID);
	/**
	 * Resolve the paths of all the apps in a directory.
	 */
	public static List<String> resolveAppPaths(String dir) {
		return Arrays.stream(new File(dir).listFiles()).filter(f -> f.getName().endsWith("apk"))
				.map(File::getAbsolutePath).distinct().collect(Collectors.toList());
	}
	
	/**
	 * Resolve the paths of the app with given id in a directory.
	 */
	public static String resolveAppPath(String dir, int id) {
		Pattern pattern = Pattern.compile(appNameRegex);
		List<String> appDirs = new ArrayList<>();
		File d = new File(dir);
		for (File f : d.listFiles()) {
			Matcher matcher = pattern.matcher(f.getName());
			if (matcher.matches() && matcher.group(APPID).equals(String.valueOf(id))
					&& f.getName().endsWith("apk"))
				appDirs.add(f.getAbsolutePath());
		}
		if (appDirs.size() == 1)
			return appDirs.get(0);
		else
			return null;
	}

	/**
	 * Resolve the paths of the apps with given ids in a list of integers.
	 */
	public static List<String> resolveAppPaths(String dir, List<Integer> ids) {
		return ids.stream().map(id -> resolveAppPath(dir, id))
				.filter(Objects::nonNull).distinct().collect(Collectors.toList());
	}
	
	/**
	 * Resolve the paths of the apps with given ids in an array.
	 */
	public static List<String> resolveAppPaths(String dir, String[] ids) {
		return prepareAppIds(ids).stream().map(id -> resolveAppPath(dir, id))
				.filter(Objects::nonNull).distinct().collect(Collectors.toList());
	}
	
	/**
	 * Convert app ids accepted from command line.
	 */
	private static List<Integer> prepareAppIds(String[] args) {
		List<Integer> appIds = new ArrayList<>();
		for(String arg : args) {
			try {
				appIds.add(Integer.valueOf(arg));
			} catch (Exception e) {
				// System.out.println(arg + " is not an integer. Skip it.");
			}
		}
		return appIds;
	}
	
	// Test
	public static void main(String[] args) {
		Config.init(null);
		// Config.v().get("");
		System.out.println(resolveAppPath(Config.v().get(Config.APPDIR), 0));
	}
}
