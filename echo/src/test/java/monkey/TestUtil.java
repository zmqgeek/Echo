package monkey;

import java.util.function.BiConsumer;

import monkey.Main;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import monkey.util.TestingOptions;
import util.Config;

/**
 * This class defines utility methods for testing.
 * 
 * @author echo
 */
public class TestUtil {
	/**
	 * Initialize Appium testing
	 */
	public static void initTesting(String id, BiConsumer<AppInfoWrapper, Env> testing) {
		Config.init(null);
		String[] args = new String[] {"-app", id, "-emulator", "Nexus_5_API_19"};
		TestingOptions.v().processOptions(args);
		TestingOptions.v().getAppPaths().stream().map(AppInfoWrapper::new).forEach(i -> Main.testingApp(i, testing));
	}
}
