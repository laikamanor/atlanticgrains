package com.example.atlanticgrains;

import android.os.StrictMode;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class api_class {
    String gURL = "",
    gMethod = "",
    gBody = "",
    gIpAddress = "";
    public api_class(String url, String method, String body, String ipAddress){
        gURL = url;
        gMethod = method;
        gBody = body;
        gIpAddress = ipAddress;
    }

    public String getResponse(){
        try{
            if (android.os.Build.VERSION.SDK_INT > 9)
            {
                StrictMode.ThreadPolicy policy = new
                        StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            OkHttpClient client;
            client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, gBody);
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(gIpAddress + gURL)
                    .method(gMethod, gMethod.equals("GET") ? null : body)
                    .build();
            Response response;
            response = client.newCall(request).execute();
            return response.body().string();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return ex.toString();
        }
    }
}
