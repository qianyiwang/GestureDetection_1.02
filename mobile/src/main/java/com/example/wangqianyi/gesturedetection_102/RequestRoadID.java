package com.example.wangqianyi.gesturedetection_102;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wangqianyi on 2016-09-30.
 */
public class RequestRoadID extends AsyncTask<String, String, String> {

    String url;
    String roadID;
    final String severUrl = "https://roads.googleapis.com/v1/speedLimits?placeId=";
    final String YOUR_API_KEY = "AIzaSyBfBGb9MsqxV8P4Sui0LDxKPDyB9IUj7V4";

    RequestRoadID(String msg){
        this.url = msg;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpResponse response = httpclient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);


                try {
                    JSONObject jsono = new JSONObject(data);
                    JSONArray jarray = jsono.getJSONArray("snappedPoints");
                    JSONObject jobj = jarray.getJSONObject(0);
                    roadID = jobj.getString("placeId");
                    Log.v("response", roadID);
//                    String newUrl = severUrl+roadID+"&key="+YOUR_API_KEY;
//                    Log.v("newUrl",newUrl);
//
//                    HttpGet httpGet2 = new HttpGet(newUrl);
//                    HttpClient httpclient2 = new DefaultHttpClient();
//                    HttpResponse response2 = httpclient2.execute(httpGet2);
//                    int status2 = response2.getStatusLine().getStatusCode();
//                    if (status2 == 200){
//                        HttpEntity entity2 = response2.getEntity();
//                        String data2 = EntityUtils.toString(entity2);
//                        JSONObject json2 = new JSONObject(data2);
//                        Log.v("speedLimit", String.valueOf(json2));
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
