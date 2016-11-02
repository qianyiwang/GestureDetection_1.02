package com.example.wangqianyi.gesturedetection_102;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class WatchMainActivity extends Activity implements SensorEventListener {

    float thresh_peak = 6; //the threshold to start analysis data
    long thresh_interval = 1000; // threshold of analysis data period
    int thresh_peakNum = 2; // threshold to distinguish single and double

    ArrayList dataArr = new ArrayList();
    ArrayList dataArr_x = new ArrayList();
    ArrayList dataArray_acc_x = new ArrayList();
    ArrayList dataGry = new ArrayList();
    boolean findFirstPeak = false;
    boolean gripDect = true;
    Vibrator vibrator;
    //Sensor variable
    Sensor senAccelerometer, senGyroscope;
    SensorManager mSensorManager;
    private static final float NS2S = 1.0f / 1000000000.0f;
    float acc_x, acc_y, acc_z, gry_x, gry_y, gry_z, acc_y_lowpass;
    private float mGry; // acceleration apart from gravity
    private float mGryCurrent; // current acceleration including gravity
    private float mGryLast; // last acceleration including gravity
    private float mAcc; // acceleration apart from gravity
    private float mAccCurrent; // current acceleration including gravity
    private float mAccLast; // last acceleration including gravity

    TextToSpeech t1;

    MessageServer myMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        senAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency
        mSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gry_x = event.values[0];
            gry_y = event.values[1];
            gry_z = event.values[2];
            mGryLast = mGryCurrent;
            float omegaMagnitude = (float) Math.sqrt(gry_x * gry_x + gry_y * gry_y + gry_z * gry_z);
            mGryCurrent = omegaMagnitude;
            float delta = mGryCurrent - mGryLast;
            mGry = mGry * 0.9f + delta; // perform low-cut filter

            if(mGry>=thresh_peak) {
                if (!findFirstPeak) {
                    findFirstPeak = true;
                    setFlag();
                } else {
                    //                    dataArr.add(omegaMagnitude);
                    //                    dataArr_x.add(gry_x);
                    dataArray_acc_x.add(acc_y);
                    Log.v("hand moving acc_z", String.valueOf(acc_y_lowpass));
                    dataGry.add(mGry);
                }
            }
//            if(mGry>=thresh_peak){
//                if(!findFirstPeak){
//                    findFirstPeak = true;
//                    setFlag();
//                }
//            }
//
//            if(findFirstPeak){
//                dataArray_acc_x.add(acc_y);
//                dataGry.add(mGry);
//            }
        }

        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            acc_x = event.values[0];
            acc_y = event.values[1];
            acc_z= event.values[2];
            acc_y_lowpass = acc_y_lowpass * 0.8f + (1 - 0.8f) * event.values[1];

            mAccLast = mAccCurrent;
            mAccCurrent = (float) Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
            float delta = mAccCurrent - mAccLast;
            mAcc = mAcc * 0.9f + delta; // perform low-cut filter
            if(acc_z>5&&Math.abs(acc_x)<2&&Math.abs(acc_y)<2&&gripDect){
                gripDect = false;
//                Toast.makeText(getApplicationContext(),"GRIP",0).show();
//                t1.speak("GRIP", TextToSpeech.QUEUE_FLUSH, null);
//                vibrator.vibrate(100);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){

                @Override
                public void run() {
                    gripDect = true;
                }
            },1000);

        }
    }

    public void setFlag(){
        // Execute some code after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findFirstPeak = false;
                ArrayList peakNum_gry = new ArrayList();
                ArrayList peakNum_acc = new ArrayList();
                peakNum_gry = findPeaks(dataGry);
                peakNum_acc = findPeaks(dataArray_acc_x);
                for (int j=0;j<dataGry.size();j++){
//                    Log.v("dataGry", dataGry.get(j).toString());
                }
//                Log.v("len peakNum_gry", String.valueOf(peakNum_gry.size()));
//                Log.v("len peakNum_acc", String.valueOf(peakNum_acc.size()));

                if(!dataArray_acc_x.isEmpty()){
                    if((float)Collections.min(dataArray_acc_x)<-15) // inside
                    {
                        if(peakNum_gry.size()>=2){
                            Toast.makeText(getApplicationContext(),"DOUBLE INSIDE",0).show();
                            t1.speak("double inside", TextToSpeech.QUEUE_FLUSH, null);
                            myMessage.sendMessage("Hey, I know you twist inside twice");
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"SINGLE INSIDE",0).show();
                            t1.speak("single inside", TextToSpeech.QUEUE_FLUSH, null);
                            myMessage.sendMessage("Hey, I know you twist inside once");
                        }
                    }
                    else // outside
                    {
                        if(peakNum_gry.size()>=2){
                            Toast.makeText(getApplicationContext(),"DOUBLE OUTSIDE",0).show();
                            t1.speak("double outside", TextToSpeech.QUEUE_FLUSH, null);
                            myMessage.sendMessage("Hey, I know you twist outside twice");
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"SINGLE OUTSIDE",0).show();
                            t1.speak("single outside", TextToSpeech.QUEUE_FLUSH, null);
                            myMessage.sendMessage("Hey, I know you twist outside once");
                        }
                    }
                }

                dataArray_acc_x.clear();
                dataGry.clear();
            }
        }, thresh_interval);
    }

    public ArrayList findPeaks(ArrayList dataArr){
        ArrayList peakNum = new ArrayList();

        for (int i=1; i<dataArr.size(); i++){

            if(i<dataArr.size()-1){
                if((float)dataArr.get(i)>(float)dataArr.get(i-1)&&(float)dataArr.get(i)>(float)dataArr.get(i+1)){
                    peakNum.add((float)dataArr.get(i));
                }
            }

        }
        return peakNum;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
