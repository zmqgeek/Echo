package util;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

/**
 * Parse AndroidManifest.xml file.
 * @author echo
 */

public class ManifestParser {
	private final static String NAME = "name";
	private ProcessManifest manifest;

	// The argument can be app id or app path
	public ManifestParser(String s) {
		String apkPath = null;
		try {
			apkPath = AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), Integer.valueOf(s));
		} catch (Exception e) {
			apkPath = s;
		}
		try {
			manifest = new ProcessManifest(apkPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return a list activity names in the app.
	 */
	public List<String> getActivityNames() {
		return manifest.getActivities().stream().map(n -> n.getAttribute(NAME).getValue())
				.map(String.class::cast).map(this::expandClassName)
				.collect(Collectors.toList());
	}

	/**
	 * Return a list of activity XML nodes, in which all the attributes are included.
	 */
	public List<AXmlNode> getActivityNodes() {
		return manifest.getActivities();
	}

	// Return the package name of an app
	public String getPackageName() {
		return manifest.getPackageName();
	}
	
	public static String getPackageName(String appPath) {
		if(! new File(appPath).isFile())
			return null;
		return new ManifestParser(appPath).getPackageName();
	}
	
	// Return the main activities
	public List<String> getLaunchableActivities() {
		return manifest.getLaunchableActivities().stream()
				.map(n -> (String) n.getAttribute(NAME).getValue())
				.map(this::expandClassName)
				.distinct()
				.collect(Collectors.toList());
	}
	
	// Return a main activity.
	// If there are several main activities, return the first one
	public String getLaunchableActivity() {
		List<String> launchableActs = getLaunchableActivities();
		assert launchableActs.size() != 0;
		return launchableActs.get(0);
	}
	
	/**
	 * Generates a full class name from a short class name by appending the
	 * globally-defined package when necessary
	 * 
	 * @param className
	 *            The class name to expand
	 * @return The expanded class name for the given short name
	 */
	private String expandClassName(String className) {
		String packageName = getPackageName();
		if (className.startsWith("."))
			return packageName + className;
		else if (!className.contains("."))
			return packageName + "." + className;
		else
			return className;
	}
}
