package reduction.ttg.node;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import monkey.event.Event;
import monkey.util.Layout;
import reduction.event.InspectEvent;
import reduction.ttg.TTGNode;
import reduction.ttg.node.NormalStateFactory.NormalStateKey;

/**
 * A NormalState represents an error-free tesing state during testing. 
 * Program state includes the activity name and the XML layout file content of current page.
 * An event may not trigger state transition. The events that are performed over the same state are aggregated together.
 * 
 * Events are organized as event sequences and stored into a list. 
 * Once a transfer happens, a new event sequence is created and appended to the end of event sequence list. 
 * 
 * @author echo
 */
public class NormalState extends TTGNode {
	/**
	 * The constructor can be accessed within the same package.
	 * Only the NormalStateFactory is able to create the NormalState.
	 * Other classes can only create the NormalState via the NormalStateFactory.
	 */
	NormalState(int _id, NormalStateKey key) {
		assert key.layout != null;
		id = _id;
		entry = false;
		layout = key.layout;
		eventSeqs = new LinkedList<>();
	}

	public Layout getLayout() {
		return layout;
	}

	// Return events stored in current state.
	public List<Event> getEvents() {
		return eventSeqs.stream().flatMap(Collection::stream).collect(Collectors.toList());
	}
	
	public Deque<Deque<Event>> getEventSeqs() {
		return eventSeqs;
	}

	// Add an event to the last event sequence.
	public void addEvent(Event event) {
		assert ! (event instanceof InspectEvent);
		eventSeqs.getLast().add(event);
	}
	
	public byte[] getScreenshot() {
		return screenshot;
	}
	
	public void initScreenshot(byte[] screenshot) {
		if(this.screenshot == null) {
			this.screenshot = screenshot;
		} else {}
	}
	
	// Create a new event sequence and add it to the event sequence queue.
	// This happens in two scenarios: 
	// 1. A state is newly created;
	// 2. A state transfers to an existing state.
	public void createNewEventSeq() {
		LinkedList<Event> eventSeq = new LinkedList<>();
		eventSeqs.add(eventSeq);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null)
			return false;
		if(! this.getClass().equals(o.getClass()))
			return false;
		NormalState node = (NormalState) o;
		return layout.equals(node.layout);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + layout.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("#ID ");
		builder.append(id);
		if(entry)
			builder.append(" #Entry# ");
		builder.append(" Activity: ");
		builder.append(layout.getActivity());
		builder.append(" Layout: ");
		builder.append(layout.getLayoutContent());
//		builder.append(" Event: [");
//		for(Event e : events) {
//			builder.append(e);
//			builder.append(" ");
//		}
// 		builder.append("]");
		return builder.toString();
	}
	
	
	private Layout layout;
	private Deque<Deque<Event>> eventSeqs;
	private byte[] screenshot;
}
