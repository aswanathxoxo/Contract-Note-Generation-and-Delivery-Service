package com.gtl.process;

import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.gtl.model.HtmldocParameters;
import com.gtl.utils.EmailUtil;
import com.gtl.utils.HtmlDocHandler;

import lombok.extern.log4j.Log4j2;
@Log4j2
@Service
public class PostProcess {

    @Autowired
    EmailUtil emailUtil;

    @Autowired
    HtmlDocHandler htmlDocHandler;
    

    public void sendEmailWithPdf(Map<String, Object> clientData, String pdfFilePath, String tradeCode) {
        String recipientEmail = (String) clientData.get("recipientEmail") != null ? 
                                (String) clientData.get("recipientEmail") : 
                                "varghese_paul@geojit.com"; // Set your default email address here
        String subject = "Provisional Daily Margin Statement for " + tradeCode;
        String emailContent = htmlDocHandler.processMarginEmailTemplate(tradeCode); // Prepare email content

        try {
            emailUtil.sendEmail(recipientEmail, "customerstatements@geojit.com", tradeCode, subject, emailContent, pdfFilePath);
            log.info("Email sent successfully to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send email for trade code {}: {}", tradeCode, e.getMessage());
        }
    }
}