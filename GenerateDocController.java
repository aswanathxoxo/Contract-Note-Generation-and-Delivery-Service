package com.gtl.controller;

import com.gtl.process.ProcessExecuterClient;
import com.gtl.process.ProcessInitiator;
import com.gtl.process.ProcessRequest;
import com.gtl.service.DbReqProcessor;
import com.gtl.utils.JSONConverterUtil;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import  com.gtl.model.DocumentRequestContext;

import java.util.Map;

@RestController
@RequestMapping("/service")
@Log4j2
public class GenerateDocController {

    @Autowired
    private DbReqProcessor dbReqProcessor;


    @Autowired
    private ProcessExecuterClient processExecuterClient;

    @Autowired
    private ProcessInitiator processInitiator;

    @PostMapping("/generate-pdf")
    public String generateDocument(@RequestBody Map<String, Object> request) {
        String dbResponse="";
        try {
            log.info("Received Document Generation Request: {}", request);

            Map<String, Object> docGenRequest = (Map<String, Object>) request.get("DocGenRequest");
            String eUser = request.get("Euser").toString();
            int mesageId = Integer.parseInt(request.get("MessageID").toString());

            ProcessRequest processRequest = new ProcessRequest();
            processRequest.setData(docGenRequest);

            DocumentRequestContext context = new DocumentRequestContext();
            Map<String, Object> headerDetails = (Map<String, Object>) docGenRequest.get("DocHeaderDtls");
            Map<String, Object> destinationDetails = (Map<String, Object>) docGenRequest.get("TargetDtls");
            String type = headerDetails != null ? (String) headerDetails.get("Type") : "";

            context.setDocumentType(type);
            context.setEUser(eUser);
            context.setMessageId(mesageId);
            context.setDbValue(destinationDetails != null ? (String) destinationDetails.get("DB") : null);
            context.setSpid(destinationDetails != null ? (String) destinationDetails.get("SPID") : null);

            // Check document type and call appropriate service method
            if (type.equalsIgnoreCase("KRA")) {
                dbResponse =dbReqProcessor.getKRADataForPDFGeneration(context, processRequest);
            }
        } catch (Exception e) {
            log.error("Error during document generation: ", e);
            return ("Error during document generation: "+e);
        }
        return dbResponse;
    }

    @PostMapping("/generate-report")
    public String generateReport(@RequestBody Map<String, Object> request) {
        String response = "";
        
        try {
            log.info("Received Report Generation Request: {}", request);
            ProcessRequest processRequest = new ProcessRequest();

            processRequest.setData(request);
            processRequest.getData().put("MessageID", "");
            processRequest.getData().put("Euser", "eUser");

            // Call the report generation service
            processExecuterClient.execute(processRequest);
            response = "Report generation initiated successfully.";

        } catch (Exception e) {
            log.error("Error during report generation: ", e);
            return ("Error during report generation: " + e);
        }
        return response;
    }

    @PostMapping("/initiate-process")
    public String initiateProcess(@RequestBody Map<String, Object> request) {
        String response = "";
        
        try {
            log.info("Received Process Initiation Request: {}", request);
            response = processInitiator.initiate(request);

        } catch (Exception e) {
            log.error("Error during process initiation: ", e);
            return ("Error during process initiation: " + e);
        }
        return response;
    }

    @GetMapping("/test")
    public  String test(){
        return  "Generate Document Service Running....";
    }


}
