package reduction.ttg;

import java.util.Set;

import org.jgrapht.graph.DirectedPseudograph;

import monkey.event.Event;
import monkey.util.Layout;
import reduction.ttg.node.ErrorState;
import reduction.ttg.node.NormalState;
import reduction.ttg.node.NormalStateFactory;

/**
 * Testing trace graph (TTG) records the information of testing.
 * TTGNode represents a program state.
 * TTGEdge represents a state transition via event.
 * 
 * @author echo
 */
public class TestingTraceGraph {
	public static synchronized TestingTraceGraph v() {
		if(singleton == null)
			singleton = new TestingTraceGraph();
		return singleton;
	}

	public static void reset() {
		singleton = null;
	}

	public NormalState getLastNormalState() {
		return lastNormalState;
	}

	// Delegate methods of DirectedPseudograph.
	public Set<TTGNode> vertexSet() {
		return ttg.vertexSet();
	}

	public Set<TTGEdge> edgeSet() {
		return ttg.edgeSet();
	}

	public Set<TTGEdge> incomingEdgesOf(TTGNode node) {
		return ttg.incomingEdgesOf(node);
	}

	public Set<TTGEdge> outgoingEdgesOf(TTGNode node) {
		return ttg.outgoingEdgesOf(node);
	}

	// TTG operations
	// Add a new state into TTG
	public void addNewNormalState(Layout from, boolean isEntry) {
		NormalState fromNode = NormalStateFactory.create(from);
		if(isEntry) fromNode.setAsEntry();
		assert ! ttg.containsVertex(fromNode);
		ttg.addVertex(fromNode);
		lastNormalState = fromNode;
		// Create a new event sequence in the NormalState node.
		fromNode.createNewEventSeq();
	}

	// TTG operations
	// Add a new state into TTG and capture the screenshot
	// New method added in screenshot branch
	public void addNewNormalState(Layout from, boolean isEntry, byte[] screenshot) {
		NormalState fromNode = NormalStateFactory.create(from);
		if(isEntry) fromNode.setAsEntry();
		assert ! ttg.containsVertex(fromNode);
		ttg.addVertex(fromNode);
		lastNormalState = fromNode;
		// Create a new event sequence in the NormalState node.
		fromNode.createNewEventSeq();
		fromNode.initScreenshot(screenshot);
	}

	// Insert an edge into TTG
	public void addEdge(NormalState from, Layout to, Event event) {
		assert ttg.containsVertex(from);
		NormalState toNode = NormalStateFactory.getOrCreate(to);
		TTGEdge edge = new TTGEdge(from, toNode, event);
		if(! ttg.containsEdge(edge)) {
			if(! ttg.containsVertex(toNode))
				ttg.addVertex(toNode);
			ttg.addEdge(from, toNode, edge);
		}
		lastNormalState = toNode;
		/**
		 * The state <i>from</i> transfers to the state <i>to</i>, so that an new event sequence of the to state is created.
		 */
		toNode.createNewEventSeq();
	}

	// Insert an edge into TTG
	// Screenshot is captured
	// New method added in screenshot branch
	public void addEdge(NormalState from, Layout to, Event event, byte[] screenshot) {
		assert ttg.containsVertex(from);
		NormalState toNode = NormalStateFactory.getOrCreate(to);
		TTGEdge edge = new TTGEdge(from, toNode, event);
		if(! ttg.containsEdge(edge)) {
			if(! ttg.containsVertex(toNode))
				ttg.addVertex(toNode);
			ttg.addEdge(from, toNode, edge);
		}
		lastNormalState = toNode;
		/**
		 * The state <i>from</i> transfers to the state <i>to</i>, so that an new event sequence of the to state is created.
		 */
		toNode.createNewEventSeq();
		toNode.initScreenshot(screenshot);
	}

	// Update the events performed against an existing layout without introducing any layout updates
	public void updateState(NormalState from, Event event) {
		assert ttg.containsVertex(from);
		from.addEvent(event);
		lastNormalState = from;
	}

	// Add an error state into the TTG
	public void addErrorState(NormalState from, Event event) {
		assert ttg.containsVertex(from);
		TTGNode errorState = new ErrorState();
		assert ! ttg.containsVertex(errorState);
		ttg.addVertex(errorState);
		TTGEdge edge = new TTGEdge(from, errorState, event);
		ttg.addEdge(from, errorState, edge);
	}

	public DirectedPseudograph<TTGNode, TTGEdge> getTTG() {
		return ttg;
	}

	@Override
	public String toString() {
		return toString(ttg);
	}

	public static String toString(DirectedPseudograph<TTGNode, TTGEdge> graph) {
		StringBuilder builder = new StringBuilder();
		for(TTGEdge e: graph.edgeSet()) {
			builder.append(e);
			builder.append("\n");
		}
		return builder.toString();
	}

	private TestingTraceGraph() {
		ttg = new DirectedPseudograph<>(TTGEdge.class);
		lastNormalState = null;
	}

	private DirectedPseudograph<TTGNode, TTGEdge> ttg;
	private NormalState lastNormalState;
	private static TestingTraceGraph singleton;
}
