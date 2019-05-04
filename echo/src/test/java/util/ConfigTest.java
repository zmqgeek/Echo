package util;

import org.junit.Test;

public class ConfigTest {
	@Test
	public void test1() {
		Config.init(null);
		System.out.println(Config.v().get(Config.ANDROIDJAR));
		System.out.println(Config.v().get(Config.APPDIR));
		System.out.println(Config.v().get(Config.NEWPROJECT));
		System.out.println(Config.v().get(Config.OUTPUT));
		System.out.println(Config.v().get(Config.RES));
		
		System.out.println(Config.v().get("SDK"));
		System.out.println(Config.v().get("build-tool-version"));
		System.out.println(Config.v().get("heap-size"));
		System.out.println(Config.v().get("stack-size"));
	}
}
