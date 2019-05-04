package reduction.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class accepts image as a byte array and dumps it to file.
 * 
 * @author echo
 */
public class ImageDumper {
	public static final String PNG = "png";
	
	public static void dumpImage(final byte[] bytes, final String outputDirName, final String fileName) {
		assert bytes != null;
		assert bytes.length != 0;
		File outputFile = new File(genFileName(outputDirName, fileName, PNG));
		BufferedImage img = null;
		try {
			img = ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		assert img != null;
		try {
			ImageIO.write(img, PNG, outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String genFileName(String outputDir, String fileName, String extension) {
		return String.join(File.separator, outputDir, fileName + "." + extension);
	}
}
