package monkey.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import monkey.event.Throttle;
import util.AppPathResolver;
import util.Config;
import util.Log;

/**
 * Process testing options.
 * 
 * @author echo
 */
public class TestingOptions {
	private String emulatorName = "";

	private List<String> appPaths = null;

	private int throttle = 500;

	private int numberOfEvents = 5000;

	private int seed = 0;

	private boolean replay = false;

	private int portNumber = 4723;
	
	private boolean takeScreenshot = false;
	
	private boolean slidingWindowModel = false;

	public String getEmulatorName() {
		return emulatorName;
	}

	public List<String> getAppPaths() {
		return appPaths;
	}

	public int getThrottle() {
		return throttle;
	}

	public int getNumberOfEvents() {
		return numberOfEvents;
	}

	public void setNumberOfEvents(int numberOfEvents) {
		this.numberOfEvents = numberOfEvents;
	}

	public int getSeed() {
		return seed;
	}

	public void setRandomSeed() {
		seed = ThreadLocalRandom.current().nextInt();
	}

	public boolean isReplay() {
		return replay;
	}

	public int getPortNumber() {
		return portNumber;
	}
	
	public void setPortNumber(int _portNumber) {
		portNumber = _portNumber;
	}
	
	public boolean takeScreenshot() {
		return takeScreenshot;
	}
	
	public void setTakeScreenshot(boolean _takeScreenshot) {
		takeScreenshot = _takeScreenshot;
	}
	
	public boolean slidingWindowModel() {
		return slidingWindowModel;
	}
	
	private TestingOptions() {}

	public static final TestingOptions v() {
		return SingletonHolder.singleton;
	}

	public void processOptions(String[] args) {
		for(int i = 0; i < args.length; i++) {
			String argument = args[i];
			// process emulator name
			if(argument.equals("-emulator")) {
				emulatorName = args[i + 1];
			}
			// process app paths
			else if(argument.equals("-app")) {
				List<Integer> appIds = new ArrayList<>();
				for(int j = i + 1; j < args.length; j++) {
					try {
						appIds.add(Integer.valueOf(args[j]));
					} catch (Exception e) {
						break;
					}
				}
				appPaths = AppPathResolver.resolveAppPaths(Config.v().get(Config.APPDIR), appIds);
			}
			// process throttle time
			else if(argument.equals("-throttle")) {
				try {
					throttle = Integer.valueOf(args[i + 1]);
					Throttle.v().init(throttle);
				} catch (Exception e) {
					Log.println(args[i + 1] + " is not a valid throttle time. Use 500 ms.");
				}
			}
			// process the number of test cases
			else if(argument.equals("-event")) {
				try {
					numberOfEvents = Integer.valueOf(args[i + 1]);
				} catch (Exception e) {
					Log.println(args[i + 1] + " is not a valid integer. Inject 5000 events.");
				}
			}
			// process the seed
			else if(argument.equals("-seed")) {
				try {
					seed = Integer.valueOf(args[i + 1]);
				} catch (Exception e) {
					Log.println(args[i + 1] + " is not a valid integer. Use 0 as seed.");
				}
			}
			// whether replay the error
			else if(argument.equals("-replay")) {
				replay = true;
			}
			// port number of the Appium server
			else if(argument.equals("-port")) {
				try {
					portNumber = Integer.valueOf(args[i + 1]);
				} catch (Exception e) {
					Log.println(args[i + 1] + " is not a valid integer. Use 4723 as port number.");
				}
			}
			else if(argument.equals("-screenshot")) {
				takeScreenshot = true;
			} else if(argument.equals("-sliding")) {
				slidingWindowModel = true;
			}
		}
		// assert emulatorName != null;
		assert appPaths != null && ! appPaths.isEmpty();
	}

	// Dump all the options
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		try {
			Field flds[] = TestingOptions.class.getDeclaredFields();
			for(Field fld : flds) {
				buffer.append(fld.getName());
				buffer.append(": ");
				buffer.append(fld.get(this).toString());
				buffer.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	/**
	 * Multithread-safe singleton holder.
	 */
	private static class SingletonHolder {
		private static final TestingOptions singleton = new TestingOptions();
	}
}