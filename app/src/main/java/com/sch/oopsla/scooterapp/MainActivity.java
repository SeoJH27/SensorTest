package com.sch.oopsla.scooterapp;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //view
    TextView text, sensing, list, time;
    Button button, CKbutton, REbutton;
    boolean buttonF = false;
    int i = 1;

    // timer
    private Timer timer;
    TimerTask timerTask;
    boolean timeF = false;
    TimerClass tc;

    //senser
    private SensorManager mSensorManager = null;
    private Sensor mGyroscopeSensor = null;
    private Sensor mAccelerometer = null;
    UserSensorListner userSensorListner;

    // Sensor variables
    private float[] mGyroValues = new float[3];
    private float[] mAccValues = new float[3];
    private double mAccPitch, mAccRoll;
    private final float a = 0.2f;
    private static final float NS2S = 1.0f/1000000000.0f;
    private double pitch = 0, roll = 0;
    private double timestamp;
    private double dt;
    private double temp;

    private boolean gyroRunning;
    private boolean accRunning;


    public void onAccuracyChanged(Sensor sensor, int accuracy){}
    public void onSensorChanged(SensorEvent event){}

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        text = (TextView) findViewById(R.id.testView2);
        time = (TextView) findViewById(R.id.time);
        sensing = (TextView) findViewById(R.id.sensing);
        button = (Button) findViewById(R.id.button);
        list = (TextView) findViewById(R.id.list);
        CKbutton = (Button) findViewById(R.id.CKbutton);
        REbutton = (Button) findViewById(R.id.REbutton);
        userSensorListner = new UserSensorListner();

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer= mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonF == false){
                    button.setBackgroundColor(Color.parseColor("#FF0000"));
                    button.setText("종료");
                    button.setTextColor(getResources().getColor(R.color.black));
                        if (mAccelerometer != null){
                            mSensorManager.registerListener(userSensorListner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        }
                        if(mGyroscopeSensor != null){
                            mSensorManager.registerListener(userSensorListner, mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    buttonF = true;
                }
                else{
                    mSensorManager.unregisterListener(userSensorListner);
                    button.setBackgroundColor(Color.parseColor("#000000"));
                    button.setText("보정");
                    button.setTextColor(getResources().getColor(R.color.white));
                    buttonF = false;
                    time.setText("time");
                    time.setTextSize(14);
                    time.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });

        CKbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.append(i + " " + "roll : "+String.format("%.2f", roll)+"\t\t\tpitch : "+String.format("%.2f", pitch) + "\n");
                i+=1;
            }
        });

        REbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.setText("");
                i = 1;
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onStop(){
        super.onStop();
    }


    private void complementaty(double new_ts){

        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI; // Y 축 기준
        mAccRoll= Math.atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI; // X 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);

        temp = (1/a) * (mAccRoll - roll) + mGyroValues[0];
        roll = roll + (temp*dt);

        text.setText("roll : "+String.format("%.2f", roll)+"\t\t\tpitch : "+String.format("%.2f", pitch));

        if( ( 70 < roll && roll < 180 && -180 < pitch && pitch < -80) ||
                ( -20 < roll && roll < 0 && 60 < pitch && pitch < 90) ||
                ( -180 < roll && roll < -70 && 70 < pitch && pitch < 180) ||
                ( 140 < Math.abs(roll) && 140 < Math.abs(pitch)         ) ||
                (-90 < roll && roll < -50 && 0 < pitch && pitch < -20) ||
                (-20 < roll && roll < 10 && -90 < pitch && pitch < -50)){
            sensing.setTextSize(20);
            sensing.setTextColor(Color.parseColor("#FF0000"));
            sensing.setText("비정상");
            timeF = true;

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            time.setText("신고하세요!");
                            time.setTextColor(Color.parseColor("#FF0000"));
                            time.setTextSize(30);
                            timer.cancel();
                            timeF = false;
                        }
                    });
                }
            };

            tc = new TimerClass();
            timer = new Timer();
            timer.schedule(timerTask, 5000, 1000);
            tc.run();
            Log.v("DDDDDD", timeF+"");
        }
        else{
            sensing.setTextColor(Color.parseColor("#000000"));
            sensing.setTextSize(14);
            sensing.setText("정상");
            if(timeF){
                timeF = false;
                timer.cancel();
            }
        }
    }

    public class UserSensorListner implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()){

                /** GYROSCOPE */
                case Sensor.TYPE_GYROSCOPE:

                    /*센서 값을 mGyroValues에 저장*/
                    mGyroValues = event.values;

                    if(!gyroRunning)
                        gyroRunning = true;

                    break;

                /** ACCELEROMETER */
                case Sensor.TYPE_ACCELEROMETER:

                    /*센서 값을 mAccValues에 저장*/
                    mAccValues = event.values;

                    if(!accRunning)
                        accRunning = true;

                    break;

            }

            /**두 센서 새로운 값을 받으면 상보필터 적용*/
            if(gyroRunning && accRunning){
                complementaty(event.timestamp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }

    public class TimerClass {
        public void run(){

        }
    }
}
