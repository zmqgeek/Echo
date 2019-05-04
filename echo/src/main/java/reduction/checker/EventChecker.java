package reduction.checker;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DirectedPseudograph;

import monkey.event.Event;
import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import reduction.util.TTGReductionHelper;

/**
 * Check the event reduction results. 
 * @author echo
 */
public class EventChecker {
	public EventChecker(DirectedPseudograph<TTGNode, TTGEdge> ttg) {
		eventsOnEdges = new ArrayList<>();
		eventsOnNodes = new ArrayList<>();
		eventsOnEdges.addAll(TTGReductionHelper.getEventsFromNode(ttg));
		eventsOnNodes.addAll(TTGReductionHelper.getEventsFromEdge(ttg));
	}
	
	public boolean isOnNode(Event event) {
		return eventsOnNodes.contains(event);
	}
	
	public boolean isOnEdge(Event event) {
		return eventsOnEdges.contains(event);
	}
	
	private List<Event> eventsOnEdges;
	private List<Event> eventsOnNodes;
}
