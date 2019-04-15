package crawler.util;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	private static final Logger logger = LoggerFactory.getLogger(Util.class);

	private Util() {
	}

	public static String loadText(String path) {
		try (FileInputStream in = new FileInputStream(path)) {
			return IOUtils.toString(in, "UTF-8");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
