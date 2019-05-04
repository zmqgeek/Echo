package reduction;

import java.util.Optional;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DirectedPseudograph;

import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;
import reduction.ttg.node.ErrorState;
import reduction.ttg.node.NormalState;

/**
 * This class generates a path from the entry to error state 
 * in the TTG according to some strategy.
 * 
 * @author echo
 */
public interface PathFinder {
	public abstract GraphPath<TTGNode, TTGEdge> findPath(DirectedPseudograph<TTGNode, TTGEdge> ttg);
	// Return the entry node on the TTG. 
	// An runtime exception is raised if the entry node does not exist.
	public static NormalState getEntryNode(AbstractGraph<TTGNode, TTGEdge> ttg) {
		Optional<TTGNode> fromOpt = ttg.vertexSet().stream().filter(TTGNode::isEntry).findFirst();
		if(! fromOpt.isPresent())
			throw new RuntimeException("Entry state node is not found. ");
		return (NormalState) fromOpt.get();
	}
	
	// Return the error state node on the TTG. 
	// An runtime exception is raised if the entry node does not exist.
	public static ErrorState getErrorNode(AbstractGraph<TTGNode, TTGEdge> ttg) {
		Optional<TTGNode> toOpt = ttg.vertexSet().stream().filter(TTGNode::isErrorState).findFirst();
		if(! toOpt.isPresent())
			throw new RuntimeException("Error state node is not found. ");
		return (ErrorState) toOpt.get();
	}
}
