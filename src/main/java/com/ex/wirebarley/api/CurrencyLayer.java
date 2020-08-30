package com.ex.wirebarley.api;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class CurrencyLayer {

	private static final String 		TARGET_URL 		= "http://apilayer.net/api/";
	private static final String 		ACCESS_KEY 		= "37482796245ba05928d801956a2a35fb";
	private static final String 		END_POINT 		= "live";
	private static final DecimalFormat 	MONEY_FORMAT 	= new DecimalFormat("#,##0.00");

	private static Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);

	// Api 호출 메서드
	private static JSONObject restApiCall(String currencies) {

		JSONObject resultJson = new JSONObject();

		try {
			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(5000);	//타임아웃 설정 5초
			factory.setReadTimeout(5000);		//타임아웃 설정 5초

			RestTemplate restCallApiTemplate = new RestTemplate(factory);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));    //Response Header to UTF-8

			UriComponents builder = UriComponentsBuilder.fromHttpUrl(TARGET_URL + END_POINT)
					.queryParam("access_key", ACCESS_KEY)
					.queryParam("currencies", currencies)
					.build(false);    //자동으로 encode해주는 것을 막기 위해 false

			ResponseEntity<String> result = restCallApiTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), String.class);
			resultJson = (JSONObject)new JSONParser().parse(result.getBody());

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			logger.error("CurrencyLayer API Error : restApiCall" + e.getMessage());

			JSONObject errorJson = new JSONObject();
			errorJson.put("code", e.getRawStatusCode());
			errorJson.put("info"  , e.getStatusText());

			resultJson.put("success", false);
			resultJson.put("error", errorJson);
		} catch (Exception e) {
			logger.error("CurrencyLayer API Error : restApiCall" + e.getMessage());

			JSONObject errorJson = new JSONObject();
			errorJson.put("code", "999");
			errorJson.put("info"  , "Exception 오류");

			resultJson.put("success", false);
			resultJson.put("error", errorJson);
		}

		logger.debug(resultJson.toJSONString());
		return resultJson;
	}

	/**
	 * 선택한 param 국가의 USD 환율 조회
	 * @param currencies : 선택한 국가 코드
	 * @return api 통신 성공 : 횐율 정보 / 실패: 통신 실패 정보
	 */
	public static Map<String, Object> getRateFromCurrencyLayer(JSONObject jsonParam) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (jsonParam.containsKey("currencies")) {
			String currencies = jsonParam.get("currencies").toString();
			JSONObject jsonObj = restApiCall(currencies);

			resultMap.put("success", jsonObj.get("success"));

			if ((boolean) resultMap.get("success")) {
				resultMap.put("timestamp", jsonObj.get(("timestamp")));
				resultMap.put("rate", MONEY_FORMAT.format(((JSONObject) jsonObj.get("quotes")).get("USD" + currencies)));
				resultMap.put("currencies", currencies + "/USD");
			} else {
				resultMap.put("msg", ((JSONObject) jsonObj.get("error")).get("info"));
				resultMap.put("code", ((JSONObject) jsonObj.get("error")).get("code"));
			}
		} else {
			resultMap.put("msg", "[ParamError]: Required param is 'currencies'");
			resultMap.put("success", false);
			resultMap.put("code", "800");
		}

		logger.info(resultMap.toString());
		return resultMap;
	}

	/**
	 * 선택한 param 국가의 USD 환율 조회 및 수취금액 계산
	 * @param jsonParam : {currencies = 국가 코드, money = 환전 금액}
	 * @return api 통신 성공 : 횐율 정보 / 실패: 통신 실패 정보
	 */
	public static Map<String, Object> getCalculateFromCurrencyLayer(JSONObject jsonParam) {

		Map<String, Object> resultMap = null;
		if (jsonParam.containsKey("currencies") && jsonParam.containsKey("money")) {
			resultMap = getRateFromCurrencyLayer(jsonParam);

			if ((boolean) resultMap.get("success")) {
				try {
					BigDecimal rate = new BigDecimal(resultMap.get("rate").toString().replace(",", ""));
					BigDecimal money = new BigDecimal(jsonParam.get("money").toString());

					if (money.compareTo(new BigDecimal(10000)) > 0) throw new Exception("Money is too Big");
					else if (money.compareTo(new BigDecimal(0)) <= 0) throw new Exception("Money is too Small");

					resultMap.put("calculate", MONEY_FORMAT.format(money.multiply(rate)));
				} catch (Exception e) {

					resultMap.put("success", false);
					resultMap.put("msg", e.getMessage());
					resultMap.put("code", "900");
				}
			}
		} else {
			resultMap = new HashMap<String, Object>();

			resultMap.put("msg", "[ParamError]: Required param is 'currencies' and 'money'");
			resultMap.put("success", false);
			resultMap.put("code", "800");
		}

		logger.info(resultMap.toString());
		return resultMap;
	}
}