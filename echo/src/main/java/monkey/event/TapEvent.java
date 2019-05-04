package monkey.event;

import io.appium.java_client.TouchAction;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import util.PointF;

/**
 * This class represents touch events from the touchscreen.
 * 
 * This class is adapted from the class MonkeyTouchEvent of the Android Open Source Project.
 * 
 * @author echo
 */
public class TapEvent extends MotionEvent {
	public TapEvent(long downAt, int metaState) {
		super(Event.EVENT_TYPE_TOUCH, downAt, metaState);
	}
	
	public TapEvent() {
		super(Event.EVENT_TYPE_TOUCH, -1, -1);
	}

	/**
	 * Inject a touch event. Touch a coordinate then waiting for a short period of time.
	 */
	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		assert mFromPointers.size() == 1;
		PointF p = mFromPointers.get(0);
		int mX = Math.round(p.x);
		int mY = Math.round(p.y);
		new TouchAction(env.driver()).tap(mX, mY).waitAction(Throttle.v().getDuration()).perform();
	}
	
	@Override
	public String toString() {
		assert mFromPointers.size() == 1;
		PointF p = mFromPointers.get(0);
		int mX = Math.round(p.x);
		int mY = Math.round(p.y);
		StringBuilder builder = new StringBuilder();
		builder.append("# ");
		builder.append(getID());
		builder.append(" [TouchEvent] Touch the point (").append(mX).append(", ").append(mY).append(").");
		return builder.toString();
	}
}
