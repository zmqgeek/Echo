package monkey.random;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedPseudograph;
import org.junit.Test;

import monkey.TestUtil;
import monkey.event.Throttle;
import monkey.exception.TestFailureException;
import monkey.random.RandomEventSource;
import monkey.util.Logcat;
import monkey.util.TestingOptions;
import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import reduction.ttg.TestingTraceGraph;
import reduction.util.TTGReductionHelper;
import util.Config;
import util.Log;
import util.Timer;

public class TestRealWorldApp {
	// Test app
	@Test
	public void testApp0() {
		testApp(0,  10000, 0);
	}
	
	// Bug found
	@Test
	public void testApp1() {
		String[] args = new String[] {"-app", "1", "-event",  "5000", "-throttle", "500", "-seed", "0"};
		monkey.Main.main(args);
	}
	
	// Appium crashes
	@Test
	public void testApp2() {
		testApp(2,  10000, 0);
	}
	
	// Bug found
	@Test
	public void testApp3() {
		String[] args = new String[] {"-app", "3", "-event",  "5000", "-throttle", "500", "-seed", "0"};
		monkey.Main.main(args);
	}
	
	// Bug found
	@Test
	public void testApp4() {
		String[] args = new String[] {"-app", "4", "-event",  "5000", "-throttle", "500", "-seed", "0"};
		monkey.Main.main(args);
		Set<TTGNode> nodes = TestingTraceGraph.v().vertexSet();
		System.out.println("TTG");
		System.out.println(TestingTraceGraph.v());
		for(TTGNode n : nodes) {
			System.out.println("# Node: " + n);
			System.out.println("# Outging edges: ");
			TestingTraceGraph.v().outgoingEdgesOf(n).forEach(System.out::println);
		} 
	}
	
	// No bugs found
	@Test
	public void testApp5() {
		testApp(5,  10000, 0);
	}

	// This is a game
	@Test
	public void testApp6() {
		testApp(6,  10000, 0);
	}

	// NullPointerException
	@Test
	public void testApp7() {
		String[] args = new String[] {"-app", "7", "-event",  "5000", "-throttle", "500", "-seed", "0"};
		monkey.Main.main(args);
	}

	// No bugs found up to 1177 events
	// This application is simple (only one activity)
	@Test
	public void testApp8() {
		testApp(8,  10000, 0);
	}
	
	// No bugs found up to 1064 events
	@Test
	public void testApp9() {
		testApp(9,  10000, 0);
	}

	// Bug found
	@Test
	public void testApp10() {
		String[] args = new String[] {"-app", "10", "-event",  "5000", "-throttle", "500", "-seed", "8888"};
		monkey.Main.main(args);
	}
	
	// No bug found up to 1215 events
	@Test
	public void testApp11() {
		String[] args = new String[] {"-app", "11", "-event",  "5000", "-throttle", "500", "-seed", "0"};
		monkey.Main.main(args);
	}
	
	// App 12 A game. NullPointerException
	// App 13 ClassCastException
	// App 14 No exception found up to 1650 events
	// App 15 This app is simple. Show info. 
	// App 16 Make phone call. Simple app. 
	// App 17 A Game. No exception found up to 1310 events
	// App 18 A File explorer. App is simple.
	// App 19 
	@Test
	public void testApp12To22() {
		for(int i = 19; i <= 22; i++) {
			String[] args = new String[] {"-app", Integer.toString(i), "-event",  "5000", "-throttle", "500", "-seed", "0"};
			monkey.Main.main(args);
		}
	}
	
	/**
	 * Test Buggy apps
	 */
	@Test
	public void testBuggyApps() {
		List<Integer> buggyAppIDs = Arrays.asList(
				// finding bugs successfully
				// 1, 4, 6, 7, 9, 10, 12, 16, 31, 32, 33, 34, 35, 36, 39, 45, 48, 53, 54, 68
				// fail to find bugs
				// 5, 11, 13, 14, 15, 25, 29, 30, 44, 51, 54
				// error
				// 2, 29, 51
//				32, 33, 36, 39, 39
				// 31,
				4
				);
		for(int i = 0; i < Math.min(5, buggyAppIDs.size()); i++) {
			String[] args = new String[] {"-app", buggyAppIDs.get(i).toString(), "-event",  "10000", "-throttle", "500", "-seed", "0", "-screenshot"};
			monkey.Main.main(args);
		}
		DirectedPseudograph<TTGNode, TTGEdge> ttg = TestingTraceGraph.v().getTTG();
		Set<TTGNode> nodes = ttg.vertexSet();
		Set<TTGEdge> edges = ttg.edgeSet();
		System.out.println("#Node: " + nodes.size());
		System.out.println("#Edge: " + edges.size());
		System.out.println("#Events: " + TTGReductionHelper.getEvents(ttg).size());
	}
	
	/**
	 * Test the real-world app
	 */
	private void testApp(int i, int events, int seed) {
		Timer timer = new Timer();
		timer.start();
		TestUtil.initTesting(Integer.toString(i), (info, env) -> {
			System.out.println(info.getPkgName());
			Logcat.clean();
			Throttle.v().init(500);
			TestingOptions.v().setNumberOfEvents(events);
			String output = Config.v().get(Config.OUTPUT);
			File outputDir = new File(output);
			if(! outputDir.exists())
				outputDir.mkdir();
			String fileName = String.join(File.separator, output, info.getAppFileName() + ".txt");
			try(PrintStream printStream = new PrintStream(fileName)) {
				Log.init(printStream);
				RandomEventSource eventSource = new RandomEventSource(info, env, seed);
				eventSource.runTestingCycles();
				timer.stop();
				Log.println("# Time: " + timer.getDurationInSecond() + " s.");
			} catch (TestFailureException e) {
				// TODO: handle exception
			}catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		});
	}
}
