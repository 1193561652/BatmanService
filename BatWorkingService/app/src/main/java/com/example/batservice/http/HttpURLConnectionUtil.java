package com.example.batservice.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class HttpURLConnectionUtil {
    public static boolean get(String url, Map<String, Object> param, String[] result, String[] error) {
        StringBuilder builder = new StringBuilder();
        try {
            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                params.append(entry.getKey());
                params.append("=");
                params.append(entry.getValue().toString());
                params.append("&");
            }
            if (params.length() > 0) {
                params.deleteCharAt(params.lastIndexOf("&"));
            }
            URL restServiceURL = new URL(url + (params.length() > 0 ? "?" + params.toString() : ""));
            HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setConnectTimeout(10 * 1000);
            httpConnection.setReadTimeout(10 * 1000);
            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : " + httpConnection.getResponseCode());
            }
            InputStream inStrm = httpConnection.getInputStream();
            byte[] b = new byte[1024];
            int length = -1;
            while ((length = inStrm.read(b)) != -1) {
                builder.append(new String(b, 0, length));
            }
            result[0] = builder.toString();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            error[0] = e.getMessage();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            error[0] = e.getMessage();
            return false;
        }
    }
}
