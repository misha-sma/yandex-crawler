package validator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawler.util.Util;

public class TimesValidator {
	private static final Logger logger = LoggerFactory.getLogger(TimesValidator.class);

	public static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+) мин");
	public static final Pattern HOURS_PATTERN = Pattern.compile("(\\d+) ч");
	public static final Pattern DAYS_PATTERN = Pattern.compile("(\\d+) дн");

	public static void validate() {
		String path = "/home/misha-sma/Trains/yandex-crawler/files/trains/";
		File folder = new File(path);
		for (File file : folder.listFiles()) {
			logger.info("validator file=" + file.getName());
			String text = Util.loadText(file);
			String[] lines = text.split("\n");
			int startIndex = 1;
			String departureDays = lines[1].trim();
			if (departureDays.equals("ежд") || departureDays.contains("пн") || departureDays.contains("вт")
					|| departureDays.contains("ср") || departureDays.contains("чт") || departureDays.contains("пт")
					|| departureDays.contains("сб") || departureDays.contains("вс")) {
				startIndex = 4;
			}
			int previuosTravelTime = -1;
			for (int i = startIndex; i < lines.length; ++i) {
				String[] parts = lines[i].split("\\|");
				if (parts.length < 2) {
					logger.info("TotalLinesCount=" + lines.length + "  ProcessedLinesCount=" + i);
					break;
				}
				if (parts[2].trim().equals("null")) {
					logger.info("TotalLinesCount=" + lines.length + "  ProcessedLinesCount=" + i);
					break;
				}
				int travelTime = 0;
				Matcher m = MINUTES_PATTERN.matcher(parts[2]);
				if (m.find()) {
					travelTime = Integer.parseInt(m.group(1));
				}
				m = HOURS_PATTERN.matcher(parts[2]);
				if (m.find()) {
					travelTime += 60 * Integer.parseInt(m.group(1));
				}
				m = DAYS_PATTERN.matcher(parts[2]);
				if (m.find()) {
					travelTime += 24 * 60 * Integer.parseInt(m.group(1));
				}
				if (travelTime <= previuosTravelTime) {
					logger.info(lines[i]);
				}
				previuosTravelTime = travelTime;
			}
		}
		logger.info("ENDDDDDDDDDD!!!!!!!");
	}
}
