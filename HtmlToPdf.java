package com.gtl.utils;

import com.gtl.config.GlobalConfigurationReader;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.io.FileOutputStream;
import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class HtmlToPdf {

    @Autowired
    private GlobalConfigurationReader globalConfig;

    public  String writeHtmlToPDF(String html, String type, String options) {
        String outputFilePath = "";
        try {

            outputFilePath = globalConfig.getOutputFilePath() +type + ".pdf";
            log.info("outputFilePath ::::::::::::  "+globalConfig.getOutputFilePath());

            HtmlToPDFOptions pdfOptions = new HtmlToPDFOptions(options);

            Document document = new Document(pdfOptions.getPageSize());
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(outputFilePath));

            document.open();
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            worker.parseXHtml(pdfWriter, document, new StringReader(html));

            document.close();
            log.info("PDF report exported to: " + outputFilePath);
        } catch (Exception ex) {
            log.error("Error occurred while generating PDF: " + ex.getMessage(), ex);
        }
        return outputFilePath;
    }

}
