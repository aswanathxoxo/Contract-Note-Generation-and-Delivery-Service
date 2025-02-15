package com.gtl.config;

import com.gtl.constants.ConfigConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfigurationReader {

    @Value(ConfigConstants.TOPIC_REGEX)
    private  String topicRegex;

    @Value(ConfigConstants.FILE_UPLOAD_PATH)
    private  String fileUploadPath;

    @Value(ConfigConstants.OUTPUT_FILE_PATH)
    private  String outputFilePath;

    @Value((ConfigConstants.DB_SERVICE_URL))
    private String dbServiceUrl;

    @Value((ConfigConstants.IMG_SERVICE_URL))
    private String imageServiceUrl;

    @Value((ConfigConstants.POSTPROCESS_SP_ID))
    private String postProcessSpId;

    @Value((ConfigConstants.DEFAULT_DB_CONN))
    private String defaultDbConn;

    @Value(ConfigConstants.SMTP_AUTH)
    private String smtpAuth;

    @Value(ConfigConstants.SMTP_HOST)
    private String smtpHost;

    @Value(ConfigConstants.SMTP_PASSWORD)
    private String smtpPassword;

    @Value(ConfigConstants.SMTP_PORT)
    private String smtpPort;

    @Value(ConfigConstants.SMTP_STARTTLS)
    private String smtpStarttls;

    @Value(ConfigConstants.SMTP_USERNAME)
    private String smtpUsername;

    public String getDbServiceUrl() {
        return dbServiceUrl;
    }

    public String getTopicRegex() {
        return topicRegex;
    }

    public String getFileUploadPath() {
        return fileUploadPath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public String getImageServiceUrl() {
        return imageServiceUrl;
    }

    public String getPostProcessSpId() {
        return postProcessSpId;
    }

    public String getDefaultDbConn() {
        return defaultDbConn;
    }

    public String getSmtpAuth() {
        return smtpAuth;
    }

    public String getSmtpHost() {
        return smtpHost;
    }
    
    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpStarttls() {
        return smtpStarttls;
    }

    public String getSmtpUserName() {
        return smtpUsername;
    }
    
}
