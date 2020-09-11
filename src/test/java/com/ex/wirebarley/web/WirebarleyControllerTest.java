package com.ex.wirebarley.web;

import com.ex.wirebarley.dto.Exchange;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class WirebarleyControllerTest {

	@Autowired
	private WirebarleyController wirebarleyController;

	/**
	 * 처음 화면 View Test
	 */
	@Test
	void mainTest(){

		ModelAndView mv = wirebarleyController.main();

		assertThat(mv.getViewName()).isEqualTo("wirebarley");
		assertThat(mv.getModel().get("success")).isEqualTo(true);
	}

	/**
	 * 통화 환율 조회 API 테스트
	 * * Parameter 유무 및 잘못된 Parameter 값 체크
	 * * CurrencyLayer API 작동 확인 체크
	 */
	@SuppressWarnings("unchecked")
	@Test
	void getRateJsonTest() {

		Exchange.Request request = new Exchange.Request("KRW");
		ModelAndView mv = wirebarleyController.getRateJson(request);

		System.out.println(mv.getModel().get("result"));

		Map<String, Object> result = (Map<String, Object>) mv.getModel().get("result");


/*		JSONObject jsonObject = new JSONObject();

		// KRW, JPY, PHP
		jsonObject.put("currencies", "JPY");

		// Error
		// jsonObject.put("currencies", "error");

		ModelAndView mv = wirebarleyController.getRateJson(jsonObject);
		Map<String, Object> result = (HashMap<String, Object>) mv.getModel().get("result");

		if ((boolean)result.get("success")) {
			assertThat(result.get("success")).isEqualTo(true);
			System.out.println("SUCCESS JsonValue = " + result.toString());
		} else {


			 * Error Code
			 * * 100 ~ 500 : Html Status Code
			 * * 800 : Parameter Error
			 * * 900 : Money Change Error(Too Big or Too Small)
			 * * 999 : 'CURRENCYLAYER' API Error

			System.out.println("[" + result.get("code") + "]" + result.get("msg"));
		}*/
	}

	/**
	 * 통화 환율 조회 API 테스트 및 환전 금액 계산 테스트
	 * * Parameter 유무 및 잘못된 Parameter 값 체크
	 * * CurrencyLayer API 작동 확인 체크
	 */
	@Test
	void getCalculateJsonTest() {

		// Key: currencies = [KRW, JPY, PHP]
		// Key: Money (Only Number, 1 ~ 10000)
		Exchange.Request request = new Exchange.Request("PHP", "500");

		ModelAndView mv = wirebarleyController.getCalculateJson(request);

		System.out.println(mv.getModel().get("result"));
	}
}