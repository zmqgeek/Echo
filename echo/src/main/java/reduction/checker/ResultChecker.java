package reduction.checker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DirectedPseudograph;

import monkey.event.Event;
import reduction.EventCollector;
import reduction.PathFinder;
import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import reduction.ttg.node.ErrorState;
import reduction.ttg.node.NormalState;

/**
 * Check the results of event reduction.
 * @author echo
 */
public class ResultChecker {
	// This method checks the event sequences of given TTG.
	public static void check(DirectedPseudograph<TTGNode, TTGEdge> ttg, 
			Class<? extends PathFinder> pathFinderClz, Class<? extends EventCollector> eventCollectorClz) {
		List<TTGNode> nodes = new ArrayList<>(ttg.vertexSet());
		nodes.sort((n1, n2) -> Integer.compare(n1.getID(), n2.getID()));
		for(TTGNode node : nodes) {
			if(node instanceof ErrorState)
				System.out.println(node);
			else {
				NormalState normalState = (NormalState) node;
				System.out.println("# " + normalState.getID() + ", outdegree " + ttg.outDegreeOf(node));
				for(TTGEdge edge : ttg.outgoingEdgesOf(normalState)) {
					TTGNode tgt = edge.getTarget();
					System.out.println("From " + normalState.getID() + " to " + tgt.getID() + " via #" + edge.getEvent().getID());
				}
				System.out.println("#Event sequence: " + normalState.getEventSeqs().size());
				for(Deque<Event> eventSeq : normalState.getEventSeqs()) {
					if(eventSeq.isEmpty())
						System.out.println("  " + eventSeq);
					else {
						System.out.println("  From " + eventSeq.peekFirst().getID() + " to " + eventSeq.peekLast().getID());
					}
				}
				System.out.println();
				System.out.println("# " + normalState.getID() + ", indegree " + ttg.outDegreeOf(node));
				for(TTGEdge edge : ttg.incomingEdgesOf(normalState)) {
					System.out.println("From " + edge.getSource().getID() + " to " + edge.getTarget().getID() + " via #" + edge.getEvent().getID());
				}
			}
			System.out.println();
		}
		// Check the path generated by PathFinder.
		assert ! pathFinderClz.equals(PathFinder.class);

		Optional<TTGNode> opt = ttg.vertexSet().stream().filter(TTGNode::isErrorState).findFirst();
		// If error event is not in the TTG, return an empty list
		if(! opt.isPresent())
			System.out.println("# No error found.");
		else {
			// Use reflection to create instances of PathFinder and EventCollector.
			PathFinder pathFinder = null;
			try {
				pathFinder = pathFinderClz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			System.out.println("Shortest path info: ");
			GraphPath<TTGNode, TTGEdge> path = pathFinder.findPath(ttg);
			List<TTGNode> nodesOnPath = path.getVertexList();
			List<TTGEdge> edgesOnPath = path.getEdgeList();
			assert edgesOnPath.size() == nodesOnPath.size() - 1;
			for(int i = 0; i < nodesOnPath.size() - 1; i++) {
				System.out.println("From " + nodesOnPath.get(i).getID() + " to " + nodesOnPath.get(i + 1).getID() 
				+ " via # " + edgesOnPath.get(i).getEvent().getID());
			}
			System.out.println();
			// Check the outgoing edges of the nodes on the generated path.
			for(int i = 0; i < nodesOnPath.size() - 1; i++) {
				TTGNode currentNode = nodesOnPath.get(i);
				TTGNode nextNode = nodesOnPath.get(i + 1);
				List<TTGEdge> outgoingEdges = new ArrayList<>();
				outgoingEdges.addAll(ttg.getAllEdges(currentNode, nextNode));
				outgoingEdges.sort((e1, e2) -> Integer.compare(e1.getEvent().getID(), e2.getEvent().getID()));
				System.out.println("From # " + currentNode.getID() + " to # " + nextNode.getID());
				System.out.println("Outgoing edges: ");
				outgoingEdges.stream().map(TTGEdge::getEvent).map(Event::getID).forEach(j -> System.out.println("  #" + j));
			}
			System.out.println();
			assert eventCollectorClz != EventCollector.class;
			EventCollector eventCollector = null;
			try {
				eventCollector = eventCollectorClz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<Event> reducedEvents = eventCollector.collectEventsOnPath(ttg, path);
			System.out.println("Reduced events ID:");
			reducedEvents.stream().map(Event::getID).forEach(System.out::println);
			System.out.println("Reduced events: ");
			reducedEvents.forEach(System.out::println);
		}
	}
}
