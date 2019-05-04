package monkey.event;

import java.io.Serializable;

import monkey.exception.TestFailureException;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;

/**
 * An abstract class for event. 
 * 
 * This class is adapted from the class MonkeyEvent of the Android Open Source Project.
 * 
 * @author echo
 */
public abstract class Event implements Serializable {
	protected int eventType;
	// ID of the event.
	protected int ID;
	public static final int EVENT_TYPE_KEY = 0;
	public static final int EVENT_TYPE_TOUCH = 1;
	public static final int EVENT_TYPE_TRACKBALL = 2;
	public static final int EVENT_TYPE_ROTATION = 3;  // Screen rotation
	public static final int EVENT_TYPE_ACTIVITY = 4;
	public static final int EVENT_TYPE_FLIP = 5; // Keyboard flip
	public static final int EVENT_TYPE_THROTTLE = 6;
	public static final int EVENT_TYPE_PERMISSION = 7;
	public static final int EVENT_TYPE_NOOP = 8;
	public static final int EVENT_INSPECT = 9;

	public static final int INJECT_SUCCESS = 1;
	public static final int INJECT_FAIL = 0;

	// error code for remote exception during injection
	public static final int INJECT_ERROR_REMOTE_EXCEPTION = -1;
	// error code for security exception during injection
	public static final int INJECT_ERROR_SECURITY_EXCEPTION = -2;

	public Event(int type) {
		eventType = type;
	}

	public int getEventType() {
		return eventType;
	}

	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	// Perform the corresponding event 
	public abstract void injectEvent(AppInfoWrapper info, Env env) throws TestFailureException;

	/**
	 * @return true if it is safe to throttle after this event, and false otherwise.
	 */
	public abstract boolean isThrottlable();

	public abstract String toString();

	// equals() implementation of Class Event.
	// If the two events are the same type and their IDs are equal, then these two events are the equal.
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null)
			return false;
		if(! this.getClass().equals(o.getClass()))
			return false;
		Event event = (Event) o;
		if(ID == event.ID)
			return true;
		else return false;
	}

	// hashCode() implementation of Class Event.
	// The hash code of an event is its ID.
	public int hashCode() {
		return ID;
	}
}
