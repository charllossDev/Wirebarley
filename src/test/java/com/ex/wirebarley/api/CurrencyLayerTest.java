package com.ex.wirebarley.api;

import com.ex.wirebarley.dto.Exchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CurrencyLayerTest {

    public CurrencyLayer currencyLayer;

    @Test
    void mainTest()  {

        Exchange.Request request = new Exchange.Request("KRW");
        CurrencyLayer.restGenericApiCall(request);


        //assertThat(mv.getViewName()).isEqualTo("wirebarley");
        //assertThat(mv.getModel().get("success")).isEqualTo(true);
    }

}
