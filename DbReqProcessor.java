package com.gtl.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtl.config.GlobalConfigurationReader;
import com.gtl.model.DocumentRequestContext;
import com.gtl.process.ProcessRequest;
import com.gtl.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class DbReqProcessor {
    @Autowired
    private HttpServiceRequest httpServiceRequest;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private GlobalConfigurationReader configurationReader;

    public String createRequestToSave(DocumentRequestContext context) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("dbConn", context.getDbValue());
        requestJson.addProperty("requestId", context.getSpid());
        requestJson.addProperty("batchStatus", "false");
        requestJson.addProperty("outTblCount", "0");

        JsonObject detailObject = new JsonObject();
        detailObject.addProperty("MessageID", context.getMessageId());
        detailObject.addProperty("RptType", context.getDocumentType());
        detailObject.addProperty("HTMLData", context.getPopulatedHtml());
        detailObject.addProperty("PDFData", context.getBase64Pdf());
        detailObject.addProperty("EUser", context.getEUser());

        requestJson.add("detailArray", new Gson().toJsonTree(List.of(detailObject)));
        String requestData = requestJson.toString();

        return httpServiceRequest.postData(requestData);
    }

    public String getKRADataForPDFGeneration(DocumentRequestContext context, ProcessRequest processRequest) {
        try {
            JsonArray detailArray = new JsonArray();
            JsonObject detailMap = new JsonObject();
            JsonObject jsonParams = new JsonObject();
            Map<String, Object> dataMap = processRequest.getData();
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                // Exclude "MessageID" and "Euser" from Map
                if (!"MessageID".equals(key) && !"Euser".equals(key)) {
                    jsonParams.add(key, new Gson().toJsonTree(entry.getValue()));
                }
            }
            detailMap.add("JsonParams", jsonParams);
            detailMap.addProperty("UserCode", context.getEUser());
            detailArray.add(detailMap);

            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("dbConn", context.getDbValue());
            requestJson.addProperty("requestId", context.getSpid());
            requestJson.addProperty("batchStatus", "false");
            requestJson.addProperty("outTblCount", "0");
            requestJson.add("detailArray", detailArray);

            // Call the postData method of HttpServiceRequest to send the request
            String dbResponse = httpServiceRequest.postData(requestJson.toString());
            JSONObject response = new JSONObject(dbResponse);

            LinkedHashMap<Integer, String> imageResponsesMap = new LinkedHashMap<>();
            if (response.getInt("errorCode") == 0) {
                JSONArray resultsArray = response.getJSONArray("results");
                if (resultsArray.length() > 0) {
                    JSONObject resultObject = resultsArray.getJSONObject(0);
                    JSONArray rows = resultObject.getJSONArray("rows");
                    for (int i = 0; i < rows.length(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        String docId = row.getString(1);  // DocID at index 1
                        String imgTableName = row.getString(2);  // ImageTableName at index 2

                        // Call getKRAImageDetails and Post Data to Image Service API
                        String imageResponse = getKRAImageDetails(context, docId, imgTableName);
                        log.info("imageResponse " + i + " for DocID {}: {}", docId, imageResponse);

                        imageResponsesMap.put(i, imageResponse);
                    }
                } else {
                    log.warn("No results found in the response.");
                }

                // Generate PDF with images from imageResponsesMap and capture the base64 response
                String base64pdf = fileUtils.generatePdfFromImages(imageResponsesMap);
                context.setBase64Pdf(base64pdf);
                //GTL_SPSaveDocumentData
                context.setSpid(configurationReader.getPostProcessSpId());
                context.setDbValue(configurationReader.getDefaultDbConn());
                context.setPopulatedHtml("");
                return  createRequestToSave(context);
            } else {
                log.error("Error Response Received From SP: " + response);
                return "Error Response Received From SP: " + response;
            }
        } catch (Exception e) {
            log.error("An error occurred in getKRADataForPDFGeneration: " + e.getMessage(), e);
            return "An error occurred while processing the request: " + e.getMessage();
        }

    }

    public String getKRAImageDetails( DocumentRequestContext requestContext,String docId, String imgTableName) {
        JsonObject requestJson = new JsonObject();

        requestJson.addProperty("DocID",docId);
        requestJson.addProperty("Doctype","KRA");
        requestJson.addProperty("ImgTableName",imgTableName);
        requestJson.addProperty("DocName","");
        requestJson.addProperty("ImgSize",0);
        requestJson.addProperty("ImgType","");
        requestJson.addProperty("Euser",requestContext.getEUser());
        requestJson.addProperty("Docdata","");
        requestJson.addProperty("ApplicationNo","");
        requestJson.addProperty("DocumentJson","");
        requestJson.addProperty("DBConn", configurationReader.getDefaultDbConn());

        return httpServiceRequest.postDataToImageService(requestJson.toString());
    }

}
