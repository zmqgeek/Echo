package monkey.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import monkey.util.TestingOptions;
import reduction.event.CheckActivityEvent;
import reduction.event.CheckLayoutEvent;
import reduction.event.SlidingWindowEvent;
import util.Log;

/**
 * Queue of testing events.
 * 
 * This class is adapted from the class MonkeyEventQueue of the Android Open Source Project.
 * 
 * @author echo
 */

@SuppressWarnings("serial")
public class EventQueue extends LinkedList<Event> {
	// Initialize the meta-class of inspecting event
	// TODO
	// Make this as an option.
	static {
		inspectEventMetaClz = Arrays.asList(
			CheckActivityEvent.class,
			CheckLayoutEvent.class
		);
		
		// Sliding window event. 
		slidingWindowEvents = Arrays.asList(SlidingWindowEvent.v());
	}
	
	public EventQueue() {
		super();
		throttleEvent = new ThrottleEvent();
		inspectEvents = new ArrayList<>();
		/**
		 * Prepare inspecting event objects.
		 * Both throttling and inspecting events are reused.
		 * It is safe to do that because these events are just instructions on the client side, 
		 * rather than the real events being injected into the system.
		 * 
		 * @author echo
		 */
		for(Class<? extends Event> c : inspectEventMetaClz) {
			try {
				inspectEvents.add(c.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
				Log.println("Failed to inject event " + c.getName());
				System.exit(0);
			}
		}
		// Initialize the event queue with the first two events
		// that inspects the current activities.
		super.addAll(inspectEvents);
		super.addLast(new ThrottleEvent());
		
	}

	@Override
	public void addLast(Event event) {
		super.add(event);
		// Insert a fix time delay after some events to let the GUI respond to the event
		if (event.isThrottlable()) {
			super.addLast(throttleEvent);
		}
		// Insert inspecting events
		// If sliding window model is adopted, sliding window event is injected. 
		// Otherwise,  normal inspection events are injected.
		if(TestingOptions.v().slidingWindowModel()) {
			super.addAll(slidingWindowEvents);
		} else {
			 super.addAll(inspectEvents);
		}
	}
	
	// This list contains the meta class objects of the inspecting events 
	// that are going to be injected
	private List<Event> inspectEvents;
	// Throttling event that is going to be injected
	private ThrottleEvent throttleEvent;
	// Meta-class of inspecting events
	private static List<Class<? extends Event>> inspectEventMetaClz;
	// 
	private static List<Event> slidingWindowEvents;
}
