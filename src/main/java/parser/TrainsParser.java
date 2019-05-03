package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawler.util.Util;

public class TrainsParser {
	private static final Logger logger = LoggerFactory.getLogger(TrainsParser.class);

	public static final Pattern STATION_PATTERN = Pattern.compile(
			"<a href=\"https://rasp.yandex.ru/station/\\d+/\\?type=train\" class=\"Link ThreadTable__stationLink\"><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></a>");
	public static final Pattern ARRIVAL_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\"><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></div>");
	public static final Pattern STAY_TIME_PATTERN = Pattern
			.compile("<div class=\"ThreadTable__wrapperInner\"><span class=\"Duration\">([^<]*)</span></div>");
	public static final Pattern DEPARTURE_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\"><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></div>");
	public static final Pattern TRAVEL_TIME_PATTERN = Pattern
			.compile("<div class=\"ThreadTable__wrapperInner\"><span class=\"Duration\">([^<]*)</span></div>");

	public static void main(String[] args) {
		String path = "/home/misha-sma/Trains/yandex-crawler/files/html/";
		File folder = new File(path);
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}
			logger.info("file=" + file.getName());
			String text = Util.loadText(file).replace("\n", "");
			int idTrain = parseIdTrain(file);
			try {
				FileWriter fw = new FileWriter("/home/misha-sma/Trains/yandex-crawler/files/trains/"
						+ file.getName().replace(".html", ".txt"));
				fw.write(String.valueOf(idTrain));
				fw.write("\n");

				String[] timeParts = text.split("ThreadTable__rowStation");
				for (int j = 2; j < timeParts.length; ++j) {
					String station = null;
					String arrivalTime = null;
					String stayTime = null;
					String departureTime = null;
					String travelTime = null;
					int offset = 0;
					Matcher m = STATION_PATTERN.matcher(timeParts[j]);
					if (m.find()) {
						station = m.group(1);
						offset = m.end();
					}
					if (station == null) {
						continue;
					}
					m = ARRIVAL_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						arrivalTime = m.group(1);
						offset = m.end();
					}
					m = STAY_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						stayTime = m.group(1);
						offset = m.end();
					}
					m = DEPARTURE_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						departureTime = m.group(1);
						offset = m.end();
					}
					m = TRAVEL_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						travelTime = m.group(1);
					}
					fw.write(station + " | " + stayTime + " | " + travelTime + "\n");
				}
				fw.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		System.out.println("ENDDDD!!!!");
	}

	private static int parseIdTrain(File file) {
		String fileName = file.getName();
		for (int i = 0; i < fileName.length(); ++i) {
			char c = fileName.charAt(i);
			if (!Character.isDigit(c)) {
				return Integer.parseInt(fileName.substring(0, i));
			}
		}
		return -1;
	}

}
