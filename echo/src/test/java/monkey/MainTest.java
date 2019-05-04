package monkey;

import java.io.File;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;

import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;
import monkey.Main;
import monkey.event.DragEvent;
import monkey.event.TapEvent;
import monkey.event.Throttle;
import monkey.event.ThrottleEvent;
import monkey.exception.TestFailureException;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import monkey.util.TestingOptions;
import reduction.event.CheckActivityEvent.ActivityChecker;
import soot.toolkits.scalar.Pair;
import util.Config;
import util.AndroidKeyCodeWrapper;
import util.ManifestParser;
import util.PointF;

public class MainTest {
	@Test
	public void test1() {
		Main.main(new String[] {"-emulator", "Nexus_5_API_19", "-app", "0"});
	}

	@Test
	public void test1_2() {
		Main.main(new String[] {"-emulator", "Nexus_5_API_19_2", "-app", "0"});
	}

	@Test
	public void test2() {
		Main.main(new String[] {"-emulator", "Nexus_5_API_19", "-app", "0", "1"});
	}

	@Test
	public void test3() {
		Main.main(new String[] { "-app", "0", "1", "-emulator", "Nexus_5_API_19"});
	}

	@Test
	public void test4() {
		Config.init(null);
		String[] args = new String[] {"-app", "0", "-emulator", "Nexus_5_API_19" };
		TestingOptions.v().processOptions(args);
		TestingOptions.v().getAppPaths().stream().map(AppInfoWrapper::new)
		.forEach(i -> {
			Main.testingApp(i, (info, env) -> {
				AndroidDriver<AndroidElement> d = env.driver();
				d.closeApp();
				d.removeApp(info.getPkgName());
			});
		});
	}

	@Test
	public void test5() {
		Config.init(null);
		String[] args = new String[] {"-app", "0", "-emulator", "Nexus_5_API_19" };
		ManifestParser manifestParser = new ManifestParser("1");
		System.out.println("# pkg name: " + manifestParser.getPackageName());
		System.out.println("# activity name: " + manifestParser.getLaunchableActivity());
		TestingOptions.v().processOptions(args);
		TestingOptions.v().getAppPaths().stream().map(AppInfoWrapper::new)
		.forEach(i -> {
			Main.testingApp(i, (info, env) -> {
				AndroidDriver<AndroidElement> d = env.driver();
				d.startActivity(new Activity("arity.calculator", "calculator.Calculator"));
				d.closeApp();
				d.removeApp(info.getPkgName());
			});
		});
	}

	/**
	 * Test return from unwanted app
	 * Press back key to return
	 */
	@Test
	public void test6() {
		initTesting("0", (i, env) -> {
			AndroidDriver<AndroidElement> d = env.driver();
			d.startActivity(new Activity("arity.calculator", "calculator.Calculator"));
			if(! i.contains(d.currentActivity()))
				d.pressKeyCode(AndroidKeyCode.BACK);
		});
	}

	/**
	 * Launch the app with package name and launchable activity name 
	 */
	@Test
	public void test7() {
		Main.testingApp("com.example.yzhan.startmode", ".MainActivity", (p, env) -> {
			AndroidDriver<AndroidElement> d = env.driver();
			Dimension dimension = d.manage().window().getSize();
			System.out.println("# Window height: " + dimension.height);
			System.out.println("# Window width: " + dimension.width);
			d.closeApp();
		});
	}

	/**
	 * Test click button with coordinate
	 */
	@Test
	public void test8() {
		Throttle.v().init(500);
		System.out.println("Event throttle " + Throttle.v().getDuration());
		Main.testingApp("com.android.gesture.builder", ".GestureBuilderActivity", (p, env) -> {
			AndroidDriver<AndroidElement> d = env.driver();
			Dimension dimension = d.manage().window().getSize();
			System.out.println("# Window height: " + dimension.height);
			System.out.println("# Window width: " + dimension.width); 
			// click the "Add gesture" button
			new TouchAction(d).tap(280,  1700).waitAction(Throttle.v().getDuration()).perform();
			//  swipe
			new TouchAction(d).press(518, 518).moveTo(200, 200).release().waitAction(Throttle.v().getDuration()).perform();
			new TouchAction(d).press(520, 963).moveTo(-100, -100).release().waitAction(Throttle.v().getDuration()).perform();
			// multi couch
			TouchAction actOne = new TouchAction(d).press(357, 539).moveTo(-100, -100).waitAction(Throttle.v().getDuration()).release();
			TouchAction actTwo = new TouchAction(d).press(470, 583).moveTo(100, 100).waitAction(Throttle.v().getDuration()).release();
			new MultiTouchAction(d).add(actOne).add(actTwo).perform();
			d.closeApp();
		});
	}

	/**
	 * Test obtain screenshot and save to a file in current directory
	 */
	@Test
	public void test9() {
		initTesting("0", (i, env) -> {
			try {
				AndroidDriver<AndroidElement> d = env.driver();
				File screenshot = d.getScreenshotAs(OutputType.FILE);
				File dest = new File(String.join(File.separator, "." , screenshot.getName()));
				FileUtils.copyFile(screenshot, dest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Test obtaining window hierarchy of current screen
	 * The string is a xml file.
	 */
	@Test
	public void test10() {
		initTesting("0", (i, env) -> {
			AndroidDriver<AndroidElement> d = env.driver();
			System.out.println(d.getPageSource());
			d.closeApp();
		});
	}

	/**
	 * Test throttle time.
	 */
	@Test
	public void test11() {
		Config.init(null);
		String[] args = new String[] {"-app", "0", "-emulator", "Nexus_5_API_19", "-throttle", "500"};
		TestingOptions.v().processOptions(args);
		TestingOptions.v().getAppPaths().stream().map(AppInfoWrapper::new).forEach(i -> Main.testingApp(i, (info, env) -> {
			AndroidDriver<AndroidElement> d = env.driver();
			new ThrottleEvent();
			d.closeApp();
		}));
	}

	/**
	 * Test the class AndroidKeyCodeWrapper.
	 */
	@Test
	public void test12() {
		List<Pair<Integer, String>> list = AndroidKeyCodeWrapper.v().getList();
		for(int i = 0; i < list.size(); i++) {
			Pair<Integer, String> p = list.get(i);
			assert i == p.getO1();
			System.out.println(i + ", " + p.getO1() + ", " + p.getO2());
		}
		System.out.println("Size: " + list.size());
	}

	/**
	 * Test randomly key code generation.
	 */
	@Test
	public void test13() {
		SecureRandom random = new SecureRandom();
		for(int i = 0; i < 10000; i++) {
			int r = random.nextInt(AndroidKeyCodeWrapper.v().size());
			Pair<?, ?> p = AndroidKeyCodeWrapper.v().get(r);
			System.out.println(p.getO1() + ", " + p.getO2());
		}
		System.out.println("Size: " + AndroidKeyCodeWrapper.v().size());
	}

	/**
	 * Test click button with coordinate
	 * @see test8()
	 * Use longPress() rather than press()
	 */
	@Test
	public void test14() {
		Throttle.v().init(500);
		System.out.println("Event throttle " + Throttle.v().getDuration());
		Main.testingApp("com.android.gesture.builder", ".GestureBuilderActivity", (p, env) -> {
			try {
				new TapEvent().addFrom(0, new PointF(280, 1700)).injectEvent(null, env);
				new DragEvent().addFromTo(0, new PointF(env.width() / 2, env.height() / 2), new PointF(100, 100)).injectEvent(null, env);
				new DragEvent().addFromTo(0, new PointF(398, 1388), new PointF(365, 1371)).injectEvent(null, env);
			} catch (TestFailureException e) {
				// TODO: handle exception
			}
		});
	}

	/**
	 * Test read regular expressions from file.
	 */
	@Test
	public void test15() {
		Config.init(null);
		System.out.println(ActivityChecker.isErrorAndroidActivity("com.android.settings.inputmethod.test"));
		System.out.println(ActivityChecker.isErrorAndroidActivity("com.android.settings.inputmethod."));
		System.out.println(ActivityChecker.isErrorAndroidActivity("com.android.settings.applications.test"));
		System.out.println(ActivityChecker.isErrorAndroidActivity("com.android.providers.media.test"));
	}
	
	/**
	 * Initialize Appium testing
	 */
	private void initTesting(String id, BiConsumer<AppInfoWrapper, Env> testing) {
		Config.init(null);
		String[] args = new String[] {"-app", id, "-emulator", "Nexus_5_API_19"};
		TestingOptions.v().processOptions(args);
		TestingOptions.v().getAppPaths().stream().map(AppInfoWrapper::new).forEach(i -> Main.testingApp(i, testing));
	}
}
