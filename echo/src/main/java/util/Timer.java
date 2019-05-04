package util;

/**
 * Timer class.
 * 
 * @author echo
 */
public class Timer {
	private long startTime;
	private long endTime;
	
	public Timer() {
		startTime = -1;
		endTime = -1;
	}
	
	public void start() {
		startTime = System.nanoTime();
	}
	
	public void stop() {
		endTime = System.nanoTime();
	}
	
	public double getDurationInSecond() {
		assert startTime != -1;
		assert endTime != -1;
		return (endTime - startTime) / 1E9;
	}
}
