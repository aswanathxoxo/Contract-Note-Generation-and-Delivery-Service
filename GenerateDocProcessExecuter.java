package com.gtl.service;

import com.gtl.model.DocumentRequestContext;
import com.gtl.model.HtmldocParameters;
import com.gtl.process.ProcessExecuterClient;
import com.gtl.process.ProcessRequest;
import com.gtl.utils.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.apache.kafka.streams.kstream.EmitStrategy.log;

@Log4j2
@Component
public class GenerateDocProcessExecuter extends ProcessExecuterClient {

    @Autowired
    private HtmlDocHandler htmlDocHandler;
    @Autowired
    private DbReqProcessor dbReqProcessor;
    @Autowired
    private PdfProcessUtil pdfProcessUtil;
    @Autowired
    private PasswordProtectionUtil passwordProtectionUtil;
    @Autowired
    private Base64Util base64Util;
    @Autowired
    private HtmlToPdf htmlToPdf;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private SigningUtil signingService;  // Inject the SigningService
    @Autowired
    private SoftPdfSignUtil softSignUtil;
    @Value("${sign.type}")
    private String typeof;

    @Override
    public void execute(ProcessRequest request) throws Exception {

        Map<String, Object> headerDetails = (Map<String, Object>) request.getData().get("DocHeaderDtls");
        String type = headerDetails != null ? (String) headerDetails.get("Type") : "Document";
        Map<String, Object> destinationDetails = (Map<String, Object>) request.getData().get("TargetDtls");

        DocumentRequestContext saveRequestContext = new DocumentRequestContext();
        saveRequestContext.setDbValue(destinationDetails != null ? (String) destinationDetails.get("DB") : null);
        saveRequestContext.setSpid(destinationDetails != null ? (String) destinationDetails.get("SPID") : null);

        Object messageIdObj = request.getData().get("MessageID");
        if (messageIdObj != null) {
            try {
                saveRequestContext.setMessageId(Integer.parseInt(messageIdObj.toString()));
            } catch (NumberFormatException e) {
                log.error("Invalid MessageID format: {}", messageIdObj);
                saveRequestContext.setMessageId(0);
            }
        } else {
            log.warn("MessageID not found in the request data. Setting MessageID to 0.");
            saveRequestContext.setMessageId(0);
        }

        // Proceed to set the EUser
        saveRequestContext.setEUser(
                request.getData().get("Euser") != null ? request.getData().get("Euser").toString() : "defaultUser");
        saveRequestContext.setDocumentType(type);
        String clientId = "";

        // Process HTML document if htmldoc_parameters exist
        if (request.getData().containsKey("HTMLDocDtls")) {
            log.info(" 'HTMLDocDtls' found in the request. Proceeding to Generate Html Document... ");
            HtmldocParameters htmldocParameters = htmlDocHandler.convertToHtmldocParameters(request.getData().get("HTMLDocDtls"));
            if (htmldocParameters != null) {
                String templateId = htmldocParameters.getTemplateID();
                String dpClientId = htmldocParameters.getDpClientID();
                String populatedHtml = htmlDocHandler.processHtmlTemplate(htmldocParameters, templateId);
                saveRequestContext.setPopulatedHtml(populatedHtml);
                htmlDocHandler.writeHtmlToFile(populatedHtml, dpClientId, type);
            } else {
                log.error("Failed to convert htmldoc_parameters from request to HtmldocParameters.");
            }
        }

        // Process PDF document if pdfdoc_parameters exist
        if (request.getData().containsKey("PdfDocDtls")) {
            log.info(" 'PdfDocDtls' found in the request.  Proceeding to Generate PDF Document...");
            Map<String, Object> pdfdocParameters = (Map<String, Object>) request.getData().get("PdfDocDtls");
            String passwordProtected = headerDetails != null ? (String) headerDetails.get("PasswordProtected") : null;
            String password = headerDetails != null ? (String) headerDetails.get("Password") : null;
            String templateId = pdfdocParameters.containsKey("TemplateID") ? pdfdocParameters.get("TemplateID").toString() : null;
            String pdfSign = headerDetails != null ? (String) headerDetails.get("Pdfsign") : "N";  // Get Pdfsign value

            if (type.equalsIgnoreCase("KRA")) {
                dbReqProcessor.getKRADataForPDFGeneration(saveRequestContext, request);
                return;
            }

            String outputPdfFile = pdfProcessUtil.mapValuesToJrxmlTemplate(pdfdocParameters, saveRequestContext.getDocumentType(), templateId);

            // If password protection is enabled, protect the PDF
            if (passwordProtected != null && passwordProtected.equalsIgnoreCase("Y")) {
                outputPdfFile = passwordProtectionUtil.protectPdf(password, outputPdfFile);
            }


            if (pdfSign.equalsIgnoreCase("Y")) {

                String signedPdfPath = outputPdfFile.replace(".pdf", "_signed.pdf");
                if (typeof.equals("hard")) {
                    signingService.sign(outputPdfFile, signedPdfPath);
                } else if (typeof.equals("soft")) {
                    softSignUtil.sign(outputPdfFile, signedPdfPath);
                }
                saveRequestContext.setPdfPath(signedPdfPath);
                saveRequestContext.setBase64Pdf(base64Util.convertPdfToBase64(signedPdfPath));
            } else {
                log.info("Pdfsign is 'N', skipping signing process.");
            }
        }

        // Process HTML and PDF conversion if both html_data and pdf_options exist
        if (request.getData().containsKey("html_data") && request.getData().containsKey("pdf_options")) {
            log.info("html_data and pdf_options found in the request.");
            String html = (String) request.getData().get("html_data");
            String options = (String) request.getData().get("pdf_options");
            String outputFile = htmlToPdf.writeHtmlToPDF(html, type, options);

            String passwordProtected = headerDetails != null ? (String) headerDetails.get("PasswordProtected") : null;
            String password = headerDetails != null ? (String) headerDetails.get("Password") : null;
            if (passwordProtected != null && passwordProtected.equalsIgnoreCase("Y")) {
                outputFile = passwordProtectionUtil.protectPdf(password, outputFile);
            }

            // Sign the PDF
            String signedOutputFile = outputFile.replace(".pdf", "_signed.pdf");
            signingService.sign(outputFile, signedOutputFile);  // Sign the generated PDF

            saveRequestContext.setBase64Pdf(base64Util.convertPdfToBase64(signedOutputFile));  // Convert signed PDF to Base64
            saveRequestContext.setPopulatedHtml(html);
        }

        // Handle email sending if mail details are provided
        if (request.getData().containsKey("MailDtls")) {
            log.info("MailDtls found in the request");
            Map<String, Object> mailDetailsMap = (Map<String, Object>) request.getData().get("MailDtls");

            String mailNeeded = mailDetailsMap != null ? (String) mailDetailsMap.get("MailNeeded") : "N";
            if (mailNeeded.equalsIgnoreCase("Y")) {
                String recipientEmail = mailDetailsMap != null ? (String) mailDetailsMap.get("Recipient") : null;
                String subject = mailDetailsMap != null ? (String) mailDetailsMap.get("Subject") : "Your Document";
                String fileName = type;
                String from = mailDetailsMap != null ? (String) mailDetailsMap.get("From") : "aswanathbackup@gmail.com";

                String htmlContent = saveRequestContext.getPopulatedHtml();
                String pdfFilePath = saveRequestContext.getPdfPath();

                try {
                    emailUtil.sendEmail(recipientEmail, from, fileName, subject, htmlContent, pdfFilePath);
                    log.info("Email sent successfully to {}", recipientEmail);
                } catch (Exception e) {
                    log.error("Failed to send email: {}", e.getMessage());
                }
            }
        }

        // Post-process SP calling
        dbReqProcessor.createRequestToSave(saveRequestContext);
    }
}
