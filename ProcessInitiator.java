package com.gtl.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProcessInitiator {

    @Autowired
    private PreProcess preProcess;

    @Autowired
    private Process process;

    public String initiate(Map<String, Object> request) {
        // Step 1: Pre-process the request
        ProcessRequest processRequest = new ProcessRequest();
        processRequest.setData(request);
        
        Map<String, Map<String, Object>> preProcessedData = preProcess.createDataSet();

        // Step 2: Process the data
        String processResult = process.executeProcess(preProcessedData);

        return "Process completed successfully.";
    }

}