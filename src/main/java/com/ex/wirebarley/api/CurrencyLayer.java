package com.ex.wirebarley.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

	private static final String TARGET_URL 	= "http://apilayer.net/api/";
	private static final String ACCESS_KEY 	= "3c85b8f9e2747eca37ea4c0de66c41ac";
	private static final String END_POINT 	= "live";

	private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

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
			resultJson.put("code", e.getRawStatusCode());
			resultJson.put("msg"  , e.getStatusText());
			resultJson.put("success", false);
		} catch (Exception e) {
			resultJson.put("code", "999");
			resultJson.put("msg"  , "Exception 오류");
			resultJson.put("success", false);
		}

		return resultJson;
	}

	/**
	 * 선택한 param 국가의 USD 환율 조회
	 * @param currencies : 선택한 국가 코드
	 * @return api 통신 성공 : 횐율 정보 / 실패: 통신 실패 정보
	 */
	public static Map<String, Object> getRateFromCurrencyLayer(String currencies) {

		JSONObject jsonObj = restApiCall(currencies);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("success", jsonObj.get("success"));

		if ((boolean)resultMap.get("success")) {
			resultMap.put("timestamp", jsonObj.get(("timestamp")));
			resultMap.put("rate", MONEY_FORMAT.format(((JSONObject)jsonObj.get("quotes")).get("USD" + currencies)));
			resultMap.put("currencies", currencies + "/USD");
		} else {
			resultMap.put("msg", jsonObj.get("msg"));
			resultMap.put("code", jsonObj.get("code"));
		}

		return resultMap;
	}

	/**
	 * 선택한 param 국가의 USD 환율 조회 및 수취금액 계산
	 * @param jsonParam : {currencies = 국가 코드, money = 환전 금액}
	 * @return api 통신 성공 : 횐율 정보 / 실패: 통신 실패 정보
	 */
	public static Map<String, Object> getCalculateFromCurrencyLayer(JSONObject jsonParam) {

		String currencies = jsonParam.get("currencies").toString();
		Map<String, Object> resultMap = getRateFromCurrencyLayer(currencies);

		if ((boolean)resultMap.get("success")) {
			BigDecimal rate 	= new BigDecimal(resultMap.get("rate").toString().replace(",", ""));
			BigDecimal money 	= new BigDecimal(jsonParam.get("money").toString());

			resultMap.put("calculate", MONEY_FORMAT.format(money.multiply(rate)));
		}

		return resultMap;
	}
}