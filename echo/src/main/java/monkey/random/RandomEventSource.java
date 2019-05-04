package monkey.random;

import java.security.SecureRandom;
import java.util.Random;

import org.openqa.selenium.Dimension;

import io.appium.java_client.android.AndroidKeyCode;
import monkey.event.DragEvent;
import monkey.event.Event;
import monkey.event.EventQueue;
import monkey.event.EventSource;
import monkey.event.KeyEvent;
import monkey.event.MultiTouchEvent;
import monkey.event.TapEvent;
import monkey.event.ThrottleEvent;
import monkey.exception.TestFailureException;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import monkey.util.TestingOptions;
import reduction.event.InspectEvent;
import util.AndroidKeyCodeWrapper;
import util.Log;
import util.PointF;

/**
 * Generate random event source.
 * 
 * This class is adapted from the class MonkeySourceRandom of the Android Open Source Project.
 * 
 * @author echo
 */
public class RandomEventSource implements EventSource {
	/** Key events that move around the UI. */
	private static final int[] NAV_KEYS = {
			AndroidKeyCode.KEYCODE_DPAD_UP, AndroidKeyCode.KEYCODE_DPAD_DOWN,
			AndroidKeyCode.KEYCODE_DPAD_LEFT, AndroidKeyCode.KEYCODE_DPAD_RIGHT,
	};

	/**
	 * Key events that perform major navigation options (so shouldn't be sent
	 * as much).
	 */
	private static final int[] MAJOR_NAV_KEYS = {
			AndroidKeyCode.KEYCODE_MENU, /*KeyEvent.KEYCODE_SOFT_RIGHT,*/
			AndroidKeyCode.KEYCODE_DPAD_CENTER,
	};

	/** Key events that perform system operations. */
	private static final int[] SYS_KEYS = {
			/**
			 * Home key is not generated
			 * @author echo
			 */
			// AndroidKeyCode.KEYCODE_HOME, 
			AndroidKeyCode.KEYCODE_BACK,
			/**
			 * Call key opens contacts. 
			 * This event is not generated.
			 * @author echo
			 */
			// AndroidKeyCode.KEYCODE_CALL, 
			// End call key turns off the screen.
			// It is not generated.
			// AndroidKeyCode.KEYCODE_ENDCALL,
			AndroidKeyCode.KEYCODE_VOLUME_UP, AndroidKeyCode.KEYCODE_VOLUME_DOWN, AndroidKeyCode.KEYCODE_VOLUME_MUTE,
			AndroidKeyCode.KEYCODE_MUTE,
	};

	public static final int FACTOR_TOUCH        = 0;
	public static final int FACTOR_MOTION       = 1;
	public static final int FACTOR_PINCHZOOM    = 2;
	public static final int FACTOR_TRACKBALL    = 3;
	public static final int FACTOR_ROTATION     = 4;
	public static final int FACTOR_PERMISSION   = 5;
	public static final int FACTOR_NAV          = 6;
	public static final int FACTOR_MAJORNAV     = 7;
	public static final int FACTOR_SYSOPS       = 8;
	public static final int FACTOR_APPSWITCH    = 9;
	public static final int FACTOR_FLIP         = 10;
	public static final int FACTOR_ANYTHING     = 11;
	public static final int FACTORZ_COUNT       = 12;    // should be last+1

	private static final int GESTURE_TAP = 0;
	private static final int GESTURE_DRAG = 1;
	private static final int GESTURE_PINCH_OR_ZOOM = 2;
	/** percentages for each type of event.  These will be remapped to working
	 * values after we read any optional values.
	 **/
	private float[] mFactors = new float[FACTORZ_COUNT];
	private int mEventCount;  //total number of events generated so far
	private AppInfoWrapper mAppInfo;
	private Env env;
	private EventQueue mQ;
	private Random mRandom;
	private boolean errorOccured = false;

	public RandomEventSource(AppInfoWrapper appInfo, Env env, long seed) {
		// default values for random distributions
		// note, these are straight percentages, to match user input (cmd line args)
		// but they will be converted to 0..1 values before the main loop runs.
		mFactors[FACTOR_TOUCH] = 25.0f;
		mFactors[FACTOR_MOTION] = 15.0f;
		mFactors[FACTOR_PINCHZOOM] = 2.0f;
		mFactors[FACTOR_TRACKBALL] = 0.0f;
		// Adjust the values if we want to enable rotation by default.
		mFactors[FACTOR_ROTATION] = 0.0f;
		// disbale permission by default
		mFactors[FACTOR_PERMISSION] = 0.0f;
		mFactors[FACTOR_NAV] = 27.0f;
		mFactors[FACTOR_MAJORNAV] = 15.0f;
		mFactors[FACTOR_SYSOPS] = 2.0f;
		mFactors[FACTOR_APPSWITCH] = 0.0f;
		mFactors[FACTOR_FLIP] = 0.0f;
		mFactors[FACTOR_ANYTHING] = 14.0f;

		mEventCount = 0;
		this.mAppInfo = appInfo;
		this.env = env;
		mRandom = new SecureRandom();
		mRandom.setSeed((seed == 0) ? -1 : seed);
		mQ = new EventQueue();
	}
	/**
	 * Adjust the percentages (after applying user values) and then normalize to a 0..1 scale.
	 */
	public boolean adjustEventFactors() {
		// go through all values and compute totals for user & default values
		float userSum = 0.0f;
		float defaultSum = 0.0f;
		int defaultCount = 0;
		for (int i = 0; i < FACTORZ_COUNT; ++i) {
			if (mFactors[i] <= 0.0f) {   // user values are zero or negative
				userSum -= mFactors[i];
			} else {
				defaultSum += mFactors[i];
				++defaultCount;
			}
		}

		// if the user request was > 100%, reject it
		if (userSum > 100.0f) {
			Log.println("** Event weights > 100%");
			return false;
		}

		// if the user specified all of the weights, then they need to be 100%
		if (defaultCount == 0 && (userSum < 99.9f || userSum > 100.1f)) {
			Log.println("** Event weights != 100%");
			return false;
		}

		// compute the adjustment necessary
		float defaultsTarget = (100.0f - userSum);
		float defaultsAdjustment = defaultsTarget / defaultSum;

		// fix all values, by adjusting defaults, or flipping user values back to >0
		for (int i = 0; i < FACTORZ_COUNT; ++i) {
			if (mFactors[i] <= 0.0f) {   // user values are zero or negative
				mFactors[i] = -mFactors[i];
			} else {
				mFactors[i] *= defaultsAdjustment;
			}
		}

		// if verbose, show factors
		Log.println("// Event percentages:");
		for (int i = 0; i < FACTORZ_COUNT; ++i) {
			Log.println("//   " + i + ": " + mFactors[i] + "%");
		}

		// finally, normalize and convert to running sum
		float sum = 0.0f;
		for (int i = 0; i < FACTORZ_COUNT; ++i) {
			sum += mFactors[i] / 100.0f;
			mFactors[i] = sum;
		}
		return true;
	}

	/**
	 * set the factors
	 *
	 * @param factors percentages for each type of event
	 */
	public void setFactors(float factors[]) {
		int c = FACTORZ_COUNT;
		if (factors.length < c) {
			c = factors.length;
		}
		for (int i = 0; i < c; i++)
			mFactors[i] = factors[i];
	}

	public void setFactors(int index, float v) {
		mFactors[index] = v;
	}

	public void notifyError() {
		errorOccured = true;
	}

	/**
	 * Testing cycle
	 * @throws TestFailureException 
	 */
	public void runTestingCycles() throws TestFailureException {
		final int numberOfEvents = TestingOptions.v().getNumberOfEvents();
		int eventCounter = 0;
		adjustEventFactors();
		// Inject one more event so that the last one can be inspected.
		while(eventCounter <= numberOfEvents 
				&& ! errorOccured) {
			Event event = getNextEvent();
			event.setID(eventCounter);
			try {
				event.injectEvent(mAppInfo, env);
				Log.println(event);
				if(! (event instanceof InspectEvent || event instanceof ThrottleEvent)) {
					// Keep the event traces
					env.appendEvent(event);
					// ThrottleEvent and InspectEvent are not counted.
					eventCounter++;
				}
			} catch (TestFailureException e) {
				// TestFailureException is thrown to let the caller of this method to handle it.
				throw e;
			} 
			catch (Exception e) {
				Log.println("## Fail to inject the event " + event);
				e.printStackTrace();
				// Remove the inspecting and throttling events following the events that are failed to be injected
				while(mQ.peekFirst() instanceof InspectEvent || mQ.peekFirst() instanceof ThrottleEvent)
					mQ.removeFirst();
			}
		}
	}

	/**
	 * Generates a random motion event. This method counts a down, move, and up as multiple events.
	 *
	 * TODO:  Test & fix the selectors when non-zero percentages
	 * TODO:  Longpress.
	 * TODO:  Fling.
	 * TODO:  Meta state
	 * TODO:  More useful than the random walk here would be to pick a single random direction
	 * and distance, and divvy it up into a random number of segments.  (This would serve to
	 * generate fling gestures, which are important).
	 *
	 * @param random Random number source for positioning
	 * @param motionEvent If false, touch/release.  If true, touch/move/release.
	 *
	 */
	private void generatePointerEvent(Random random, int gesture) {
		// Obtain screen size
		Dimension dimension = env.dimension();
		PointF from = randomPoint(random, dimension);
		long downAt = System.currentTimeMillis();

		if(gesture == GESTURE_TAP) {
			mQ.addLast(new TapEvent(downAt, 0).addFrom(0, from));
		} else if(gesture == GESTURE_DRAG) {
			PointF move = randomSlop(random, from, dimension);
			mQ.addLast(new DragEvent(downAt, 0).addFromTo(0, from, move));
		} else if(gesture == GESTURE_PINCH_OR_ZOOM) {
			PointF move = randomSlop(random, from, dimension);
			PointF _from = randomPoint(random, dimension);
			PointF _move = randomSlop(random, _from, dimension);
			mQ.addLast(new MultiTouchEvent(downAt, 0).addFromTo(0, from, move).addFromTo(1, _from, _move));
		}
	}

	// Generate a random coordinate in current screen
	private PointF randomPoint(Random random, Dimension dimension) {
		return new PointF(random.nextInt(dimension.getWidth()), random.nextInt(dimension.getHeight()));
	}

	// Randomly walk some distance from current coordinate
	private PointF randomSlop(Random random, PointF p, Dimension dimension) {
		PointF vector = new PointF((random.nextFloat() - 0.5f) * 50, (random.nextFloat() - 0.5f) * 50);
		int count = random.nextInt(10);
		float x = p.x;
		float y = p.y;
		for (int i = 0; i < count; i++) {
			x = (float) Math.max(Math.min(x + random.nextFloat() * vector.x, dimension.getWidth() - 1), 0);
			y = (float) Math.max(Math.min(y + random.nextFloat() * vector.y, dimension.getHeight() - 1), 0);
		}
		// Log.println("# move to " + x + ", " + y);
		return new PointF(x, y);
	}

	/**
	 * generate a random event based on mFactor
	 */
	private void generateEvents() {
		float cls = mRandom.nextFloat();
		int lastKey = 0;

		if (cls < mFactors[FACTOR_TOUCH]) {
			generatePointerEvent(mRandom, GESTURE_TAP);
			return;
		} else if (cls < mFactors[FACTOR_MOTION]) {
			generatePointerEvent(mRandom, GESTURE_DRAG);
			return;
		} else if (cls < mFactors[FACTOR_PINCHZOOM]) {
			generatePointerEvent(mRandom, GESTURE_PINCH_OR_ZOOM);
			return;
		} 
		// These events are not generated.
		//		else if (cls < mFactors[FACTOR_TRACKBALL]) {
		//			return;
		//		} else if (cls < mFactors[FACTOR_ROTATION]) {
		//			return;
		//		} else if (cls < mFactors[FACTOR_PERMISSION]) {
		//			return;
		//		}

		// The remaining event categories are injected as key events
		// The remaining event categories are injected as key events
		if (cls < mFactors[FACTOR_NAV]) {
			lastKey = NAV_KEYS[mRandom.nextInt(NAV_KEYS.length)];
		} else if (cls < mFactors[FACTOR_MAJORNAV]) {
			lastKey = MAJOR_NAV_KEYS[mRandom.nextInt(MAJOR_NAV_KEYS.length)];
		} else if (cls < mFactors[FACTOR_SYSOPS]) {
			lastKey = SYS_KEYS[mRandom.nextInt(SYS_KEYS.length)];
		} 
		// These events are not generated.
		//		else if (cls < mFactors[FACTOR_APPSWITCH]) {
		//			MonkeyActivityEvent e = new MonkeyActivityEvent(mMainApps.get(
		//					mRandom.nextInt(mMainApps.size())));
		//			mQ.addLast(e);
		//			return;
		//		} 
		//		else if (cls < mFactors[FACTOR_FLIP]) {
		//			MonkeyFlipEvent e = new MonkeyFlipEvent(mKeyboardOpen);
		//			mKeyboardOpen = !mKeyboardOpen;
		//			mQ.addLast(e);
		//			return;
		//		} 
		else {
			lastKey = AndroidKeyCodeWrapper.v().generateRandomKey(mRandom);
		}
		KeyEvent e = new KeyEvent(lastKey);
		mQ.addLast(e);
	}

	public boolean validate() {
		//check factors
		return adjustEventFactors();
	}

	/**
	 * if the queue is empty, we generate events first
	 * @return the first event in the queue
	 */
	public Event getNextEvent() {
		if (mQ.isEmpty()) {
			generateEvents();
		}
		mEventCount++;
		Event e = mQ.getFirst();
		mQ.removeFirst();
		return e;
	}
}
