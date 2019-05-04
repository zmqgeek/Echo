package monkey.event;

import java.time.Duration;

/**
 * A fix time delay between event. Default value is 100ms.
 * 
 * @author echo
 */
public class Throttle {
	public long getThrottleDuration() {
		return throttleDuration;
	}

	public Duration getDuration() {
		return duration;
	}
	
	public void init(long millis) {
		throttleDuration = millis;
		duration = Duration.ofMillis(millis);
	}
	
	private Throttle() {
		throttleDuration = 100;
		duration = Duration.ofMillis(throttleDuration);
	}
	
	public static Throttle v() {
		return SingletonHolder.singleton;
	}
	
	private long throttleDuration;
	
	private Duration duration;
	
	/**
	 * Multithread-safe singleton holder.
	 */
	private static class SingletonHolder {
		private static final Throttle singleton = new Throttle();
	}
}
