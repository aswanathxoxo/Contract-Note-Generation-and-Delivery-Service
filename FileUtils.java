package com.gtl.utils;

import com.gtl.config.GlobalConfigurationReader;
import com.gtl.process.ProcessRequest;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Log4j2
@Component
public class FileUtils {
    @Autowired
    private GlobalConfigurationReader globalConfigurationReader;
    @Autowired
    private Base64Util base64Util;


    public String getHtmlTemplate(String filePath) {
        try {
            log.info("Html Template Recieved from the path: " + filePath);
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read HTML template from path: " + filePath, e);
        }
    }

    public String extractStringValue(Object value) {
        if (value instanceof Utf8) {
            return value.toString();
        } else if (value instanceof Long) {
            return Long.toString((Long) value);
        } else if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }

    public ProcessRequest createRequest(GenericRecord message) {
        String content = extractStringValue(message.get("Content"));
        log.info("Content Received From message: " + content);
        String messageId = extractStringValue(message.get("MessageID"));
        String eUser = extractStringValue(message.get("Euser"));
        log.info("MessageID: {},Euser: {},", messageId, eUser);
        ProcessRequest processRequest = new ProcessRequest();
        processRequest.setData(JSONConverterUtil.jsonStringToMapConverter(content));
        processRequest.getData().put("MessageID", messageId);
        processRequest.getData().put("Euser", eUser);
        return processRequest;
    }

    public String generatePdfFromImages(Map<Integer, String> imageResponsesMap) throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date( ));
        String outputFilePath = globalConfigurationReader.getOutputFilePath() + "KRA_Combined_"+timestamp+".pdf";
        PdfWriter writer = new PdfWriter(outputFilePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        float desiredWidth = 520;
        float desiredHeight = 670;
        float marginTop = 50;
        float marginBottom = 50;
        float marginLeft = 20;
        float marginRight = 20;

        float pageWidth = PageSize.A4.getWidth() - marginLeft - marginRight;
        float pageHeight = PageSize.A4.getHeight() - marginTop - marginBottom;
        StringBuilder errorMessages = new StringBuilder();

        for (Map.Entry<Integer, String> entry : imageResponsesMap.entrySet()) {
            String base64ImageData = entry.getValue();

            JSONObject imageJson = new JSONObject(base64ImageData);
            if (imageJson.getInt("errorCode") == 0) {
                String base64Image = imageJson.getString("ImgData");
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                ImageData imageData = ImageDataFactory.create(imageBytes);
                Image image = new Image(imageData);

                // Set the image size to the desired dimensions
                image.setWidth(desiredWidth);
                image.setHeight(desiredHeight);

                // Center the image by calculating the position
                float xPos = marginLeft + (pageWidth - desiredWidth) / 2; // Center horizontally
                float yPos = marginBottom; // Align at the bottom margin

                // Set the fixed position for the image
                image.setFixedPosition(xPos, yPos);

                // Add the image to the PDF document
                document.add(image);

                // Add a page break after each image except the last one
                if (entry.getKey() != imageResponsesMap.size() - 1) {
                    document.add(new AreaBreak());
                }
            } else {
                String errorMsg = String.format("Error retrieving image for entry %d: %s",
                        entry.getKey(), imageJson.getString("errorMessage"));
                log.error(errorMsg);
                errorMessages.append(errorMsg).append("\n");
            }
        }

        // Close the document
        document.close();
        pdfDoc.close();
        log.info("PDF generated successfully at " + outputFilePath);

        if (errorMessages.length() > 0) {
            return "Errors occurred during PDF generation:\n" + errorMessages;
        }

        return base64Util.convertPdfToBase64(outputFilePath);
    }

}
