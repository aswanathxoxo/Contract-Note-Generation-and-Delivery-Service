package com.gtl.utils;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;

@Component
public class SigningUtil {

    private static final Logger logger = LoggerFactory.getLogger(SigningUtil.class);

    @Value("${token.pin}")
    private String tokenPin;

    @Value("${pkcs11.library.path}")
    private String pkcs11LibraryPath;

    @Value("${token.key.alias}")
    private String keyAlias;

    @PostConstruct
    public void init() {
        logger.info("Initializing SigningUtil with the following configuration:");
        logger.info("Token PIN: {}", tokenPin);
        logger.info("PKCS#11 Library Path: {}", pkcs11LibraryPath);
        logger.info("Key Alias: {}", keyAlias);
    }

    public void sign(String src, String destPath) {
        try {
            // Ensure the destination directory exists
            File destFile = new File(destPath);
            if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                logger.warn("Failed to create directories for destination path: {}", destPath);
            }

            // Add BouncyCastle provider
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            // Create and configure the PKCS#11 provider
            Provider pkcs11Provider = createPkcs11Provider(pkcs11LibraryPath);
            Security.addProvider(pkcs11Provider);

            // Load the KeyStore from the USB token
            KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
            keyStore.load(null, tokenPin.toCharArray());

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, tokenPin.toCharArray());
            Certificate[] certificateChain = keyStore.getCertificateChain(keyAlias);

            if (privateKey == null || certificateChain == null) {
                throw new Exception("Private key or certificate chain not found on the token.");
            }

            // Sign the PDF
            try (PdfReader reader = new PdfReader(src);
                 PdfWriter writer = new PdfWriter(destPath)) {

                PdfSigner signer = new PdfSigner(reader, writer, new StampingProperties());

                // Define the visible signature rectangle
                float width = 150; // Signature box width
                float height = 50; // Signature box height
                float x = 595 - width - 10; // Page width - box width - right margin
                float y = 10; // Bottom margin

                Rectangle signatureRectangle = new Rectangle(x, y, width, height);
                int pageNumber = 1; // Signature on the first page

                PdfSignatureAppearance appearance = signer.getSignatureAppearance();
                appearance.setReason("Signed digitally using USB token");
                appearance.setLocation("Your Location");
                appearance.setPageRect(signatureRectangle);
                appearance.setPageNumber(pageNumber);
                appearance.setCertificate(certificateChain[0]);

                signer.setFieldName("Signature1");

                IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, pkcs11Provider.getName());
                IExternalDigest digest = new BouncyCastleDigest();
                signer.signDetached(digest, signature, certificateChain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

                logger.info("PDF signed successfully and saved to: {}", destPath);
            }
        } catch (Exception e) {
            logger.error("Error signing the PDF: {}", e.getMessage(), e);
        }
    }

    private Provider createPkcs11Provider(String pkcs11LibraryPath) throws IOException {
        // Create a temporary configuration file
        File tempConfigFile = File.createTempFile("pkcs11-", ".conf");

        try (FileWriter writer = new FileWriter(tempConfigFile)) {
            writer.write(String.format("""
                    name=ePass2003
                    library=%s
                    slot=1
                    """, pkcs11LibraryPath));
        }

        tempConfigFile.deleteOnExit();

        return Security.getProvider("SunPKCS11").configure(tempConfigFile.getAbsolutePath());
    }
}
