package util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.appium.java_client.android.AndroidKeyCode;
import soot.toolkits.scalar.Pair;

/**
 * This class converts key code to corresponding key name.
 * This class stores the pairs of key code and its name.
 * 
 * @author echo
 */
public class AndroidKeyCodeWrapper {
	public static AndroidKeyCodeWrapper v() {
		return SingletonHolder.singleton;
	}

	public int size() {
		return list.size();
	}

	public List<Pair<Integer, String>> getList() {
		return list;
	}

	public String getKeyCodeName(int i) {
		return keyCode2KeyName.get(i);
	}

	public Pair<Integer, String> get(int i) {
		assert i >= 0 && i < list.size();
		return list.get(i);
	}

	// Generate random key code
	public int generateRandomKey(Random random) {
		return get(random.nextInt(size())).getO1();
	}
	
	private AndroidKeyCodeWrapper() {
		list = new ArrayList<>();
		keyCode2KeyName = new HashMap<>();
		// Obtain all the key codes via reflection
		try {
			for(Field f : Class.forName("io.appium.java_client.android.AndroidKeyCode").getFields()) {
				int keyCode = f.getInt(null);
				String name = f.getName();
				if(FUNCTION_KEYS.contains(keyCode))
					continue;
				// Home key event is not generated
				if(name.startsWith("KEYCODE")) {
					list.add(new Pair<>(keyCode, name));
					keyCode2KeyName.put(keyCode, name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		// Sort the key code and name pair according to the key code
//		list.sort(new Comparator<Pair<Integer, String>>() {
//			@Override
//			public int compare(Pair<Integer, String> x, Pair<Integer, String> y) {
//				return x.getO1().compareTo(y.getO1());
//			}
//		});
		assert list.size() == keyCode2KeyName.size();
	}

	private List<Pair<Integer, String>> list;
	private Map<Integer, String> keyCode2KeyName;
	
	// Some keys are excluded for test case generation.
	// These keys are function keys, like launching other apps or turning off the screen.
	// They are unnecessary to be generated.
	private static final Set<Integer> FUNCTION_KEYS;
	static {
		FUNCTION_KEYS = new HashSet<>(Arrays.asList( new Integer[] {
				AndroidKeyCode.KEYCODE_HOME,
				AndroidKeyCode.KEYCODE_CALCULATOR,
				AndroidKeyCode.KEYCODE_CALENDAR,
				AndroidKeyCode.KEYCODE_CONTACTS,
				AndroidKeyCode.KEYCODE_MUSIC,
				AndroidKeyCode.KEYCODE_ENVELOPE,
				AndroidKeyCode.KEYCODE_EXPLORER,
				AndroidKeyCode.KEYCODE_CALL,
				AndroidKeyCode.KEYCODE_ENDCALL,
				AndroidKeyCode.KEYCODE_POWER,
				AndroidKeyCode.KEYCODE_APP_SWITCH,
				AndroidKeyCode.KEYCODE_ESCAPE,
				AndroidKeyCode.KEYCODE_BUTTON_B,
		}));
	}

	/**
	 * Multithread-safe singleton holder.
	 */
	private static class SingletonHolder {
		private static AndroidKeyCodeWrapper singleton = new AndroidKeyCodeWrapper();
	}
}
