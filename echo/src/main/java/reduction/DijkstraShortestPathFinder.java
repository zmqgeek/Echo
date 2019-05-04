package reduction;

import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedPseudograph;

import monkey.event.Event;
import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;

/**
 * This class generates the shortest path from the entry to the error state on the TTG.
 * 
 * @author echo
 */
public class DijkstraShortestPathFinder implements PathFinder {
	@Override
	public GraphPath<TTGNode, TTGEdge> findPath(DirectedPseudograph<TTGNode, TTGEdge> ttg) {
		// Find the shortest path from entry event to error event
		return shortestPath(ttg);
	}

	// Find the reachable events between two nodes on the graph.
	public static List<Event> getEventsOnShortestPath(DirectedPseudograph<TTGNode, TTGEdge> ttg, TTGNode from, TTGNode to) {
		return shortestPath(ttg, from, to).getEdgeList().stream().map(TTGEdge::getEvent).collect(Collectors.toList());
	}

	// Find the shortest paths between two nodes on the graph.
	// Use Dijkstra algorithm to find the shortest path.
	static <V, E> GraphPath<V, E> shortestPath(DirectedPseudograph<V, E> graph, V from, V to) {
		DijkstraShortestPath<V, E> shortestPath = new DijkstraShortestPath<>(graph);
		return shortestPath.getPath(from, to);
	}

	// Find the shortest paths between the entry node to the error state node on the TTG.
	public static GraphPath<TTGNode, TTGEdge> shortestPath(DirectedPseudograph<TTGNode, TTGEdge> ttg) {
		TTGNode source = PathFinder.getEntryNode(ttg);
		TTGNode sink = PathFinder.getErrorNode(ttg);
		return DijkstraShortestPathFinder.shortestPath(ttg, source, sink);
	}

	// Find the reachable events from the entry node to the error state on the TTG.
	public static List<Event> getEventsOnShortestPath(DirectedPseudograph<TTGNode, TTGEdge> ttg) {
		TTGNode source = PathFinder.getEntryNode(ttg);
		TTGNode sink = PathFinder.getErrorNode(ttg);
		return DijkstraShortestPathFinder.getEventsOnShortestPath(ttg, source, sink);
	}
}
