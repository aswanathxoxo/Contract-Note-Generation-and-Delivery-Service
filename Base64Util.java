package com.gtl.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

@Log4j2
@Component
public class Base64Util {

    static String remarks;
    static String status = "";

    public  String convertHtmlToBase64(String htmlContent) {
        try {
            // Convert the HTML content to Base64
            return Base64.getEncoder().encodeToString(htmlContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error while converting HTML content to Base64: ", e);
            return null;
        }
    }

    public  String convertPdfToBase64(String filePath) {
        try {
            File pdfFile = new File(filePath);
            byte[] fileContent = Files.readAllBytes(pdfFile.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            log.error("Error reading PDF file for Base64 conversion: ", e);
            return null;
        }
    }

}
