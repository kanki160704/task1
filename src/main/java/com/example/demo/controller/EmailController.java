package com.example.demo.controller;

import org.apache.http.Header;

import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


@Controller
public class EmailController {
    private static final String API_KEY = "1a6b097e121058cb09d017d04630351d-us21";
    private static final String BASE_URL = "https://us21.api.mailchimp.com/3.0/lists/";
    private static final String LIST_ID = "4f44c5a355";
    private static final String BASE_URL_SEND = "https://us21.api.mailchimp.com/3.0/";
    private static final String TEMPLATE = "10563625";

    @PostMapping("/send-email")
    public String sendEmail(
            @RequestParam(name = "recipientEmail", required = false) String email,
            @RequestParam(name = "startTime", required = false) String startTime,
            @RequestParam(name = "endTime", required = false) String endTime,
            @RequestParam(name = "subject", required = false) String subject,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "templateId", required = false) String templateId
    ) {
        try {
            addSubscriber(email);
            String campaignId = createCampaign(subject, description, startTime, endTime);
            sendCampaign(campaignId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "send-email";
    }

    @GetMapping("/send-email")
    public String init() {
        return "send-email";
    }

    public static String createCampaign(String subject, String description, String startTime, String endTime) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(BASE_URL_SEND + "campaigns");

        httpPost.addHeader("Authorization", "Basic " + API_KEY);
        httpPost.addHeader("Content-Type", "application/json");

/*        String jsonBody = "{" +
                "\"type\":\"regular\"," +
                "\"recipients\":{\"list_id\":\"" + LIST_ID + "\"}," +
                "\"settings\":{\"subject_line\":\"" + subject + "\",\"reply_to\":\"kanki2050360276@gmail.com\",\"from_name\":\"K Z\"}," +
                "\"content\":{\"plain_text\":\"" + description + "\",\"html\":\"<p>Your HTML content</p>\"}" +
                "}";*/

        String jsonBody = "{" +
                "\"type\":\"regular\"," +
                "\"recipients\":{\"list_id\":\"" + LIST_ID + "\"}," +
                "\"settings\":{\"subject_line\":\"" + subject + "\",\"reply_to\":\"kanki2050360276@gmail.com\",\"from_name\":\"K Z\"}," +
                "\"content\":{\"plain_text\":\"" + description + "\",\"html\":\"<p>Your HTML content</p>\"}";

        if (startTime.equals("") == false) {
            jsonBody += ",\"start_time\":\"" + startTime + "\"";
        }
        if (endTime.equals("") == false) {
            jsonBody += ",\"end_time\":\"" + endTime + "\"";
        }
        jsonBody += "}";
        System.out.println(jsonBody);
        try {
            httpPost.setEntity(new StringEntity(jsonBody));
            CloseableHttpResponse response = client.execute(httpPost);

            // 解析响应，获取campaign的ID
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject jsonResponse = new JSONObject(responseBody);
            String campaignId = jsonResponse.getString("id");

            //printHead(httpPost);
            return campaignId;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void sendCampaign(String campaignId) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(BASE_URL_SEND + "campaigns/" + campaignId + "/actions/send");

        httpPost.addHeader("Authorization", "Basic " + API_KEY);

        try {
            CloseableHttpResponse response = client.execute(httpPost);
            System.out.println(response.getStatusLine());
            printHead(httpPost);

            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response: " + responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printHead(HttpPost httpPost) {
        Header[] headers = httpPost.getAllHeaders();
        for (Header header : headers) {
            System.out.println(header.getName() + ": " + header.getValue());
        }
    }

    public static void addSubscriber(String email) throws JSONException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(BASE_URL + LIST_ID + "/members");

        httpPost.addHeader("Authorization", "Basic " + API_KEY);
        httpPost.addHeader("Content-Type", "application/json");

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("email_address", email);
        jsonBody.put("status", "subscribed");

        try {
            StringEntity entity = new StringEntity(jsonBody.toString());
            httpPost.setEntity(entity);

            CloseableHttpResponse response = client.execute(httpPost);

            System.out.println("Status for adding emails: " + response.getStatusLine());
            String responseBody = EntityUtils.toString(response.getEntity());
            //System.out.println("Response: " + responseBody);

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
