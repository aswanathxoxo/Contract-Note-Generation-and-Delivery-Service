package com.gtl.model;

import lombok.Data;

@Data
public class HtmldocParameters {
    private  String TemplateID;
    private String CustomerName;
    private String TradeCode;
    private String TIN;
    private String ClientID;
    private String DpClientID;
    private  String Username;
    private String DPID;
    private String RegisteredEmail;
    private String BranchName;
    private String BranchCode;
    private String BranchPhoneNos;
    private String BranchEmail;
    private String BranchTimings;
    private String TollFree;
    private String PaidLine;
    private String CustCareEmail1;
    private String CustCareEmail2;
    private ContactNumbers RegisteredContactNos;
    @Data
    public static class ContactNumbers {
        private String Mobile;
        private String Landline;
    }
}
