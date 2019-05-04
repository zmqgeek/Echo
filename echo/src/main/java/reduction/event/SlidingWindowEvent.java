package reduction.event;

import java.util.ArrayList;
import java.util.List;

import monkey.exception.TestFailureException;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import reduction.ttg.TestingTraceGraph;
import util.Log;

/**
 * Adaptive sliding window model 
 * The silding window model are parameterized by three coefficients: window size ws, 
 * frequency f and window adjustment size delta. 
 * 
 * Sliding window model aims to inspect testing process more efficiently. 
 * 
 * @author echo
 */
public class SlidingWindowEvent extends InspectEvent {

	private boolean debug = false;

	// Singleton
	public static SlidingWindowEvent v() {
		return SingletonHolder.singleton;
	}

	private SlidingWindowEvent() {
		windowSize = 1;
		eventCounter = windowSize;
		delta = 1;
		changingSlidingWindowFreq = 1;
		slidingWindowCounter = changingSlidingWindowFreq;
		checkActivityEvent = new CheckActivityEvent();
		checkLayoutEvent = new CheckLayoutEvent();
		testingInfo = new TestingInfo();
	}

	// Inspecting events that are going to be injected
	private CheckActivityEvent checkActivityEvent;
	private CheckLayoutEvent checkLayoutEvent;

	// Sliding window
	private static final int MAX_WIN_SIZE  = Integer.MAX_VALUE;
	private int windowSize;
	private int eventCounter;

	// Window size adjustment size 
	private int delta;
	
	// Frequency to adjust the size of sliding window
	private final int changingSlidingWindowFreq;
	private int slidingWindowCounter;
	// How to adjust the size of sliding window
	private TestingInfo testingInfo;

	@Override
	public void injectEvent(AppInfoWrapper info, Env env) throws TestFailureException {
		// Inject CheckActivityEvent
		checkActivityEvent.injectEvent(info, env);
		if(! debug) {
			eventCounter--;
			// The events in a sliding window have been injected
			// Check whether the size of the sliding window is changed
			if(eventCounter == 0) {
				Log.println("# Injecting CheckLayoutEvent. ");
				checkLayoutEvent.injectEvent(info, env);
				slidingWindowCounter--;
				updateWindowSize();
				Log.println("######## Window Size: " + windowSize);
				eventCounter = windowSize;
				updateTestingInfo();
			}
		}
	}

	private void updateWindowSize() {
		if(slidingWindowCounter == 0) {
			if(! isTTGUpdated()) {
				if(windowSize < MAX_WIN_SIZE) {
					windowSize += delta;
				}
			} else {
				windowSize = windowSize - delta > 1 ? windowSize - delta : 1;
			}
			slidingWindowCounter = changingSlidingWindowFreq;
		}
		assert slidingWindowCounter >= 1;
		assert windowSize >= 1;
	}

	private boolean isTTGUpdated() {
		int lastNodeIndex = testingInfo.ttgNodeSizes.size() - changingSlidingWindowFreq;
		int lastTTGNodeSize = testingInfo.ttgNodeSizes.get(lastNodeIndex);
		int curTTGNodeSize = TestingTraceGraph.v().vertexSet().size();
		
		int lastEdgeIndex = testingInfo.ttgEdgeSizes.size() - changingSlidingWindowFreq;
		int lastTTGEdgeSize = testingInfo.ttgEdgeSizes.get(lastEdgeIndex);
		int curTTGEdgeSize = TestingTraceGraph.v().edgeSet().size();
		
		return curTTGNodeSize != lastTTGNodeSize || curTTGEdgeSize != lastTTGEdgeSize;
	}
	
	private void updateTestingInfo() {
		int node = TestingTraceGraph.v().vertexSet().size();
		int edge = TestingTraceGraph.v().edgeSet().size();
		testingInfo.ttgNodeSizes.add(node);
		testingInfo.ttgEdgeSizes.add(edge);
	}
	
	@Override
	public String toString() {
		return "[SlidingWindowEvent]";
	}

	private static class SingletonHolder {
		private static final SlidingWindowEvent singleton = new SlidingWindowEvent();
	}

	private class TestingInfo {
		List<Integer> ttgNodeSizes;
		List<Integer> ttgEdgeSizes;
		public TestingInfo() {
			ttgNodeSizes = new ArrayList<>();
			ttgNodeSizes.add(0);
			
			ttgEdgeSizes = new ArrayList<>();
			ttgEdgeSizes.add(0);
		}
	}
}
