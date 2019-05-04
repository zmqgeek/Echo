package util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dump log during testing.
 * 
 * @author echo
 */
public class Log {
	public static void init(PrintStream out) {
		output = new ArrayList<>();
		output.add(out);
		output.add(System.out);
	}
	
	public static void println(String info) {
		output.forEach(o -> o.println(info));
	}
	
	public static void println(Object o) {
		output.forEach(output -> output.println(o.toString()));
	}
	
	private static List<PrintStream> output = Arrays.asList(System.out);
}
