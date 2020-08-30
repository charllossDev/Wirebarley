package com.ex.wirebarley.web;

import com.ex.wirebarley.api.CurrencyLayer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

/**
 * 국가 별 환율 조회 컨트롤러
 */
@RestController()
public class WirebarleyController {

	/**
	 * 환율 조회 View / 최초 한국/USD 환율 조회
	 * @return 환율 조회 View
	 */
	@GetMapping("/")
	public ModelAndView main() throws Exception {

		Map<String, Object> resultMap = CurrencyLayer.getRateFromCurrencyLayer("KRW");
		ModelAndView mv = new ModelAndView((boolean)resultMap.get("success") ? "wirebarley" : "error");
		mv.addAllObjects(resultMap);

		return mv;
	}

	/**
	 * 국가 별 환율 조회 API
	 * @param jsonObj : {currencies = 국가코드(3자리)}
	 * @return 선택 국가 / USD 환율
	 */
	@PostMapping("/api/rate")
	public ModelAndView getRateJson(@RequestBody JSONObject jsonObj) {

		return setJsonResponse(CurrencyLayer.getRateFromCurrencyLayer(jsonObj.get("currencies").toString()));
	}

	/**
	 * 국가 별 횐율 조회 및 환전 금액 계산 API
	 * @param jsonObj : {currencies = 국가코드(3자리), money = 환전 예상 금액}
	 * @return 선택 국가 / USD 환율 및 환전 금액
	 */
	@PostMapping("/api/calculate")
	public ModelAndView getCalculateJson(@RequestBody JSONObject jsonObj) {

		return setJsonResponse(CurrencyLayer.getCalculateFromCurrencyLayer(jsonObj));
	}

	/**
	 * json response ModelAndView 공통
	 * @param result : json 으로 내릴 정
	 * @return ModelAndView(jsons)
	 */
	private ModelAndView setJsonResponse(Object result) {
		MappingJackson2JsonView jsonView = new MappingJackson2JsonView();
		jsonView.setPrettyPrint(true);
		jsonView.setObjectMapper(new ObjectMapper());
		ModelAndView mv = new ModelAndView(jsonView);
		mv.addObject("result", result);
		return mv;
	}
}
