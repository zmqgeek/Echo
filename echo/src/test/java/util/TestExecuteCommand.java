package util;

import java.io.File;

import org.junit.Test;

import monkey.util.Logcat;

public class TestExecuteCommand {
	
	// Test global command
	@Test
	public void test1() {
		String ret = ExecuteCommand.exec(null, "ping", "127.0.0.1");
		System.out.println(ret);
	}
	
	// Test the command in a specific directory
	@Test
	public void test2() {
		Config.init(null);
		String cmdDir = String.join(File.separator, Config.v().get(Config.SDK), "platform-tools");
		System.out.println(cmdDir);
		String ret = ExecuteCommand.exec(cmdDir, "adb.exe", "devices");
		System.out.println(ret);
	}
	
	// Test Logcat command
	@Test
	public void test3() {
		Config.init(null);
		Logcat.clean();
	}
}
