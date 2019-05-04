package reduction.ttg.node;

import java.util.HashMap;
import java.util.Map;

import monkey.util.Layout;

/**
 * This class is a factory for the NormalState. It manages creation of TTG graph node.
 * It saves a copy of the node which has been created. 
 * The nodes that have been created can be obtained via NormalStateKey.
 * 
 * @author echo
 */
public class NormalStateFactory {
	static {
		nodes = new HashMap<>();
		id = 0;
	}
	
	public static boolean contains(Layout layout) {
		return nodes.containsKey(new NormalStateKey(layout));
	}

	public static NormalState get(Layout layout) {
		NormalStateKey key = new NormalStateKey(layout);
		assert nodes.containsKey(key);
		return nodes.get(key);
	}

	public static NormalState create(Layout layout) {
		NormalStateKey key = new NormalStateKey(layout);
		return create(key);
	}
	
	public static NormalState getOrCreate(Layout layout) {
		NormalStateKey key = new NormalStateKey(layout);
		if(nodes.containsKey(key))
			return nodes.get(key);
		else {
			return create(key);
		}
	}
	
	private static NormalState create(NormalStateKey key) {
		assert ! nodes.containsKey(key);
		NormalState node = new NormalState(id, key);
		nodes.put(key, node);
		id++;
		return node;
	}
	
	// Discard all the nodes.
	public static void reset() {
		nodes.clear();
		id = 0;
	}

	private static Map<NormalStateKey, NormalState> nodes;
	private static int id;
	
	static class NormalStateKey {
		public NormalStateKey(Layout _layout) {
			layout = _layout;
		}
		Layout layout;
		
		@Override
		public boolean equals(Object o) {
			if(this == o)
				return true;
			if(o == null)
				return false;
			if(! getClass().equals(o.getClass()))
				return false;
			NormalStateKey key = (NormalStateKey) o;
			return layout.equals(key.layout);
		}
		
		@Override
		public int hashCode() {
			return layout.hashCode();
		}
	}
}
