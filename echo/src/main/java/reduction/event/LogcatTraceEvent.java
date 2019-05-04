package reduction.event;

import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import monkey.util.Logcat;
import util.Log;

/**
 * This class obtains logcat trace during replaying. 
 * 
 * @author echo
 */
public class LogcatTraceEvent extends InspectEvent {
	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		String log = Logcat.getLogAsString();
		if(Logcat.isException(log))
			Log.println(log);
	}

	@Override
	public String toString() {
		return "[LogcatTraceEvent].";
	}
}
