package com.ex.wirebarley.api;

import com.ex.wirebarley.dto.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public class CurrencyLayer {

	private static final String 		TARGET_URL 		= "http://apilayer.net/api/";
	private static final String 		ACCESS_KEY 		= "37482796245ba05928d801956a2a35fb";
	private static final String 		END_POINT 		= "live";

	private static final DecimalFormat MONEY_FORMAT 	= new DecimalFormat("#,##0.00");

	private static final Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);


	// Api 호출 메서드
	public static Exchange.ApiCurrencyLayer restGenericApiCall(Exchange.Request request) {

		Exchange.ApiCurrencyLayer result;

		try {
			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(5000);	//타임아웃 설정 5초
			factory.setReadTimeout(5000);		//타임아웃 설정 5초

			RestTemplate restCallApiTemplate = new RestTemplate(factory);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));    //Response Header to UTF-8

			UriComponents builder = UriComponentsBuilder.fromHttpUrl(TARGET_URL + END_POINT)
					.queryParam("access_key", ACCESS_KEY)
					.queryParam("currencies", request.getCurrencies())
					.build(false);    //자동으로 encode해주는 것을 막기 위해 false

			ResponseEntity<Exchange.ApiCurrencyLayer> responseEntity = restCallApiTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), Exchange.ApiCurrencyLayer.class);
			result = responseEntity.getBody();
			assert result != null;
			result.setRequest(request);

		} catch (HttpStatusCodeException e) {

			String exceptionMsg = "[HttpStatusCodeException]"+ e.getMessage();
			logger.error("CurrencyLayer API Error : restApiCall" + exceptionMsg);

			result = new Exchange.ApiCurrencyLayer(request, e.getRawStatusCode(), e.getStatusText(), exceptionMsg);

		} catch (Exception e) {

			String exceptionMsg = "[Exception]"+ e.getMessage();
			logger.error("CurrencyLayer API Error : restApiCall" +exceptionMsg);

			result = new Exchange.ApiCurrencyLayer(request, 999, "Exception 오류", exceptionMsg);
		}

		logger.info(result.toString());
		return result;
	}

	/**
	 * 선택한 param 국가의 USD 환율 조회
	 * @param request : 선택한 국가 코드
	 * @return api 통신 성공 : 횐율 정보 / 실패: 통신 실패 정보
	 */
	public static Exchange.Response getRateFromCurrencyLayer(Exchange.Request request) {

		Exchange.Response response;
		if (request.getCurrencies() != null) {
			Exchange.ApiCurrencyLayer apiResult = restGenericApiCall(request);
			response = new Exchange.Response(apiResult);
		} else {
			response = new Exchange.Response(800, "[ParamError]: Required param is 'currencies'", "ParamNullException" );
		}

		logger.info(response.toString());
		return response;
	}

	/**
	 * 선택한 param 국가의 USD 환율 조회 및 수취금액 계산
	 * @param request : {currencies = 국가 코드, money = 환전 금액}
	 * @return api 통신 성공 : 횐율 정보 / 실패: 통신 실패 정보
	 */
	public static Exchange.Response getCalculateFromCurrencyLayer(Exchange.Request request) {

		Exchange.Response response;
		if (request.getCurrencies() != null && request.getMoney() != null) {
			response = getRateFromCurrencyLayer(request);

			if (response.isSuccess()) {

				try {

					BigDecimal rate = new BigDecimal(response.getRate().replace(",", ""));
					BigDecimal money = new BigDecimal(request.getMoney());

					if (money.compareTo(new BigDecimal(10000)) > 0) 	throw new Exception("Money is too Big");
					else if (money.compareTo(new BigDecimal(0)) <= 0) 	throw new Exception("Money is too Small");

					response.setCalculate(MONEY_FORMAT.format(money.multiply(rate)));

				} catch (Exception e) {
					response = new Exchange.Response(900, e.getMessage(), e.toString());
				}
			}
		} else {
			response = new Exchange.Response(800, "[ParamError]: Required param is 'currencies' and 'money'", "ParamNullException");
		}

		logger.info(response.toString());
		return response;
	}
}