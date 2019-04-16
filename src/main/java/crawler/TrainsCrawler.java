package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawler.util.Util;

public class TrainsCrawler {
	private static final Logger logger = LoggerFactory.getLogger(TrainsCrawler.class);

	public static final Pattern HREF_PATTERN = Pattern.compile("<a href=\"(/thread/([^\"]+))\"");
	public static final Pattern TRAIN_PATTERN = Pattern
			.compile("<span class=\"SegmentTitle__title[^>]*\">([^<]+)</span>");
	public static final Pattern TRAIN_NUMBER_PATTERN = Pattern
			.compile("<span class=\"SegmentTitle__number[^>]*\">([^<]+)</span>");
	public static final Pattern DEPARTURE_DAYS_PATTERN = Pattern.compile(
			"<div class=\"SearchSegment__scheduleDays[^>]*\"><!-- react-text: \\d+ -->([^<]+)<!-- /react-text --></div>");
	public static final Pattern TRAIN_NAME_PATTERN = Pattern.compile(
			"<span class=\"SegmentTransport__item SegmentTransport__item_deluxeTrain\"[^>]*>[^«]+«([^»]+)»</span>");

	public static void main6(String[] args) {
		String str = "sdsd <div class=\"SearchSegment__scheduleDays\"><!-- react-text: 1127 -->16, 19, 22, 25, 27 Апреля и в др. дни<!-- /react-text --></div> sdsds";
		Matcher m = DEPARTURE_DAYS_PATTERN.matcher(str);
		if (m.find()) {
			System.out.println(m.group(1));
		}
	}

	public static void main5(String[] args) {
		String str = "sdsd <span class=\"SegmentTitle__number\">078Я</span> sdsds";
		Matcher m = TRAIN_NUMBER_PATTERN.matcher(str);
		if (m.find()) {
			System.out.println(m.group(1));
		}
	}

	public static void main4(String[] args) {
		String str = "sdsd <span class=\"SegmentTitle__title\">Санкт-Петербург&nbsp;— Воркута</span> sdsds";
		Matcher m = TRAIN_PATTERN.matcher(str);
		if (m.find()) {
			System.out.println(m.group(1));
		}
	}

	public static void main3(String[] args) {
		String str = "sdsd <a href=\"/thread/078YA_2_2?departure_from=2019-04-16+10%3A20%3A00&amp;station_from=9602499&amp;station_to=9604211\" sdsds";
		Matcher m = HREF_PATTERN.matcher(str);
		if (m.find()) {
			System.out.println(m.group(1));
		}
	}

	public static void main2(String[] args) {
		String cityFrom = "saint-petersburg";
		String path = "/home/misha-sma/Trains/yandex-crawler/cities-eng.txt";
		String text = Util.loadText(path);
		String[] cities = text.split("\n");
		for (String city : cities) {
			city = city.trim();
			if (city.isEmpty()) {
				continue;
			}
			int index = city.indexOf('|');
			if (index <= 0) {
				logger.error("Delimeter index<=0!!! city=" + city);
				continue;
			}
			String cityTo = city.substring(index + 1);
			logger.info("cityTo=" + cityTo);
			String trainsHtml = getTrainsHtml(cityFrom, cityTo);
			parseTrainsHtml(trainsHtml);
			// Thread.sleep(5000 + (long) (Math.random() * 5000));
		}
		logger.info("ENDDDD!!!!!");
	}

	public static void main(String[] args) {
		String trainsHtml = getTrainsHtml("saint-petersburg", "cherepovets");
		try {
			FileWriter fw = new FileWriter("/home/misha-sma/Trains/yandex-crawler/hhhh.html");
			fw.write(trainsHtml);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parseTrainsHtml(trainsHtml);
		System.out.println("ENDDDD!!!!");
	}

	private static void parseTrainsHtml(String trainsHtml) {
		// String[] trains = trainsHtml.split(
		// "<article class=\"SearchSegment SearchSegment_isNotInterval
		// SearchSegment_isNotGone SearchSegment_isVisible\">");
		String[] trains = trainsHtml
				.split("SearchSegment SearchSegment_isNotInterval SearchSegment_isNotGone SearchSegment_isVisible");

		for (int i = 1; i < trains.length; ++i) {
			String idTrain = null;
			String train = null;
			String depDays = null;
			String href = null;
			String name = null;
			Matcher m1 = TRAIN_NUMBER_PATTERN.matcher(trains[i]);
			if (m1.find()) {
				idTrain = m1.group(1);
			}
			Matcher m2 = TRAIN_PATTERN.matcher(trains[i]);
			if (m2.find()) {
				train = m2.group(1);
			}
			Matcher m3 = DEPARTURE_DAYS_PATTERN.matcher(trains[i]);
			if (m3.find()) {
				depDays = m3.group(1);
			}
			Matcher m4 = HREF_PATTERN.matcher(trains[i]);
			if (m4.find()) {
				href = m4.group(1);
			}
			Matcher m5 = TRAIN_NAME_PATTERN.matcher(trains[i]);
			if (m5.find()) {
				name = m5.group(1);
			}
			logger.info(
					"id=" + idTrain + " train=" + train + " name=" + name + " depDays=" + depDays + " href=" + href);
		}
	}

	private static String getTrainsHtml(String cityFrom, String cityTo) {
		String trainsHtml = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet("https://rasp.yandex.ru/train/" + cityFrom + "--" + cityTo);
			httpGet.setHeader(HttpHeaders.USER_AGENT,
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			trainsHtml = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			response.close();
			httpclient.close();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return trainsHtml;
	}
}
