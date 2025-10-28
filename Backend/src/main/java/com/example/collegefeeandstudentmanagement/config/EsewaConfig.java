package com.example.collegefeeandstudentmanagement.config;

import org.springframework.context.annotation.Configuration;
@Configuration
public class EsewaConfig {
    public static final String MERCHANT_ID = "EPAYTEST";
    public static final String MERCHANT_SECRET = "8gBm/:&EnhH.1/q";
    public static final String PAYMENT_URL = "https://uat.esewa.com.np/epay/main";
    public static final String VERIFY_URL = "https://uat.esewa.com.np/epay/transrec";
    public static final String SUCCESS_URL = "http://localhost:8080/api/esewa/success";
    public static final String FAILURE_URL = "http://localhost:8080/api/esewa/failure";
}
