package com.gtl.utils;

import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Service


public class SoftPdfSignUtil {
    // Inject values from application.properties
    @Value("${pdf.sign.keystorePath}")
    private String keystorePath;

    @Value("${pdf.sign.keystorePassword}")
    private String keystorePassword;
    @Value("${pdf.sign.alias}")
    private String alias;
    @Value("${pdf.sign.certPassword}")
    private String certPassword;


    public String sign(String pdfPath, String outputPath) {
        try {
            // Load the keystore
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            FileInputStream keystoreFile = new FileInputStream(keystorePath);
            keystore.load(keystoreFile, keystorePassword.toCharArray());

            // Retrieve the private key and certificate
            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, certPassword.toCharArray());
            X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);

            // Initialize PdfReader and OutputStream for signing
            PdfReader reader = new PdfReader(pdfPath);
            FileOutputStream outputStream = new FileOutputStream(outputPath); // OutputStream for writing the signed PDF

            // Initialize StampingProperties
            StampingProperties stampingProperties = new StampingProperties();

            // Initialize PdfSigner
            PdfSigner signer = new PdfSigner(reader, outputStream, stampingProperties);

            // Get the page size
            PdfPage firstPage = signer.getDocument().getFirstPage();
            com.itextpdf.kernel.geom.Rectangle pageSize = firstPage.getPageSize();

            // Calculate the bottom-right corner for the signature
            float signatureWidth = 200; // Width of the signature rectangle
            float signatureHeight = 50; // Height of the signature rectangle
            float x = pageSize.getRight() - signatureWidth - 10; // 10 units from the right edge
            float y = pageSize.getBottom() + 10; // 10 units from the bottom edge

            // Set up the signature appearance
            PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                    .setReason("Document Signing")
                    .setLocation("Kochi")
                    .setSignatureCreator("Geojith")
                    .setPageRect(new com.itextpdf.kernel.geom.Rectangle(x, y, signatureWidth, signatureHeight)) // Signature position
                    .setPageNumber(1); // Signature on page

            // Create the signature container (using a PKCS7 signature)
            IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, null);
            IExternalDigest digest = new BouncyCastleDigest();

            // Sign the document
            signer.signDetached(digest, pks, new X509Certificate[]{certificate}, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            return "PDF signed successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error signing PDF: " + e.getMessage();
        }
    }
}