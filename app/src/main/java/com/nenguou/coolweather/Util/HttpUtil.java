package com.nenguou.coolweather.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by binguner on 2018/2/10.
 */

public class HttpUtil {

    public static void sendHttpRequest(final String address, final HttpCallbackListener httpCallbackListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine())!=null){
                        response.append(line);
                    }
                    if(httpCallbackListener!=null){
                        httpCallbackListener.onFinsh(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(httpCallbackListener!=null){
                        httpCallbackListener.onError(e);
                    }
                }
            }
        }).start();
    }
}
