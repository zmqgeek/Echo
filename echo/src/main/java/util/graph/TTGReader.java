package util.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.jgrapht.graph.DirectedPseudograph;

import reduction.ttg.TTGEdge;
import reduction.ttg.TTGNode;

/**
 * This class constructs the TTG via a file.
 * @author echo
 */
public class TTGReader {
	// Deserialize the TTG.
	@SuppressWarnings("unchecked")
	public static DirectedPseudograph<TTGNode, TTGEdge> deserializeTTG(File graphFile) {
		DirectedPseudograph<TTGNode, TTGEdge> graph = null;
		try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(graphFile))) {
			graph = (DirectedPseudograph<TTGNode, TTGEdge>) input.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
	}
	
	// Read TTG from dot file
	// TODO
	public static DirectedPseudograph<TTGNode, TTGEdge> readFromDot(File graphFile) {
		DirectedPseudograph<TTGNode, TTGEdge> graph = null;
		return null;
	}
}
