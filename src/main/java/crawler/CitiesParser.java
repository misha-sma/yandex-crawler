package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawler.util.Util;

public class CitiesParser {
	private static final Logger logger = LoggerFactory.getLogger(CitiesParser.class);

	public static void main(String[] args) {
		String path = "/home/misha-sma/Trains/yandex-crawler/cities-all.txt";
		String pathOut = "/home/misha-sma/Trains/yandex-crawler/cities-true-all.txt";
		String text = Util.loadText(path);
		Pattern pattern = Pattern.compile("г\\. (([А-Я]|[а-я]|-|ё|Ё| )+)	");
		Matcher m = pattern.matcher(text);
		try (FileWriter fw = new FileWriter(pathOut)) {
			while (m.find()) {
				String city = m.group(1);
				fw.write(city + "\n");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("ENDDDD!!!!!");
	}
}
