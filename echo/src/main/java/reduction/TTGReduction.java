package reduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DirectedPseudograph;

import monkey.event.Event;
import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import util.Log;

/**
 * This class reduces TTG to replay error.
 * @author echo
 */
public class TTGReduction {
	// Reduce the testing trace based on TTG.
	public static List<Event> reduce(DirectedPseudograph<TTGNode, TTGEdge> ttg, 
			Class<? extends PathFinder> pathFinderClz, Class<? extends EventCollector> eventCollectorClz) {
		// Assure that the pathFinderClz is not PathFinder and eventCollectorClz is not EventCollector, 
		// both of which are abstract class and cannot be instantiated.
		assert ! pathFinderClz.equals(PathFinder.class);
		assert ! eventCollectorClz.equals(EventCollector.class);
		
		Optional<TTGNode> opt = ttg.vertexSet().stream().filter(TTGNode::isErrorState).findFirst();
		// If error event is not in the TTG, return an empty list
		if(! opt.isPresent())
			return new ArrayList<>();
		
		// Use reflection to create instances of PathFinder and EventCollector.
		PathFinder pathFinder = null;
		EventCollector eventCollector = null;
		try {
			pathFinder = pathFinderClz.newInstance();
			eventCollector = eventCollectorClz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		Log.println("Use " + pathFinderClz.getName() + " as path finder.");
		// First, find nodes in a path from the entry node to error state node.
		GraphPath<TTGNode, TTGEdge> path = pathFinder.findPath(ttg);
		// Then, collect events from the path. 
		return eventCollector.collectEventsOnPath(ttg, path);
	}
}
