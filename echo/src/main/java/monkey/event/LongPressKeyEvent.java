package monkey.event;

import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import util.AndroidKeyCodeWrapper;

/**
 * Long press key event.
 * Long press the key specified by the key code.
 * 
 * This event represents pressing down, holding and releasing the key.
 * 
 * @author echo
 */
public class LongPressKeyEvent extends Event {
	public LongPressKeyEvent(int keyCode) {
		super(Event.EVENT_TYPE_KEY);
		mKeyCode = keyCode;
	}
	
	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		env.driver().longPressKeyCode(mKeyCode);
	}
	
	/**
	 * Touch actions are throttled via waitAction() method.
	 * Key events are throttled via inserting a ThrottleEvent manually after them. 
	 */
	@Override
	public boolean isThrottlable() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder  = new StringBuilder();
		builder.append("# ");
		builder.append(getID());
		builder.append(" [LongPressKeyEvent] Long press key: ").append(AndroidKeyCodeWrapper.v().getKeyCodeName(mKeyCode));
		return builder.toString();
	}
	
	protected int mAction;
	private int mKeyCode;
}
