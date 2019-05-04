package reduction.ttg;

import org.jgraph.graph.DefaultEdge;

import monkey.event.Event;
import monkey.event.ThrottleEvent;
import reduction.event.InspectEvent;

/**
 * TTGEdge represents an app state transition achieved by an event.
 * 
 * @author echo
 */
public class TTGEdge extends DefaultEdge {
	public TTGEdge(TTGNode _source, TTGNode _target, Event _event) {
		source = _source;
		target = _target;
		assert ! (event instanceof InspectEvent);
		assert ! (event instanceof ThrottleEvent);
		event = _event;
	}
	
	public TTGNode getSource() {
		return (TTGNode) source;
	}
	
	public TTGNode getTarget() {
		return (TTGNode) target;
	}
	
	public Event getEvent() {
		return event;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null)
			return false;
		if(! this.getClass().equals(o.getClass()))
			return false;
		TTGEdge edge = (TTGEdge) o;
		return source.equals(edge.source) && target.equals(edge.target) && event.equals(edge.event);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + source.hashCode();
		result = prime * result + target.hashCode();
		result = prime * result + event.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("From [");
		builder.append(getSource().getID());
		builder.append("] to [");
		builder.append(getTarget().getID());
		builder.append("] via ");
		builder.append(event);
		builder.append(". ");
		builder.append("Source: [");
		builder.append(source);
		builder.append("] Target: [");
		builder.append(target);
		builder.append("] via ");
		builder.append(event);
		return builder.toString();
	}
	
	private Event event;
}
