package com.gtl.service;

import com.gtl.config.GlobalConfigurationReader;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.time.Duration;

@Log4j2
@Service
public class HttpServiceRequest {

    private final GlobalConfigurationReader globalConfigurationReader;
    @Autowired
    public HttpServiceRequest(GlobalConfigurationReader globalConfigurationReader) {
        this.globalConfigurationReader = globalConfigurationReader;
    }
    @Autowired
    private RestTemplateBuilder builder;

    public String postData(String requestData) {
        String responseData = null;
        try {
            URI targetUrl = new URIBuilder(globalConfigurationReader.getDbServiceUrl()).build();
            log.info("Db Service Url : "+globalConfigurationReader.getDbServiceUrl());
            log.info("RequestData :: "+requestData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = builder
                    .setConnectTimeout(Duration.ofMillis(300000))
                    .setReadTimeout(Duration.ofMillis(300000))
                    .build();

            HttpEntity<String> entity = new HttpEntity<>(requestData, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, entity, String.class);

            responseData = responseEntity.getBody();
            HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode();

            log.info("DbService Url : postData : responseCode ====================={} ",statusCode+"=====================");
            log.info("DbService Url : postData : responseData:: {}", responseData);

        } catch (HttpClientErrorException e) {
            log.error("DbService Url : postData : HttpClientErrorException while calling rest client: "
                    + e.getResponseBodyAsString());
        } catch (HttpStatusCodeException e) {
            log.error("DbService Url : postData : HttpStatusCodeException while calling rest client: "
                    + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("DbService Url : postData : Exception while calling rest client ", e);
        }
        return responseData;
    }

    public String postDataToImageService(String requestData) {
        String responseData = null;
        try {
            URI targetUrl = new URIBuilder(globalConfigurationReader.getImageServiceUrl()).build();
            log.info("Image Service Url : "+globalConfigurationReader.getImageServiceUrl());
            log.info("ImageService RequestData :: "+requestData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = builder
                    .setConnectTimeout(Duration.ofMillis(300000))
                    .setReadTimeout(Duration.ofMillis(300000))
                    .build();

            HttpEntity<String> entity = new HttpEntity<>(requestData, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, entity, String.class);

            responseData = responseEntity.getBody();
            HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode();

            log.info("Image Service Url : postData : responseCode ====================={} ",statusCode+"=====================");
            log.info("Image Service Url : postData : responseData:: {}", responseData);

        } catch (HttpClientErrorException e) {
            log.error("Image Service Url : postData : HttpClientErrorException while calling rest client: "
                    + e.getResponseBodyAsString());
        } catch (HttpStatusCodeException e) {
            log.error("Image Service Url : postData : HttpStatusCodeException while calling rest client: "
                    + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Image Service Url : postData : Exception while calling rest client ", e);
        }
        return responseData;
    }


}
