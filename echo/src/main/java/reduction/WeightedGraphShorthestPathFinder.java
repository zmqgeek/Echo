package reduction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import reduction.util.TTGReductionHelper;

/**
 * A NEW weighted directed graph is created. 
 * Note that the new graph is a shallow copy of the original graph. 
 * 
 * @author echo
 */
public class WeightedGraphShorthestPathFinder implements PathFinder {
	@Override
	public GraphPath<TTGNode, TTGEdge> findPath(DirectedPseudograph<TTGNode, TTGEdge> ttg) {
		DirectedPseudograph<TTGNode, TTGEdge> weightedTTG = createWeightedTTG(ttg);
		transformEdgeWeight(weightedTTG);
		TTGNode src = PathFinder.getEntryNode(weightedTTG);
		TTGNode dest = PathFinder.getErrorNode(weightedTTG);
		DijkstraShortestPath<TTGNode, TTGEdge> algorithm = new DijkstraShortestPath<>(weightedTTG);
		return algorithm.getPath(src, dest);
	}
	
	/**
	 * A NEW weighted TTG is created here. The weight of each edge is the ID of event on it.
	 */
	private DirectedPseudograph<TTGNode, TTGEdge> createWeightedTTG(DirectedPseudograph<TTGNode, TTGEdge> ttg) {
		DirectedPseudograph<TTGNode, TTGEdge> weightedTTG = new DirectedWeightedPseudograph<>(TTGEdge.class);
		for(TTGEdge e : ttg.edgeSet()) {
			TTGNode src = e.getSource();
			TTGNode dest = e.getTarget();
			if(! weightedTTG.containsVertex(src)) weightedTTG.addVertex(src);
			if(! weightedTTG.containsVertex(dest)) weightedTTG.addVertex(dest);
			weightedTTG.addEdge(src, dest, e);
			weightedTTG.setEdgeWeight(e, e.getEvent().getID());
		}
		return weightedTTG;
	}
	
	/**
	 * Transform edge weight.
	 * The larger event ID, the later it is generated. 
	 * Event reduction prioritizes the events generated later as it is closer to the error. 
	 */
	private void transformEdgeWeight(DirectedPseudograph<TTGNode, TTGEdge> weightedTTG) {
		int numOfEvents = TTGReductionHelper.getEvents(weightedTTG).size();
		List<Integer> eventIDList = IntStream.range(0, numOfEvents).boxed().collect(Collectors.toList());
		List<Integer> reversedIDList = new ArrayList<>(eventIDList);
		Collections.reverse(reversedIDList);
		
		Map<Integer, Integer> eventID2EdgeWeight = new HashMap<>();
		for(int i = 0; i < numOfEvents; i++)
			eventID2EdgeWeight.put(eventIDList.get(i), reversedIDList.get(i));
		for(TTGEdge e : weightedTTG.edgeSet()) {
//			System.out.println("## Old weight: " + weightedTTG.getEdgeWeight(e));
//			System.out.println("## New weight: " + eventID2EdgeWeight.get(e.getEvent().getID()));
			weightedTTG.setEdgeWeight(e, eventID2EdgeWeight.get(e.getEvent().getID()));
		}
	}
}
