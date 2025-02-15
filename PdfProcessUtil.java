package com.gtl.utils;

import java.util.Map;
import com.gtl.config.GlobalConfigurationReader;
import com.gtl.service.DbReqProcessor;
import lombok.extern.log4j.Log4j2;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class PdfProcessUtil {
    @Autowired
    private  GlobalConfigurationReader globalConfigurationReader;
    @Autowired
    private DbReqProcessor dbReqProcessor;
    @Autowired
    private Base64Util base64Util;

    public String mapValuesToJrxmlTemplate(Map<String, Object> pdfDocParameters, String fileName, String templateID) {
        try {
            // Load JRXML file and compile it
            String jrxmlFilePath = globalConfigurationReader.getFileUploadPath() + templateID + ".jrxml";
            log.info("Loading JRXML file from path: " + jrxmlFilePath);

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFilePath);
            String clientId = pdfDocParameters.containsKey("DpClientID") ? pdfDocParameters.get("DpClientID").toString() : "";

            // Create parameters map for the report
            Map<String, Object> parameters = new HashMap<>();

            // Add parameters only if present in the map and non-null
            addParameterIfExists(pdfDocParameters, parameters, "CustomerName");
            addParameterIfExists(pdfDocParameters, parameters, "DpClientID");
            addParameterIfExists(pdfDocParameters, parameters, "DPID");
            addParameterIfExists(pdfDocParameters, parameters, "TradeCode");
            addParameterIfExists(pdfDocParameters, parameters, "TIN");
            addParameterIfExists(pdfDocParameters, parameters, "RegisteredEmail");
            addParameterIfExists(pdfDocParameters, parameters, "BranchName");
            addParameterIfExists(pdfDocParameters, parameters, "BranchCode");
            addParameterIfExists(pdfDocParameters, parameters, "BranchPhoneNos");
            addParameterIfExists(pdfDocParameters, parameters, "BranchEmail");
            addParameterIfExists(pdfDocParameters, parameters, "BranchTimings");
            addParameterIfExists(pdfDocParameters, parameters, "Mobile");
            addParameterIfExists(pdfDocParameters, parameters, "Landline");
            addParameterIfExists(pdfDocParameters, parameters, "TollFree");
            addParameterIfExists(pdfDocParameters, parameters, "PaidLine");
            addParameterIfExists(pdfDocParameters, parameters, "CustCareEmail");
            addParameterIfExists(pdfDocParameters, parameters, "PIN");
            addParameterIfExists(pdfDocParameters, parameters, "Password");

            // Add additional report configurations (like locale, date, and number patterns)
            parameters.put(JsonQueryExecuterFactory.JSON_DATE_PATTERN, "yyyy-MM-dd");
            parameters.put(JsonQueryExecuterFactory.JSON_NUMBER_PATTERN, "#,##0.##");
            parameters.put(JsonQueryExecuterFactory.JSON_LOCALE, Locale.ENGLISH);

            // Fill the report with data
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            log.info("Report generated successfully.");

            // Export the report to PDF
            String outputFilePath = globalConfigurationReader.getOutputFilePath() + fileName + "-" + clientId + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFilePath);
            log.info("PDF report exported to: " + outputFilePath);

            return outputFilePath;
        } catch (JRException e) {
            log.error("Error generating report: ", e);
        } catch (JSONException e) {
            log.error("Error parsing JSON data: ", e);
        }
        return null;
    }

    // Helper method to safely add parameters to the map
    private void addParameterIfExists(Map<String, Object> sourceMap, Map<String, Object> targetMap, String key) {
        if (sourceMap.containsKey(key) && sourceMap.get(key) != null) {
            targetMap.put(key, sourceMap.get(key));
        }
    }

}

