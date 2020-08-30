package com.ex.wirebarley.web;

import com.ex.wirebarley.api.CurrencyLayer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

@RestController()
public class WirebarleyController {

	@GetMapping("/")
	public ModelAndView main() throws Exception {

		Map<String, Object> resultMap = CurrencyLayer.getRateFromCurrencyLayer("KRW");
		ModelAndView mv = new ModelAndView((boolean)resultMap.get("success") ? "wirebarley" : "error");
		mv.addAllObjects(resultMap);

		return mv;
	}

	@PostMapping("/api/rate")
	public ModelAndView getRateJson(@RequestBody JSONObject jsonObj) {

		return setJsonResponse(CurrencyLayer.getRateFromCurrencyLayer(jsonObj.get("currencies").toString()));
	}

	@PostMapping("/api/calculate")
	public ModelAndView getCalculateJson(@RequestBody JSONObject jsonObj) {

		return setJsonResponse(CurrencyLayer.getCalculateFromCurrencyLayer(jsonObj));
	}

	private ModelAndView setJsonResponse(Object result) {
		MappingJackson2JsonView jsonView = new MappingJackson2JsonView();
		jsonView.setPrettyPrint(true);
		jsonView.setObjectMapper(new ObjectMapper());
		ModelAndView mv = new ModelAndView(jsonView);
		mv.addObject("result", result);
		return mv;
	}
}
