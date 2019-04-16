package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawler.util.Util;

public class CitiesEngCrawler {
	private static final Logger logger = LoggerFactory.getLogger(CitiesEngCrawler.class);

	public static void main(String[] args) {
		String path = "/home/misha-sma/Trains/yandex-crawler/cities-true.txt";
		String pathOut = "/home/misha-sma/Trains/yandex-crawler/cities-eng.txt";
		String text = Util.loadText(path);
		String[] cities = text.split("\n");
		try (FileWriter fw = new FileWriter(pathOut)) {
			for (String city : cities) {
				city = city.trim();
				if (city.isEmpty()) {
					continue;
				}
				String cityEng = getCityEng(city);
				if (cityEng == null || cityEng.isEmpty()) {
					logger.error("CityEng is null!!!");
				}
				logger.info(city + "|" + cityEng);
				fw.write(city + "|" + cityEng + "\n");
				Thread.sleep(5000 + (long) (Math.random() * 5000));
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("ENDDDD!!!!!");
	}

	private static String getCityEng(String city) {
		String cityEng = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet(
					"https://suggests.rasp.yandex.net/all_suggests?field=from&format=old&lang=ru&national_version=ru&part="
							+ URLEncoder.encode(city, "UTF8"));
			httpGet.setHeader(HttpHeaders.USER_AGENT,
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			JSONArray array = new JSONArray(json);
			JSONArray citiesArray = array.getJSONArray(1);
			JSONArray values = citiesArray.getJSONArray(0);
			cityEng = values.getString(values.length() - 1);
			EntityUtils.consume(entity);
			response.close();
			httpclient.close();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return cityEng;
	}
}
