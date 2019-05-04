package monkey.util;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import util.AppPathResolver;
import util.Config;
import util.ManifestParser;

/**
 * This class includes info of an app
 * 
 * @author echo
 */
public class AppInfoWrapper {
	public AppInfoWrapper(int id) {
		appPath = AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), id);
		init();
	}

	public AppInfoWrapper(String appPath) {
		this.appPath = appPath;
		init();
	}

	private void init() {
		String[] temp = appPath.split(Pattern.quote(File.separator));
		assert temp.length > 0;
		appFileName = temp[temp.length - 1];
		appName = appFileName.split(".apk")[0];
		ManifestParser parser = new ManifestParser(appPath);
		pkgName = parser.getPackageName();
		activityNames = parser.getActivityNames();
		assert ! activityNames.isEmpty();
		launchableActivities = parser.getLaunchableActivities();
		assert ! launchableActivities.isEmpty();
		outputDirectory = String.join(File.separator, Config.v().get(Config.OUTPUT), appName);
	}

	public String getAppPath() {
		return appPath;
	}

	public String getAppName() {
		return appName;
	}

	public String getAppFileName() {
		return appFileName;
	}

	public String getPkgName() {
		return pkgName;
	}

	public List<String> getActivityNames() {
		return activityNames;
	}

	public List<String> getLaunchableActivities() {
		return launchableActivities;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	public boolean contains(String activityName) {
		return activityNames.contains(activityName);
	}

	// Clean output directory
	public void cleanOutputDirectory() {
		File outputDir = new File(outputDirectory);
		if(outputDir.exists() && outputDir.isDirectory()) {
			// Remove the directory tree
			System.out.println("# Remove old output directory. ");
			for(String s : outputDir.list()) {
				File f = new File(outputDir.getPath(), s);
				f.delete();
			}
			outputDir.delete();
		} 
		outputDir.mkdirs();
	}

	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o instanceof AppInfoWrapper) {
			AppInfoWrapper another = (AppInfoWrapper) o;
			return this.appPath.equals(another.appPath);
		}
		return false;			
	}

	public int hashCode() {
		return appPath.hashCode();
	}

	private String appPath;
	private String appName;
	private String appFileName;
	private String pkgName;
	private String outputDirectory;
	// Activity names that are in the apps
	private List<String> activityNames;
	// The entry activity names
	private List<String> launchableActivities;
}
