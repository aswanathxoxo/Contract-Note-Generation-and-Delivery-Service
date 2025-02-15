package com.gtl.model;

import lombok.Data;

@Data
public class DocumentRequestContext {

    private String dbValue;
    private String spid;
    private int messageId;
    private String documentType;
    private String populatedHtml;
    private String base64Pdf;
    private String eUser;
    private String pdfPath;
}
