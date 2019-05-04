package reduction.event;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import monkey.event.Event;
import monkey.util.Env;

/**
 * This class is the base class of the events that inspecting testing status.
 * 
 * @author echo
 */
public abstract class InspectEvent extends Event {
	public InspectEvent() {
		super(Event.EVENT_INSPECT);
	}
	
	@Override
	public boolean isThrottlable() {
		return false;
	}
	
	/**
	 * return the class name of current activity
	 */
	protected String getCurrentActivity(Env env) {
		AndroidDriver<AndroidElement> driver = env.driver();
		assert driver.getCurrentPackage() != null;
		assert driver.currentActivity() != null;
		return driver.getCurrentPackage() + driver.currentActivity();
	}
};
