package com.gtl.process;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtl.config.GlobalConfigurationReader;

import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

@Log4j2
@Service
public class Process {

    @Autowired
    private PostProcess postProcess;

    @Autowired
    private GlobalConfigurationReader globalConfigurationReader;

    public String executeProcess(Map<String, Map<String, Object>> dataSet) {
        // Step 1: Iterate over each client in the pre-processed data
        for (Map.Entry<String, Map<String, Object>> entry : dataSet.entrySet()) {
            String tradeCode = entry.getKey();
            Map<String, Object> clientData = entry.getValue();

            // Step 2: Prepare data for the Jasper report
            List<Map<String, Object>> clientDetails = (List<Map<String, Object>>) clientData.get("clientDetails");
            List<Map<String, Object>> marginDetails = prepareReportData(clientData, "marginDetails");
            List<Map<String, Object>> pledgeSecurities = prepareReportData(clientData, "pledgeSecurities");

            // Step 3: Generate the Jasper report
            try {
                // Load the Jasper report template
                JasperReport jasperReport = JasperCompileManager.compileReport( globalConfigurationReader.getOutputFilePath() + "marginreport.jrxml");

                // Prepare parameters for the report
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("clientName", clientDetails.get(0).get("clientName")); // Access first element of the list
                parameters.put("clientAddress", clientDetails.get(0).get("clientAddress")); // Access first element of the list
                parameters.put("tradeDate", clientDetails.get(0).get("process_date")); // Access first element of the list
                parameters.put("exchange", clientDetails.get(0).get("venues")); // Assuming clientData has this key
                parameters.put("REPORT_DATA", marginDetails); // Pass the report data as a parameter

                // Fill the report with data and parameters
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(marginDetails));

                // Define the output path for the PDF
                String outputPath = globalConfigurationReader.getOutputFilePath() + "report_" + tradeCode + ".pdf";

                // Step 4: Export the report to the specified path
                JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

                // Log success
                log.info("Report generated and saved to: {}", outputPath);

                // Step 5: Send the email with the generated PDF
                postProcess.sendEmailWithPdf(clientData, outputPath, tradeCode); // Use PostProcess to send the email

            } catch (JRException e) {
                log.error("Error generating report for trade code: {}", tradeCode, e);
            }
        }

        return "Reports generated and emails sent successfully.";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> prepareReportData(Map<String, Object> clientData, String detailType) {
        List<Map<String, Object>> reportData = new ArrayList<>();

        if (detailType.equalsIgnoreCase("pledgeSecurities")) {
            List<Map<String, Object>> pledgeSecurities = (List<Map<String, Object>>) clientData.get(detailType);
            return pledgeSecurities;

        } else if (detailType.equalsIgnoreCase("marginDetails")) {
            // Extract relevant data from clientData
            List<Map<String, Object>> marginDetails = (List<Map<String, Object>>) clientData.get(detailType);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            for (Map<String, Object> detail : marginDetails) {
                Map<String, Object> reportRow = new HashMap<>();

                // Populate the reportRow with the necessary fields
                reportRow.put("seg", detail.get("segment"));
                Object tradeDayObj = detail.get("trade_day");
                if (tradeDayObj instanceof java.sql.Timestamp) {
                    reportRow.put("tradeDay", dateFormat.format((java.sql.Timestamp) tradeDayObj));
                } else {
                    reportRow.put("tradeDay", tradeDayObj);
                }

                reportRow.put("funds", convertToDouble(detail.get("ma_funds")));
                reportRow.put("valueOfSecurities", convertToDouble(detail.get("ma_security_value")));
                reportRow.put("valueOfMarginPledgeSecurity", convertToDouble(detail.get("ma_security_value_mpledge")));
                reportRow.put("fdr", convertToDouble(detail.get("mo_deliverymargin")));
                reportRow.put("otherApprovedForms", convertToDouble(detail.get("mo_upfront_margin")));
                reportRow.put("totalMargins", convertToDouble(detail.get("ma_total_margin")));
                reportRow.put("totalUpfrontMargin", convertToDouble(detail.get("mo_total_requirement")));
                reportRow.put("mtm", convertToDouble(detail.get("mo_consolidated_obligation")));
                reportRow.put("deliveryMargin", convertToDouble(detail.get("mo_deliverymargin")));
                reportRow.put("totalRequirement", convertToDouble(detail.get("mo_total_requirement")));
                reportRow.put("cc", convertToDouble(detail.get("cc")));
                reportRow.put("rms", convertToDouble(detail.get("rms")));

                // Ensure marginStatus is a String
                Object marginStatusObj = detail.get("margin_status");
                reportRow.put("marginStatus", marginStatusObj != null ? String.valueOf(marginStatusObj) : "");

                // Add the populated row to the report data
                reportData.add(reportRow);
            }

            return reportData;

        }

        return reportData;

    }

    // Helper method to convert BigDecimal to Double
    private Double convertToDouble(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        return value instanceof Number ? ((Number) value).doubleValue() : null; // Handle other numeric types
    }
}