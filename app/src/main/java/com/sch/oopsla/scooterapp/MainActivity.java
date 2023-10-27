package com.sch.oopsla.scooterapp;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int SHAKE_SKIP_TIME = 500;
    private static final float SHAKE_THRESHORLD_GRAVITY = 2.7f;
    int num = 0; int abs = 0; boolean flag = false;
    float[] gravity, magnetic;
    float accels[] = new float[3];
    float mags[] = new float[3];
    float[] values = new float[3];
    float azimuth, pitch, roll;
    TextView text1, text, sensing;
    Sensor Accelerometer, Magnetometer;
    SensorManager sManager;

    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    public void onSensorChanged(SensorEvent event){
        switch (event.sensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                break;
        }

        if(mags != null && accels != null){
            gravity = new float[9];
            magnetic = new float[9];
            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
            
            //보정된 방향 데이터
            float[] outGravity = new float[9];
            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X, SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, values);


            azimuth = values[2] * 180.0f / 3.14f;
            pitch = values[1] * 180.0f / 3.14f;
            roll = values[0] * 180.0f / 3.14f;
            mags = null;
            accels = null;

            if(num == 1){
                flag = true;
                text.setText("pitch= " + pitch + "\nroll= " + roll + "\n");
                abs = Math.abs((int)pitch) + Math.abs((int)roll);
                Log.e("y", ""+abs);
                num += 2;
            }
            else if (num == 0) num += 1;

            text1.setText("pitch= " + pitch + "\nroll= " + roll + "\n");

            if(flag) {
                if (Math.abs(abs - Math.abs((int) pitch) - Math.abs((int) roll)) >= 50){
                    sensing.setTextSize(20);
                    sensing.setTextColor(Color.parseColor("#FF0000"));
                    sensing.setText("비정상");
                }
                else{
                    sensing.setTextColor(Color.parseColor("#000000"));
                    sensing.setTextSize(14);
                    sensing.setText("정상");

                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        text1 = (TextView) findViewById(R.id.testView2);
        text = (TextView) findViewById(R.id.testView1);
        sensing = (TextView) findViewById(R.id.sensing);
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Accelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Magnetometer = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (Accelerometer != null){
            sManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(Magnetometer != null){
            sManager.registerListener(this, Magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        sManager.unregisterListener(this);
    }
}
