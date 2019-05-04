package monkey.event;

import io.appium.java_client.TouchAction;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import util.PointF;

/**
 * This class represents long-pressing a coordinate on the touchscreen.
 * 
 * @author echo
 */
public class LongPressEvent extends MotionEvent {

	public LongPressEvent(long downAt, int metaState) {
		super(Event.EVENT_TYPE_TOUCH, downAt, metaState);
	}
	
	public LongPressEvent() {
		super(Event.EVENT_TYPE_TOUCH, -1, -1);
	}

	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		assert mFromPointers.size() == 1;
		PointF p = mFromPointers.get(0);
		int mX = Math.round(p.x);
		int mY = Math.round(p.y);
		new TouchAction(env.driver()).longPress(mX, mY).release().waitAction(Throttle.v().getDuration()).perform();
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
		builder.append(" [LongPressEvent] Long press (").append(mX).append(", ").append(mY).append(").s");
		return builder.toString();
	}
}
