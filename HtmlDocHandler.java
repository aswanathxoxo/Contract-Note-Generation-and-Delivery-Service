package com.gtl.utils;

import com.google.gson.Gson;
import com.gtl.config.GlobalConfigurationReader;
import com.gtl.model.HtmldocParameters;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class HtmlDocHandler {
    @Autowired
    private GlobalConfigurationReader globalConfigurationReader;
    @Autowired
    private FileUtils fileUtils;

    public HtmldocParameters convertToHtmldocParameters(Object params) {
        try {
            String json = new Gson().toJson(params);
            return new Gson().fromJson(json, HtmldocParameters.class);
        } catch (Exception e) {
            log.error("Error converting to HtmldocParameters: {}", e.getMessage());
            return null;
        }
    }

    public String processHtmlTemplate(HtmldocParameters htmldocParameters,String templateID) {
        // Retrieve the HTML template
        String htmlTemplate = fileUtils.getHtmlTemplate(globalConfigurationReader.getFileUploadPath()+templateID+".html");
        return mapValuesToHtml(htmlTemplate, htmldocParameters);
    }

    public String mapValuesToHtml(String template, HtmldocParameters htmldocParameters) {
        Map<String, String> invalidValues = new HashMap<>();

        template = replaceAndTrackInvalids(template, "<CUSTNAME>", htmldocParameters.getCustomerName(), "CustomerName", invalidValues);
        template = replaceAndTrackInvalids(template, "<TRADECODE>", htmldocParameters.getTradeCode(), "TradeCode", invalidValues);
        template = replaceAndTrackInvalids(template, "<TIN>", htmldocParameters.getTIN(), "TIN", invalidValues);
        template = replaceAndTrackInvalids(template, "<DPCLIENTID>", htmldocParameters.getDpClientID(), "DpClientID", invalidValues);
        template = replaceAndTrackInvalids(template, "<DPID>", htmldocParameters.getDPID(), "DPID", invalidValues);
        template = replaceAndTrackInvalids(template, "<EMAIL>", htmldocParameters.getRegisteredEmail(), "RegisteredEmail", invalidValues);
        template = replaceAndTrackInvalids(template, "<USERNAME>", htmldocParameters.getUsername(), "Username", invalidValues);
        template = replaceAndTrackInvalids(template, "<BRANCHNAME>", htmldocParameters.getBranchName(), "BranchName", invalidValues);
        template = replaceAndTrackInvalids(template, "<BRANCHCODE>", htmldocParameters.getBranchCode(), "BranchCode", invalidValues);
        template = replaceAndTrackInvalids(template, "<BRANCHPHONE>", htmldocParameters.getBranchPhoneNos(), "BranchPhoneNos", invalidValues);
        template = replaceAndTrackInvalids(template, "<BRANCHEMAIL>", htmldocParameters.getBranchEmail(), "BranchEmail", invalidValues);
        template = replaceAndTrackInvalids(template, "<BRANCHTIMINGS>", htmldocParameters.getBranchTimings(), "BranchTimings", invalidValues);
        template = replaceAndTrackInvalids(template, "<CLMOBILE>", htmldocParameters.getRegisteredContactNos() != null ? htmldocParameters.getRegisteredContactNos().getMobile() : null, "RegisteredContactNos (Mobile)", invalidValues);
        template = replaceAndTrackInvalids(template, "<CLPHONE>", htmldocParameters.getRegisteredContactNos() != null ? htmldocParameters.getRegisteredContactNos().getLandline() : null, "RegisteredContactNos (Landline)", invalidValues);
        template = replaceAndTrackInvalids(template, "<TOLLFREE>", htmldocParameters.getTollFree(), "TollFree", invalidValues);
        template = replaceAndTrackInvalids(template, "<PAIDLINE>", htmldocParameters.getPaidLine(), "PaidLine", invalidValues);
        template = replaceAndTrackInvalids(template, "<CUSTCAREMAILID>", htmldocParameters.getCustCareEmail1(), "CustCareEmail1", invalidValues);
        template = replaceAndTrackInvalids(template, "<CUSTCAREMAILID1>", htmldocParameters.getCustCareEmail1(), "CustCareEmail1", invalidValues);
        template = replaceAndTrackInvalids(template, "<CUSTCAREMAILID2>", htmldocParameters.getCustCareEmail2(), "CustCareEmail2", invalidValues);

        if (!invalidValues.isEmpty()) {
            String errorMessage = "The following fields had null or blank values: " + invalidValues;
            invalidValues.clear();  // Destroy the map
            throw new RuntimeException(errorMessage);
        }
        return template;
    }
    private String replaceAndTrackInvalids(String template, String placeholder, String value, String fieldName, Map<String, String> invalidValues) {
        if (value == null || value.trim().isEmpty()) {
            invalidValues.put(fieldName, " null or blank");
        }
        return template.replace(placeholder, value != null ? value : "");
    }

    public void writeHtmlToFile(String htmlContent, String dpClientId,String fileName) {
        String outputDirPath = globalConfigurationReader.getOutputFilePath();
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (created) {
                log.info("Output directory created: {}", outputDirPath);
            } else {
                log.error("Failed to create output directory: {}", outputDirPath);
                return;
            }
        }
        // Append the client ID to the file name
        String finalFileName = fileName+"-" + dpClientId + ".html";
        File outputFile = new File(outputDir, finalFileName);
        if (outputFile.exists()) {
            boolean deleted = outputFile.delete();
            if (deleted) {
                log.info("Existing file deleted: {}", outputFile.getAbsolutePath());
            } else {
                log.error("Failed to delete existing file: {}", outputFile.getAbsolutePath());
                return;
            }
        }

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(htmlContent);
            log.info("HTML content written to file: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error writing HTML content to file: {}", outputFile.getAbsolutePath(), e);
        }
    }

    public String processMarginEmailTemplate(String tradeCode) {
        String emailTemplate = fileUtils.getHtmlTemplate(globalConfigurationReader.getFileUploadPath()+"margin_email_template"+".html");
        return mapValuesToMarginEmail(emailTemplate, tradeCode);
    }

    private String mapValuesToMarginEmail(String template, String tradeCode) {
        // Replace placeholders with actual values
        template = template.replace("<CLIENT_NAME>", tradeCode);
        return template;
    }


}
