package reduction;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DirectedPseudograph;

import monkey.event.Event;
import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import reduction.ttg.node.NormalState;

/**
 * Collect events against the given path.
 * @author echo
 */
public class PathEventCollector implements EventCollector {
	@Override
	public List<Event> collectEventsOnPath(DirectedPseudograph<TTGNode, TTGEdge> ttg, GraphPath<TTGNode, TTGEdge> path) {
		List<TTGNode> nodesOnPath = path.getVertexList();
		List<TTGEdge> edgesOnPath = path.getEdgeList();
		assert edgesOnPath.size() == nodesOnPath.size() - 1;
		List<Event> events = new ArrayList<>();
		TTGEdge prevEdge = null;
		for(int i = 0; i < nodesOnPath.size() - 1; i++) {
			// currentNode must be a NormalState.
			assert nodesOnPath.get(i) instanceof NormalState;
			NormalState currentNode = (NormalState) nodesOnPath.get(i);
			final int nextIndex = i + 1;
			TTGNode nextNode = nodesOnPath.get(nextIndex);
			TTGEdge currentEdge = edgesOnPath.get(i);
			// Assure that currentEdge connects currentNode and nextNode.
			assert ttg.getAllEdges(currentNode, nextNode).contains(currentEdge);
			// If prevEdge is null, currentNode is the entry node.
			// NOTE that the events stored in the entry node are not collected. 
			if(prevEdge != null) {
				/**
				 * Collect a event sequence from the event sequence list of currentNode 
				 * according to the the ID of the event on prevEdge and currentEdge.
				 * ... --prevEdgeEventID-->currentNode [prevEdgeEventID + 1, ..., currentEdgeEventID - 1]--currentEdgeEventID--> ...
				 */
				final int seqHeadEventID = prevEdge.getEvent().getID() + 1;
				final int seqTailEventID = currentEdge.getEvent().getID() - 1;
				for(Deque<Event> eventSeq : currentNode.getEventSeqs()) {
					Event head = eventSeq.peekFirst();
					Event tail = eventSeq.peekLast();
					if(head != null && head.getID() == seqHeadEventID &&
							tail != null && tail.getID() == seqTailEventID) {
						// Comment off for ASE paper
						//events.addAll(eventSeq);
					}
				}
			}
			// Collect event on current edge.
			events.add(currentEdge.getEvent());
			// Set currentEdge as prevEdge.
			prevEdge = currentEdge;
		}
		return events;
	}
}
