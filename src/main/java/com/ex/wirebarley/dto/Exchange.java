package com.ex.wirebarley.dto;

import lombok.*;
import org.json.simple.JSONObject;
import org.junit.runners.Parameterized;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Exchange {

    private static final SimpleDateFormat DATE_FORMAT       = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" , Locale.KOREA );
    private static final DecimalFormat MONEY_FORMAT 	    = new DecimalFormat("#,##0.00");

    @Getter
    @Setter
    @ToString
    public static class Request {
        private String currencies;
        private String money;

        public Request (String currencies) {
            this.currencies     = currencies;
        }

        public Request (String currencies, String money) {
            this.currencies     = currencies;
            this.money          = money;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Response {

        // Common
        private final boolean success;

        // Success
        private String rate;
        private String date;
        private String currencies;

        // Calculate
        private String calculate;

        // Error
        private ApiError error;      // Api Error


        public Response(ApiCurrencyLayer apiCurrencyLayer) {

            this.success = apiCurrencyLayer.success;
            if (this.success) {
                this.rate           = MONEY_FORMAT.format(apiCurrencyLayer.getQuotes().get("USD" + apiCurrencyLayer.request.getCurrencies()));
                this.date           = DATE_FORMAT.format(new Date(apiCurrencyLayer.timestamp.getTime()));
                this.currencies     = apiCurrencyLayer.request.getCurrencies() + "/USD";
            } else {
                this.error          = apiCurrencyLayer.getError();
            }
        }

        public Response(int code, String info, String exception) {
            this.success            = false;
            this.error              = new ApiError(code, info, exception);
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class ApiCurrencyLayer {

        // Common
        private boolean     success;    // Api Status
        private Request     request;    // Api Request

        // Success
        private String      terms;      // Terms
        private String      privacy;    // Privacy
        private String      source;     // Target
        private Timestamp   timestamp;  // Timestamp
        private JSONObject  quotes;     // Quotes

        // Error
        private ApiError    error;      // Api Error

        // Error Constructor
        public ApiCurrencyLayer(Request request, int code, String info, String exception) {
            this.success    = false;
            this.request    = request;
            this.error      = new ApiError(code, info, exception);
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiError {
        private int         code;       // Error code
        private String      info;       // Error Message
        private String      exception;  // Error Exception
    }

}
