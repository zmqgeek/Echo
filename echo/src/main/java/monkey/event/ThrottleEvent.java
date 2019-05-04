package monkey.event;

import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import util.Log;

/**
 * Inject a fixed time delay between events during testing to let GUI respond to the injected events.
 * 
 * This class is adapted from the MonkeyThrottleEvent of the Android Open Source Project.
 * 
 * @author echo
 */

public class ThrottleEvent extends Event {
	public ThrottleEvent() {
		super(Event.EVENT_TYPE_THROTTLE);
		this.mthrottle = Throttle.v().getThrottleDuration();
	}

	public ThrottleEvent(long millis) {
		super(Event.EVENT_TYPE_THROTTLE);
		this.mthrottle = millis;
	}
	
	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		try {
			Thread.sleep(mthrottle);
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("Inject " + this.getClass().getName() + " failed.");
		}
	}
	
	@Override
	public boolean isThrottlable() {
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("# ");
		builder.append(getID());
		builder.append(" [ThrottleEvent] A fixed time delay: ").append(mthrottle);
		return builder.toString();
	}
	private long mthrottle;
}
