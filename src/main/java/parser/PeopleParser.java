package parser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawler.util.Util;

public class PeopleParser {
	private static final Logger logger = LoggerFactory.getLogger(PeopleParser.class);

	public static final Pattern LOCALITY_PATTERN = Pattern
			.compile("(г\\.|пгт|посѐлок|поселок|деревня|село|хутор|станица|слобода) ([^\\d]+)(\\d+)");

	public static final Pattern LOCALITY_PATTERN_DIGITS = Pattern
			.compile("(г\\.|пгт|посѐлок|поселок|деревня|село|хутор|станица|слобода) ([^<]+)");

	public static final Pattern COUNT_PATTERN = Pattern.compile(" {3}(\\d{3,}) {3}");

	public static void main(String[] args) {
		long initTime = System.currentTimeMillis();
		String path = "/home/misha-sma/Trains/pub-01-05-perepis-2010.txt";
		String text = Util.loadText(path);
		String[] lines = text.split("\n");
		Map<String, List<Integer>> countMap = new HashMap<String, List<Integer>>();
		for (String line : lines) {
			Matcher m = LOCALITY_PATTERN.matcher(line);
			if (m.find()) {
				String name = m.group(2);
				int count = Integer.parseInt(m.group(3));
				if (count < 100) {
					Matcher mCount = COUNT_PATTERN.matcher(line);
					if (mCount.find()) {
						count = Integer.parseInt(mCount.group(1));
						Matcher mDigits = LOCALITY_PATTERN_DIGITS.matcher(line.substring(0, mCount.start()));
						if (mDigits.find()) {
							name = mDigits.group(2);
						}
					}
				}
				name = name.replace("(рц)", "").replace(" рп ", "").replace(" дп ", "").replace(" кп ", "")
						.replace("- городское население", "").trim().replace('ѐ', 'ё').replace('Ѐ', 'Ё');
				List<Integer> value = countMap.get(name);
				if (value == null) {
					value = new LinkedList<Integer>();
					value.add(count);
					countMap.put(name, value);
					continue;
				}
				value.add(count);
			}
		}
		try (FileWriter fw = new FileWriter("/home/misha-sma/Trains/perepis-2010-true-222.txt")) {
			for (String name : countMap.keySet()) {
				List<Integer> counts = countMap.get(name);
				fw.write(name + " | ");
				int i = 0;
				for (Integer count : counts) {
					if (i > 0) {
						fw.write(", ");
					}
					fw.write(String.valueOf(count));
					++i;
				}
				fw.write("\n");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("ENDDDDDD!!!!!!! Time=" + (System.currentTimeMillis() - initTime) + " ms");
	}
}
