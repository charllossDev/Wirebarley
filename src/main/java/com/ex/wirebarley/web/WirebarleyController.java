package com.ex.wirebarley.web;

import com.ex.wirebarley.api.CurrencyLayer;
import com.ex.wirebarley.dto.Exchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

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
	public ModelAndView main() {

		Exchange.Response response = CurrencyLayer.getRateFromCurrencyLayer(new Exchange.Request("KRW"));

		ModelAndView mv = new ModelAndView(response.isSuccess() ? "wirebarley" : "error");
		mv.addObject("result", response);

		return mv;
	}

	/**
	 * 국가 별 환율 조회 API
	 * @param request : {currencies = 국가코드(3자리)}
	 * @return 선택 국가 / USD 환율
	 */
	@PostMapping("/api/rate")
	public ModelAndView getRateJson(@RequestBody Exchange.Request request) {

		return setJsonResponse(CurrencyLayer.getRateFromCurrencyLayer(request));
	}

	/**
	 * 국가 별 횐율 조회 및 환전 금액 계산 API
	 * @param request : {currencies = 국가코드(3자리), money = 환전 예상 금액}
	 * @return 선택 국가 / USD 환율 및 환전 금액
	 */
	@PostMapping("/api/calculate")
	public ModelAndView getCalculateJson(@RequestBody Exchange.Request request) {

		return setJsonResponse(CurrencyLayer.getCalculateFromCurrencyLayer(request));
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
