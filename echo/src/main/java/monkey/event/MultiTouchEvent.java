package monkey.event;

import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.TouchAction;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import util.PointF;

/**
 * This class represents multitouch event, like zooming and pinching.
 * @author echo
 */
public class MultiTouchEvent extends MotionEvent {
	public MultiTouchEvent(long downAt, int metaState) {
		super(Event.EVENT_TYPE_TOUCH, downAt, metaState);
	}
	
	public MultiTouchEvent() {
		super(Event.EVENT_TYPE_TOUCH, -1, -1);
	}

	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		assert mFromPointers.size() == mToPointers.size();
		MultiTouchAction action = new MultiTouchAction(env.driver());
		for(int i = 0; i < mFromPointers.size(); i++) {
			PointF from = mFromPointers.get(i);
			PointF to = mToPointers.get(i);
			int fromX = Math.round(from.x);
			int fromY = Math.round(from.y);
			int toX = Math.round(to.x);
			int toY = Math.round(to.y);
			action.add(new TouchAction(env.driver()).longPress(fromX, fromY).moveTo(toX, toY).release().waitAction(Throttle.v().getDuration()));
		}
		action.perform();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("# ");
		builder.append(getID());
		builder.append(" Multitouch from [");
		for(int i = 0; i < mFromPointers.size(); i++) {
			PointF p = mFromPointers.get(i);
			builder.append("(").append(p.x).append(", ").append(p.y).append("), ");
		}
		builder.append("], to[");
		for(int i = 0; i < mToPointers.size(); i++) {
			PointF p = mToPointers.get(i);
			builder.append("(").append(p.x).append(", ").append(p.y).append("), ");
		}
		builder.append("].");
		return builder.toString();
	}
}
