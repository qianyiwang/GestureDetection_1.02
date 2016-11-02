package com.example.wangqianyi.gesturedetection_102;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by wangqianyi on 2016-10-03.
 */
public class RequestPlace extends AsyncTask<String, String, String> {

    String url;
    Context context;
    public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
    Intent intent = new Intent(BROADCAST_ACTION);

    RequestPlace(String msg, Context context){
        this.url = msg;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        String data = null;
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpResponse response = httpclient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                data = EntityUtils.toString(entity);
//                Log.v("Place response",data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
//        super.onPostExecute(s);
        try {
            JSONObject jsono = new JSONObject(s);
            JSONArray jarray = jsono.getJSONArray("results");
            for (int i=0; i<jarray.length();i++)
            {
                JSONObject jobj = jarray.getJSONObject(i);
                JSONObject jobj2 = jobj.getJSONObject("geometry");
                Object location = jobj2.get("location");
                intent.putExtra("location",String.valueOf(location));
                context.sendBroadcast(intent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

