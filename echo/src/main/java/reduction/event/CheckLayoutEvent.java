package reduction.event;

import org.openqa.selenium.OutputType;
import org.xmlunit.diff.Diff;

import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import monkey.util.Layout;
import monkey.util.TestingOptions;
import reduction.ttg.TestingTraceGraph;
import reduction.ttg.node.NormalState;
import util.LayoutComparison;
import util.Log;

/**
 * This event inspects the layout XML file of current page.
 * Then the layout file is inserted into the layout trace.
 * Current layout compares with the previous one.
 * 
 * @author echo
 */
public class CheckLayoutEvent extends InspectEvent {
	public static int numberOfCheckLayoutEvent = 0;
	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		numberOfCheckLayoutEvent++;
		String curLayoutContent = env.driver().getPageSource();
		Layout curLayout = new Layout(getCurrentActivity(env), curLayoutContent);
		NormalState lastNormalState = TestingTraceGraph.v().getLastNormalState();
		if(lastNormalState == null) {
			// Current layout does not have any predecessor, add the node into TTG
			// Screenshot is captured as a byte array and is stored in the node of TTG
			if(TestingOptions.v().takeScreenshot()) {
				handleNewNormalState(env, curLayout, obtainScreenshot(info, env));
			} else {
				handleNewNormalState(env, curLayout);
			}
		} else {
			// Last layout has been found. Add a new edge into TTG (if layout updates are found) 
			// or update the new event into current TTG node (if layout does not update)
			Layout lastLayout = lastNormalState.getLayout();
			Diff diff = LayoutComparison.getDiff(lastLayout, curLayout);
			assert diff != null;
			if(diff.hasDifferences()) {
				Log.println("====== Differences with previous page:");
				diff.getDifferences().forEach(Log::println);
				Log.println("====== End");
				// Modified in screenshot branch
				// Screenshot is captured as a byte array and is stored in the node of TTG
				if(TestingOptions.v().takeScreenshot()) {
					handleNewState(env, lastNormalState, curLayout, obtainScreenshot(info, env));
				} else {
					handleNewState(env, lastNormalState, curLayout);
				}
			} else {
				Log.println("Same as the previous layout. ");
				updateExistingState(env, lastNormalState);
			}
		}
		// Append the layout trace
		env.appendLayout(curLayout);
	}

	@Override
	public String toString() {
		return "[CheckLayoutEvent]";
	}

	// Insert a new node into TTG
	private void handleNewNormalState(Env env, Layout layout) {
		TestingTraceGraph.v().addNewNormalState(layout, true);
	}

	// Insert a new node into TTG
	// New method added in screenshot branch
	private void handleNewNormalState(Env env, Layout layout, byte[] screenshot) {
		TestingTraceGraph.v().addNewNormalState(layout, true, screenshot);
	}

	// Insert an edge into TTG
	private void handleNewState(Env env, NormalState from, Layout to) {
		TestingTraceGraph.v().addEdge(from, to, env.getLastEvent());
	}

	// Insert an edge into TTG
	// New method added in screenshot branch
	private void handleNewState(Env env, NormalState from, Layout to, byte[] screenshot) {
		TestingTraceGraph.v().addEdge(from, to, env.getLastEvent(), screenshot);
	}

	// Update an existing node in TTG
	private void updateExistingState(Env env, NormalState from) {
		TestingTraceGraph.v().updateState(from, env.getLastEvent());
	}

	// Obtain the screenshot
	private byte[] obtainScreenshot(AppInfoWrapper info, Env env) {
		return env.driver().getScreenshotAs(OutputType.BYTES);
	}
}
