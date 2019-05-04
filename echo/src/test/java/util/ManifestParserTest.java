package util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ManifestParserTest {

	@Test
	public void test1() {
		Config.init(null);
		ManifestParser parser = new ManifestParser(AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), 0));
		parser.getActivityNames().forEach(System.out::println);
	}

	@Test
	public void test2() {
		Config.init(null);
		ManifestParser parser = new ManifestParser(AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), 1));
		parser.getActivityNames().forEach(System.out::println);
	}

	/*@Test
	public void test3() {
		ManifestParser.main(new String[] {"1", "2", "3"});
	}

	@Test
	public void test4() {
		ManifestParser.main(new String[] {"all"});
	}
	 */
	@Test
	public void test5() {
		Config.init(null);
		ManifestParser parser = new ManifestParser(AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), 9));
		parser.getActivityNames().forEach(System.out::println);
	}

	@Test
	public void test6() {
		Config.init(null);
		for(int i = 0; i < 67; i++) {
			System.out.println("# " + i);
			String appPath = AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), i);
			if(appPath == null)
				continue;
			ManifestParser parser = new ManifestParser(appPath);
			parser.getLaunchableActivities().forEach(System.out::println);
		}
	}

	@Test
	public void test7() {
		Config.init(null);
		for(int i = 0; i < 67; i++) {
			System.out.println("# " + i);
			String appPath = AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), i);
			if(appPath == null)
				continue;
			ManifestParser parser = new ManifestParser(appPath);
			System.out.println(parser.getLaunchableActivity());
		}
	}

	@Test
	public void test8() {
		Config.init(null);
		System.out.println("# " + 48);
		String appPath = AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), 48);
		if(appPath == null)
			return;
		ManifestParser parser = new ManifestParser(appPath);
		parser.getActivityNames().forEach(System.out::println);
		parser.getLaunchableActivities().forEach(System.out::println);
	}

	/**
	 * Obtain the buggy apps
	 */
	@Test 
	public void test9() {
		Config.init(null);
		Map<String, String> pkgName2AppPath = new HashMap<>();
		Map<String, String> pkgName2AppName = new HashMap<>();
		for(int i = 1; i <= 69; i++) {
			String appPath = AppPathResolver.resolveAppPath(Config.v().get(Config.APPDIR), i);
			if(appPath == null)
				continue;
			String[] temp = appPath.split(Pattern.quote(File.separator));
			String appName = temp[temp.length - 1];
			ManifestParser parser = new ManifestParser(appPath);
//			System.out.println(parser.getPackageName());
			pkgName2AppPath.put(parser.getPackageName(), appPath);
			pkgName2AppName.put(parser.getPackageName(), appName);
		}

		List<String> buggyApps = Arrays.asList("a2dp.Vol", "com.zoffcc.applications.aagtl", "com.morphoss.acal", "com.addi", "hu.vsza.adsdroid", "org.jtb.alogcat", "com.example.amazed", "com.templaro.opsiz.aka", "org.liberty.android.fantastischmemo", "net.sf.andbatdog.batterydog", "ch.blinkenlights.battery", "caldwell.ben.bites", "org.scoutant.blokish", "org.beide.bomber", "com.eleybourn.bookcatalogue", "org.jessies.dalvikexplorer", "org.dnaq.dialer2", "com.gluegadget.hndroid", "com.teleca.jamendo", "com.fsck.k9", "net.fercanet.LNM", "com.chmod0.manpages", "com.evancharlton.mileage", "jp.gr.java_conf.hatalab.mnv", "com.hectorone.multismssender", "i4nc4mp.myLock", "org.passwordmaker.android", "com.google.android.photostream", "com.bwx.bequick", "cri.sanity", "com.beust.android.translate", "es.senselesssolutions.gpl.weightchart");
		buggyApps.sort((a, b) -> a.compareTo(b) );
		File destDir = new File("C:\\Users\\echo\\Desktop\\buggy");
		try {
			for(String s : buggyApps) {
				String appPath = pkgName2AppPath.get(s);
//				if(appPath == null)
//					System.out.println(s + "does not exist.");
				FileUtils.copyFileToDirectory(new File(appPath), destDir);
				System.out.println(pkgName2AppName.get(s).split("_")[0] + " " + s);
//				System.out.println(s + " " + pkgName2AppName.get(s));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
