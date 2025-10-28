package com.example.collegefeeandstudentmanagement.service;

import com.example.collegefeeandstudentmanagement.config.EsewaConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
@Service
public class EsewaPaymentService {
    private static final String ESEWA_VERIFY_URL = "https://uat.esewa.com.np/epay/transrec";
    private static final String ESEWA_MERCHANT_CODE = "EPAYTEST";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> initiatePayment(String transactionId, double amount){
        Map<String, String>params =new HashMap<>();
        params.put("amt", String.valueOf(amount));
        params.put("pdc","0");
        params.put("psc","0");
        params.put("txAmt", "0");
        params.put("tAmt",String.valueOf(amount));
        params.put("pid",transactionId);
        params.put("scd",EsewaConfig.MERCHANT_ID);
        params.put("su",EsewaConfig.SUCCESS_URL);
        params.put("fu", EsewaConfig.FAILURE_URL);
        return params;
    }
    public boolean verifyPayment(String transactionId, double amount, String refId, String productId){
        final String TEST_REF_ID = "TESTREF123";
        return TEST_REF_ID.equals(refId);
    }
}
