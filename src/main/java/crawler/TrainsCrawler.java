package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
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

	public static final Pattern STATION_PATTERN = Pattern.compile(
			"<a href=\"/station/\\d+/\\?type=train\" class=\"Link ThreadTable__stationLink\" data-reactid=\"\\d+\"><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></a>");
	public static final Pattern ARRIVAL_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\" data-reactid=\"\\d+\"><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></div>");
	public static final Pattern STAY_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\" data-reactid=\"\\d+\"><span class=\"Duration\" data-reactid=\"\\d+\">([^<]*)</span></div>");
	public static final Pattern DEPARTURE_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\" data-reactid=\"\\d+\"><!-- react-text: \\d+ -->([^<]*)<!-- /react-text --></div>");
	public static final Pattern TRAVEL_TIME_PATTERN = Pattern.compile(
			"<div class=\"ThreadTable__wrapperInner\" data-reactid=\"\\d+\"><span class=\"Duration\" data-reactid=\"\\d+\">([^<]*)</span></div>");

	private static final CloseableHttpClient httpclient;
	private static final RequestConfig localConfig;

	static {
		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
		httpclient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
		localConfig = RequestConfig.copy(globalConfig).setCookieSpec(CookieSpecs.STANDARD).build();
	}

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
			String trainsHtml = getTrainsHtml(getTrainsUrl(cityFrom, cityTo));
			parseTrainsHtml(trainsHtml);
			// Thread.sleep(5000 + (long) (Math.random() * 5000));
		}
		logger.info("ENDDDD!!!!!");
	}

	public static void main(String[] args) {
		String trainsHtml = getTrainsHtml(getTrainsUrl("saint-petersburg", "cherepovets"));
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
			String timesHtml = getTrainsHtml("https://rasp.yandex.ru" + href);
			try {
				FileWriter fw = new FileWriter(
						"/home/misha-sma/Trains/yandex-crawler/" + idTrain + "_" + Math.random() + ".html");
				fw.write(timesHtml);
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(5000 + (long) (Math.random() * 10000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timesHtml=timesHtml.replace("\n", "");
					
//			String[] timeParts = timesHtml
//					.split("<tr class=\"ThreadTable__rowStation ThreadTable__rowStation_isStationFrom\"");
			String[] timeParts = timesHtml
					.split("ThreadTable__rowStation");
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
				if(station==null) {
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
				logger.info(station + "  " + arrivalTime + "  " + stayTime + "  " + departureTime + "  " + travelTime);
			}
		}
	}

	private static String getTrainsUrl(String cityFrom, String cityTo) {
		return "https://rasp.yandex.ru/train/" + cityFrom + "--" + cityTo;
	}

	private static String getTrainsHtml(String url) {
//		RequestConfig globalConfig = RequestConfig.custom()
//                .setCookieSpec(CookieSpecs.DEFAULT)
//                .build();
//        CloseableHttpClient httpclient = HttpClients.custom()
//                .setDefaultRequestConfig(globalConfig)
//                .build();
//        RequestConfig localConfig = RequestConfig.copy(globalConfig)
//                .setCookieSpec(CookieSpecs.STANDARD)
//                .build();
//        HttpGet httpGet = new HttpGet(url);
//        httpGet.setConfig(localConfig); 
		
		
		String trainsHtml = null;
//		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader(HttpHeaders.USER_AGENT,
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
			httpGet.setConfig(localConfig);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			trainsHtml = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			response.close();
//			httpclient.close();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return trainsHtml;
	}
}
