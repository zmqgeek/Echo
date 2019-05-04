package reduction.event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidKeyCode;
import monkey.event.KeyEvent;
import monkey.event.Throttle;
import monkey.event.ThrottleEvent;
import monkey.exception.TestFailureException;
import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import monkey.util.Logcat;
import reduction.ttg.TestingTraceGraph;
import reduction.ttg.node.NormalState;
import util.Config;
import util.Log;

/**
 * This event is used to inspect the current activity
 * 
 * This event can detects whether the testing has been distracted from the app being tested.
 * If that happens, we check whether it is caused by the exceptions and crashes via inspecting logcat entries. 
 * If true, testing terminates. If not, we try to return to the app being tested. 
 * We firstly try to press back button to return. If the app does not return, then the app is restarted. 
 * 
 * The app is restarted via directly launching the first activity (activity traces are kept during testing), 
 * which does not clean the previous data generated during testing. 
 * 
 * @author echo
 */
public class CheckActivityEvent extends InspectEvent {
	public CheckActivityEvent() {
		super();
	}

	@Override
	public void injectEvent(AppInfoWrapper info, Env env) throws TestFailureException {
		String curAct = getCurrentActivity(env);
		Log.println("# Current activity: " + curAct);
		if(curAct == null) {
			Log.println("Current activity is not available.");
			System.exit(0);
		} else if(info.contains(curAct)) {
			checkActivitySuccess(info, env);
		} else {
			// Testing has quit from the app being tested
			// Obtain logcat to see whether there are exceptions
			String log = Logcat.getLogAsString();
			if(! Logcat.isException(log)) {
				// No error happens. 
				/**
				 * Try to return to the app being tested by pressing back button.
				 * If it does not return to the app being tested, retest the app.
				 */
				pressingBackToReturnToTargetApp(info, env);
				if(! info.contains(getCurrentActivity(env))) {
					System.out.println("App exits. Restart the testing again.");
					Log.println("App exits. Restart the testing again.");
					throw new TestFailureException();
				} else {
					checkActivitySuccess(info, env);
				}
			} else if(Logcat.isException(log) && ActivityChecker.isErrorAndroidActivity(curAct)) {
				// Error occurs 
				/**
				 *  The error is not caused by app's activities, so that we test the app again.
				 *  This is achieved by raising a TestFailureException, which is handled by the main testing loop. 
				 */
				System.out.println("#### Error is triggered by an Android activity " + curAct);
				throw new TestFailureException();
			} else if(Logcat.isException(log) && ! ActivityChecker.isErrorAndroidActivity(curAct)) {
				// Error occurs
				// Pint the logcat info then stop testing
				Log.println("App error.");
				Log.println(log);
				env.eventSource().notifyError();
				// Insert an error state into TTG.
				NormalState lastNormalState = TestingTraceGraph.v().getLastNormalState();
				if(lastNormalState == null)
					Log.println("No event trace.");					
				else 
					TestingTraceGraph.v().addErrorState(lastNormalState, env.getLastEvent());
			} else {
				// skip
				Log.println("#### Unreachable here.");
			}
		}
	}

	@Override
	public String toString() {
		return "[CheckActivityEvent].";
	}

	// Try to return to the app being tested pressing the back button.
	private void pressingBackToReturnToTargetApp(AppInfoWrapper info, Env env) {
		for(int i = 0; i < 5; i++) {
			if(info.contains(getCurrentActivity(env)))
				break;
			new KeyEvent(AndroidKeyCode.BACK).injectEvent(info, env);
			new ThrottleEvent(Throttle.v().getThrottleDuration() * 2).injectEvent(info, env);
		}
	}

	private void checkActivitySuccess(AppInfoWrapper info, Env env) {
		// If current is in current app, then save it to the current activity trace
		env.appendActivity(env.driver().currentActivity());
		// Event has been successfully injected, obtain log so that it won't appear at next time
		Logcat.getLog();
	}
	
	// The app is exit during testing. Start the first activity in the activity transition trace.
	private void startFirstActivity(AppInfoWrapper info, Env env) {
		// If testing does not return to the app, we relaunch the app.
		String firstAct = env.getFirstActivity();
		String lastAct = env.getLastActivity();
		if(firstAct != null && firstAct.equals(lastAct)) {
			// Start the first activity during testing.
			// Using the launching app API all the app data are lost.
			// The app is relaunched via starting the first activity in the testing trace.
			Activity mainActivity = new Activity(info.getPkgName(), firstAct);
			env.driver().startActivity(mainActivity);
		}
		else {
			Log.println("# Warning: testing has been distracted from the app " + info.getPkgName());
			System.exit(0);
		}
	}

	/**
	 * ActivityChecker checks whether a given activity name is an activity from Android system that may trigger an error.
	 * This error are not considered as app bug.
	 */
	public static class ActivityChecker {
		private static Set<Pattern> errorAndroidActNamePatterns;
		static {
			errorAndroidActNamePatterns = new HashSet<>();
			try(BufferedReader reader = new BufferedReader(new FileReader(Config.v().get(Config.SYSACTS)))) {
				String temp = null;
				while((temp = reader.readLine()) != null)
					errorAndroidActNamePatterns.add(Pattern.compile(temp));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public static boolean isErrorAndroidActivity(String actName) {
			for(Pattern p : errorAndroidActNamePatterns)
				if(p.matcher(actName).matches())
					return true;
			return false;
		}
	} 
}
