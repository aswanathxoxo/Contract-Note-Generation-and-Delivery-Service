package com.gtl.utils;

import com.gtl.config.GlobalConfigurationReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import com.itextpdf.kernel.pdf.*;
import java.io.FileNotFoundException;

@Service
@Log4j2
public class PasswordProtectionUtil {

    @Autowired
    private GlobalConfigurationReader configurationReader;

    public String protectPdf(String password, String pdfFilePath) {
        log.info("--------------Inside protectPdf method --------------");
        File pdfFile = new File(pdfFilePath);
        if (!pdfFile.exists()) {
            log.error("PDF file not found: " + pdfFilePath);
            return null;
        }

        String fileName = pdfFile.getName();
        String protectedPdfPath = configurationReader.getOutputFilePath() + "Protected_" + fileName;

        try {
            PdfReader reader = new PdfReader(pdfFilePath);
            WriterProperties writerProperties = new WriterProperties()
                    .setStandardEncryption(
                            password.getBytes(),
                            password.getBytes(),
                            EncryptionConstants.ALLOW_PRINTING,
                            EncryptionConstants.ENCRYPTION_AES_128
                    );

            PdfWriter writer = new PdfWriter(protectedPdfPath, writerProperties);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            pdfDoc.close();

            log.info("PDF password protection applied successfully: " + protectedPdfPath);
            return protectedPdfPath;

        } catch (FileNotFoundException ex) {
            log.error("File not found: ", ex);
            return null;
        } catch (IOException ex) {
            log.error("Error in reading or writing PDF: ", ex);
            return null;
        } catch (Exception ex) {
            log.error("Unexpected error: ", ex);
            return null;
        }
    }
}
