package com.gtl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class MarginMailDbService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getMarginMailRecords() {
        
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("spGetMarginMailRecords");

        try {
            Map<String, Object> allResults = jdbcCall.execute(new HashMap<String, Object>());
            return allResults;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to execute stored procedure: " + e.getMessage(), e);
        }
    }
}