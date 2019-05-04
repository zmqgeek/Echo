package reduction.event;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.openqa.selenium.OutputType;

import monkey.util.AppInfoWrapper;
import monkey.util.Env;
import util.Log;

/**
 * This event takes a screenshot of current screen. Its overhead is approximate 0.8s on my machine.
 * This event can be used to check the side-effects of the previous events.
 * 
 * @author echo
 */
public class ScreenshotEvent extends InspectEvent {

	@Override
	public void injectEvent(AppInfoWrapper info, Env env) {
		try {
			byte[] imgBytes = env.driver().getScreenshotAs(OutputType.BYTES);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
			Log.println("# Height: " + img.getHeight());
			Log.println("# Width: " + img.getWidth());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public String toString() {
		return "[ScreenshotEvent]";
	}
}
