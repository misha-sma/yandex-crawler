package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawler.util.Util;
import validator.TimesValidator;

public class TrainsParser {
	private static final Logger logger = LoggerFactory.getLogger(TrainsParser.class);

	public static final Pattern STATION_PATTERN = Pattern.compile(
			"<a href=\"(https://rasp.yandex.ru)?/station/\\d+/\\?type=train\" class=\"Link ThreadTable__stationLink\"( data-reactid=\"\\d+\")?><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></a>");
	public static final Pattern ARRIVAL_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\"( data-reactid=\"\\d+\")?><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></div>");
	public static final Pattern STAY_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\"( data-reactid=\"\\d+\")?><span class=\"Duration\"( data-reactid=\"\\d+\")?>([^<]*)</span></div>");
	public static final Pattern DEPARTURE_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\"( data-reactid=\"\\d+\")?><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></div>");
	public static final Pattern TRAVEL_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\"( data-reactid=\"\\d+\")?><span class=\"Duration\"( data-reactid=\"\\d+\")?>([^<]*)</span></div>");

	public static final Pattern DAY_PATTERN = Pattern.compile("(\\d+) дн");
	public static final Pattern STAY_TIME_PATTERN_4_DEP = Pattern
			.compile("<div class=\"ThreadTable__wrapperInner\"( data-reactid=\"\\d+\")?>([^<]*)</div>");

	public static final Pattern TIME_ZONE_PATTERN = Pattern.compile("Местное время МСК \\+(\\d+) ч");

	public static void main(String[] args) {
		String path = "/home/misha-sma/Trains/yandex-crawler/files/html/";
		File folder = new File(path);
		Map<String, String> countsMap = loadPeoplesCounts();
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

				String depTime = "";
				int depTimeMinutes = 0;
				int deltaTimeZone = 0;
				String[] timeParts = text.split("ThreadTable__rowStation");
				Matcher m = TIME_ZONE_PATTERN.matcher(timeParts[0]);
				if (m.find()) {
					deltaTimeZone = Integer.parseInt(m.group(1));
				}
				for (int j = 2; j < timeParts.length; ++j) {
					String station = null;
					String arrivalTime = null;
					String stayTime = null;
					String departureTime = null;
					String travelTime = null;
					int offset = 0;
					m = STATION_PATTERN.matcher(timeParts[j]);
					if (m.find()) {
						station = m.group(3);
						offset = m.end();
					}
					if (station == null) {
						deltaTimeZone = getDeltaTimeZone(timeParts[j], deltaTimeZone);
						continue;
					}
					if (j == 2) {
						depTime = parseDepartureTime(timeParts[j]);
						logger.info("depTimeLocal=" + depTime);
						depTimeMinutes = time2Minutes(depTime);
						depTimeMinutes -= 60 * deltaTimeZone;
						if (depTimeMinutes < 0) {
							depTimeMinutes = 60 * 24 + depTimeMinutes;
						}
						int hoursMsk = depTimeMinutes / 60;
						int minutesMsk = depTimeMinutes % 60;
						logger.info("depTimeMsk=" + hoursMsk + ":" + minutesMsk);
						String stationTrue = cleanStation(station);
						String counts = countsMap.get(stationTrue);
						counts = counts == null ? "0" : counts;
						fw.write(stationTrue + " | 0 | 0 | " + counts + "\n");
						continue;
					}
					m = ARRIVAL_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						arrivalTime = m.group(2);
						offset = m.end();
					}
					m = STAY_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						stayTime = m.group(3);
						offset = m.end();
					}
					m = DEPARTURE_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						departureTime = m.group(2);
						offset = m.end();
					}
					m = TRAVEL_TIME_PATTERN.matcher(timeParts[j]);
					if (m.find(offset)) {
						travelTime = m.group(3);
						if (travelTime.contains("дн") && arrivalTime != null) {
							Matcher mT = DAY_PATTERN.matcher(travelTime);
							if (mT.find()) {
								travelTime = mT.group();
							}
							int arrTimeMinutes = time2Minutes(arrivalTime);
							arrTimeMinutes -= 60 * deltaTimeZone;
							if (arrTimeMinutes < 0) {
								arrTimeMinutes = 60 * 24 + arrTimeMinutes;
							}
							int delta = arrTimeMinutes >= depTimeMinutes ? arrTimeMinutes - depTimeMinutes
									: arrTimeMinutes + 24 * 60 - depTimeMinutes;
							int hours = delta / 60;
							int minutes = delta % 60;
							travelTime += " " + hours + " ч " + minutes + " мин";
						}
					}
					deltaTimeZone = getDeltaTimeZone(timeParts[j], deltaTimeZone);
					String stationTrue = cleanStation(station);
					String counts = countsMap.get(stationTrue);
					counts = counts == null ? "0" : counts;
					fw.write(stationTrue + " | " + stayTime + " | " + travelTime + " | " + counts + "\n");
				}
				fw.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		TimesValidator.validate();
		logger.info("ENDDDD!!!!");
	}

	private static String cleanStation(String station) {
		return station.replace("-1", "").replace("-2", "").replace("-Пасс.", "").replace("-главный", "")
				.replaceAll("\\([^в]+вокзал\\)", "").trim();
	}

	private static int getDeltaTimeZone(String html, int deltaTimeZone) {
		Matcher m = TIME_ZONE_PATTERN.matcher(html);
		if (m.find()) {
			deltaTimeZone = Integer.parseInt(m.group(1));
		} else if (html.contains("Московское время")) {
			deltaTimeZone = 0;
		}
		return deltaTimeZone;
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

	private static int time2Minutes(String time) {
		if (!time.matches("\\d{2}:\\d{2}")) {
			logger.error("time=" + time);
			return -1;
		}
		String[] parts = time.split(":");
		return Integer.parseInt(parts[1]) + 60 * Integer.parseInt(parts[0]);
	}

	private static String parseDepartureTime(String html) {
		int offset = 0;
		Matcher m = STATION_PATTERN.matcher(html);
		if (m.find()) {
			offset = m.end();
		}
		m = ARRIVAL_TIME_PATTERN.matcher(html);
		if (m.find(offset)) {
			offset = m.end();
		}
		m = STAY_TIME_PATTERN_4_DEP.matcher(html);
		if (m.find(offset)) {
			offset = m.end();
		}
		m = DEPARTURE_TIME_PATTERN.matcher(html);
		if (m.find(offset)) {
			return m.group(2).trim();
		}
		logger.error("Departure time not found!!!");
		return null;
	}

	private static Map<String, String> loadPeoplesCounts() {
		String path = "/home/misha-sma/Trains/perepis-2010-true.txt";
		String text = Util.loadText(path);
		String[] lines = text.split("\n");
		Map<String, String> countsMap = new HashMap<String, String>();
		for (String line : lines) {
			String[] parts = line.split("\\|");
			if (parts.length != 2) {
				continue;
			}
			String name = parts[0].trim();
			String counts = parts[1].trim();
			countsMap.put(name, counts);
		}
		return countsMap;
	}
}
