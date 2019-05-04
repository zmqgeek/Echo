package monkey;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;

import org.jgrapht.graph.DirectedPseudograph;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import monkey.event.Event;
import monkey.exception.TestFailureException;
import monkey.random.RandomEventSource;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import monkey.util.Logcat;
import monkey.util.TestingOptions;
import reduction.DijkstraShortestPathFinder;
import reduction.EventCollector;
import reduction.PathEventCollector;
import reduction.PathFinder;
import reduction.TTGReduction;
import reduction.event.CheckLayoutEvent;
import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import reduction.ttg.TestingTraceGraph;
import reduction.ttg.node.NormalStateFactory;
import reduction.util.TTGReductionHelper;
import util.Config;
import util.Log;
import util.Timer;
import util.graph.TTGWriter;

/**
 * @author echo
 * The main class for Appium testing
 */

public class Main {
	private static final String APPIUM_URL_TEMPLATE = "http://0.0.0.0:%d/wd/hub";

	public static void main(String[] args) {
		Config.init(null);
		TestingOptions.v().processOptions(args);
		// Test one app for each time
		AppInfoWrapper appInfo = new AppInfoWrapper(TestingOptions.v().getAppPaths().get(0));
		testingApp(appInfo);
		if(TestingOptions.v().isReplay()) {
			System.out.println("Try to replay the error.");
			replay(appInfo, TestingTraceGraph.v().getTTG());
		}
	}

	/**
	 * Setup testing session
	 */
	private static Env setUp(AppInfoWrapper appInfo) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
//		capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
		capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, appInfo.getPkgName());
		capabilities.setCapability(MobileCapabilityType.APP, appInfo.getAppPath());
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
		AndroidDriver<AndroidElement> driver = null;
		try {
			final String appiumURL = String.format(APPIUM_URL_TEMPLATE, TestingOptions.v().getPortNumber());
			Log.println("## Appium URL: " + appiumURL);
			System.out.println("## Appium URL: " + appiumURL);
			driver = new AndroidDriver<>(new URL(appiumURL), capabilities);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		Env env = new Env(driver);
		// init Logcat
		Logcat.init(appInfo, env);
		return env;
	}

	/**
	 * Setup the testing with package name and launchable activity name
	 */
	private static Env setUp(String pkgName, String actName) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
		capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, pkgName);
		capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, actName);
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
		AndroidDriver<AndroidElement> driver = null;
		try {
			driver = new AndroidDriver<>(new URL(APPIUM_URL_TEMPLATE), capabilities);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		Env env = new Env(driver);
		// init Logcat
		Logcat.init(null, env);
		return env;
	}

	// Reset testing tool
	private static void reset() {
		// discard old TTG and its nodes
		TestingTraceGraph.reset();
		NormalStateFactory.reset();
	}

	// Testing an app. 
	public static void testingApp(AppInfoWrapper appInfo) {
		Env env = setUp(appInfo);
		final int timesForTesting = 20;
		int i = 0;
		for( ; i < timesForTesting; i++) {
			env.driver().resetApp();
			// Wait for app loading
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Test app " + appInfo.getPkgName());
			reset();
			// clean the output
			appInfo.cleanOutputDirectory();
			Timer timer = new Timer();
			// Clean old logcat output.
			Logcat.clean();
			try(PrintStream printStream = new PrintStream(new File(appInfo.getOutputDirectory(), "output.txt"))) {
				timer.start();
				Log.init(printStream);
				System.out.println("Options: ");
				System.out.println(TestingOptions.v().toString());
				// Run random testing
				RandomEventSource eventSource = new RandomEventSource(appInfo, env, TestingOptions.v().getSeed());
				env.addEventSource(eventSource);
				eventSource.runTestingCycles();
				timer.stop();
				Log.println("# Time: " + timer.getDurationInSecond() + " s.");
				// Serialize the graph
				Log.println("# Serialize the TTG.");
				File graphFile = new File(appInfo.getOutputDirectory(), "graph");
				TTGWriter.serializeTTG(graphFile);
				Log.println("#CheckLayoutEvent: " + CheckLayoutEvent.numberOfCheckLayoutEvent);
				Log.println("#Node: " + TestingTraceGraph.v().vertexSet().size());
				Log.println("#Edge: " + TestingTraceGraph.v().edgeSet().size());
				Log.println("#Events: " + TTGReductionHelper.getEvents(TestingTraceGraph.v().getTTG()));
			} catch (TestFailureException e) {
				// Catch the TestFailureException. Retest the app with an randomly generated seed.
				System.out.println("# Errors occurs during testing. Retesting the app with a randomly generated seed again. ");
				TestingOptions.v().setRandomSeed();
				reset();
				continue;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			break;
		}
		System.out.println("## Testing cycle: " + i);
		env.driver().closeApp();
	}

	/**
	 * App testing is implemented as a function interface.
	 */
	public static void testingApp(AppInfoWrapper appInfo, BiConsumer<AppInfoWrapper, Env> testing) {
		Env env = setUp(appInfo);
		env.driver().resetApp();
		// Wait for app loading
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		testing.accept(appInfo, env);
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		env.driver().closeApp();
	}

	/**
	 * Launch the app with package name and launchable activity name
	 */
	public static void testingApp(String pkgName, String actName, BiConsumer<String, Env> testing) {
		Env env = setUp(pkgName, actName);
		env.driver().resetApp();
		testing.accept(pkgName, env);
	}

	// Replay the bug with given path finder and event collector.
	public static void replay(AppInfoWrapper appInfo, DirectedPseudograph<TTGNode, TTGEdge> graph, 
			Class<? extends PathFinder> pathFinderClz, Class<? extends EventCollector> eventCollectorClz) {
		int before = TTGReductionHelper.getEvents(graph).size();
		List<Event> replayEvents = TTGReduction.reduce(graph, pathFinderClz, eventCollectorClz);
		Queue<Event> replayEventQueue = TTGReductionHelper.getEventQueueForReplay(replayEvents);
		int after = replayEvents.size();
		System.out.println("# Events before reduction: " + before);
		System.out.println("# Events after reduction: " + after);
		System.out.println("Replay events:");
		replayEvents.forEach(System.out::println);
		if(replayEvents.isEmpty()) {
			System.out.println("# No bug found during testing.");
			return;
		}
		Timer timer = new Timer();
		testingApp(appInfo, (info, env) -> {
			// Clean old logcat output
			Logcat.clean();
			timer.start();
			while(! replayEventQueue.isEmpty()) {
				Event event = replayEventQueue.peek();
				assert event != null;
				try {
					System.out.println(event);
					event.injectEvent(appInfo, env);
				} catch (TestFailureException e) {
					System.out.println("Test failure occurs during replaying. ");
				}
				catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				replayEventQueue.remove();
			}
		});
		timer.stop();
		System.out.println("# Finish replay.");
		System.out.println("# Time: " + timer.getDurationInSecond() + " s.");
	}

	// Replay the bug with the DijkstraShortestPathFinder and PathEventCollector.
	public static void replay(AppInfoWrapper appInfo, DirectedPseudograph<TTGNode, TTGEdge> graph) {
		replay(appInfo, graph, DijkstraShortestPathFinder.class, PathEventCollector.class);
	}
}
