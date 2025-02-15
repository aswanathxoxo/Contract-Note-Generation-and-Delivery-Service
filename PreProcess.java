package com.gtl.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gtl.utils.MarginMailDbService;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class PreProcess {

    @Autowired
    private MarginMailDbService marginMailDbService;

    public Map<String, Map<String, Object>> createDataSet() {
        log.info("request received in createDataSet {}");

        Map<String, Object> dbResponse = marginMailDbService.getMarginMailRecords();
        log.info("db response received in createDataSet {}", dbResponse);

        return handleDbResponse(dbResponse);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> handleDbResponse(Map<String, Object> dbResponse) {
        Map<String, Map<String, Object>> clientsMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> pledgeSecuritiesMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> marginDetailsMap = new HashMap<>();
        Map<String, Map<String, Object>> finalResultMap = new HashMap<>();

        // Extracting result sets from dbResponse
        List<Map<String, Object>> resultSet1 = (List<Map<String, Object>>) dbResponse.get("#result-set-1");
        List<Map<String, Object>> resultSet2 = (List<Map<String, Object>>) dbResponse.get("#result-set-2");
        List<Map<String, Object>> resultSet3 = (List<Map<String, Object>>) dbResponse.get("#result-set-3");
        List<Map<String, Object>> resultSet4 = (List<Map<String, Object>>) dbResponse.get("#result-set-4");

        // Check for error in resultSet1
        if (!resultSet1.isEmpty()) {
            Map<String, Object> errorInfo = resultSet1.get(0);
            int errorCode = (int) errorInfo.get("errorCode");
            String errorMessage = (String) errorInfo.get("errorMessage");
            if (errorCode != 0) {
                log.error("Error in response: {} - {}", errorCode, errorMessage);
                return finalResultMap; // Return empty map on error
            }
        }

        // Populate clientsMap from resultSet2
        for (Map<String, Object> entry : resultSet2) {
            String tradeCode = (String) entry.get("trade_code");
            clientsMap.put(tradeCode, new HashMap<>(entry)); // Store details with tradeCode as key
        }

        // Populate pledgeSecuritiesMap from resultSet3
        for (Map<String, Object> entry : resultSet3) {
            String tradeCode = (String) entry.get("trade_code");
            pledgeSecuritiesMap
                .computeIfAbsent(tradeCode, k -> new ArrayList<>())
                .add(new HashMap<>(entry)); // Aggregate multiple entries
        }

        // Populate marginDetailsMap from resultSet4
        for (Map<String, Object> entry : resultSet4) {
            String tradeCode = (String) entry.get("trade_code");
            marginDetailsMap
                .computeIfAbsent(tradeCode, k -> new ArrayList<>())
                .add(new HashMap<>(entry)); // Aggregate multiple entries
        }

        // Combine all maps into finalResultMap
        for (String tradeCode : clientsMap.keySet()) {
            Map<String, Object> combinedDetails = new HashMap<>();
            combinedDetails.put("clientDetails", clientsMap.get(tradeCode));
            combinedDetails.put("pledgeSecurities", pledgeSecuritiesMap.getOrDefault(tradeCode, Collections.emptyList()));
            combinedDetails.put("marginDetails", marginDetailsMap.getOrDefault(tradeCode, Collections.emptyList()));
            finalResultMap.put(tradeCode, combinedDetails);
        }

        log.info("Final result map {}", finalResultMap);

        return finalResultMap;
    }

}