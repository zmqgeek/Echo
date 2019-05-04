package monkey.event;

import io.appium.java_client.TouchAction;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import util.PointF;

/**
 * This class represents drag event. 
 * @author echo
 */
public class DragEvent extends MotionEvent {
	public DragEvent(long downAt, int metaState) {
		super(Event.EVENT_TYPE_TOUCH, downAt, metaState);
	}
	
	public DragEvent() {
		super(EVENT_TYPE_TOUCH, -1, -1);
	}

	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		assert mFromPointers.size() == 1;
		assert mToPointers.size() == 1;
		PointF from = mFromPointers.get(0);
		PointF to = mToPointers.get(0);
		int fromX = Math.round(from.x);
		int fromY = Math.round(from.y);
		int toX = Math.round(to.x);
		int toY = Math.round(to.y);
		new TouchAction(env.driver()).longPress(fromX, fromY).moveTo(toX, toY).release()
		.waitAction(Throttle.v().getDuration()).perform();
	}

	@Override
	public String toString() {
		assert mFromPointers.size() == 1;
		assert mToPointers.size() == 1;
		PointF from = mFromPointers.get(0);
		PointF to = mToPointers.get(0);
		int fromX = Math.round(from.x);
		int fromY = Math.round(from.y);
		int toX = Math.round(to.x);
		int toY = Math.round(to.y);
		StringBuilder builder = new StringBuilder();
		builder.append("# ");
		builder.append(getID());
		builder.append(" [DragEvent] Drag from (").append(fromX).append(", ").append(fromY).append(") to (")
		.append(toX).append(", ").append(toY).append(").");
		return builder.toString();
	}
}
