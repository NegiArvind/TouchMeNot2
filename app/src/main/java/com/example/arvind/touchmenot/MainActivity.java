package com.example.arvind.touchmenot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.SymbolTable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ImageView imageView;
    TextView textView;
    SensorManager sensorManager;
    Sensor proximitySensor,lightSensor,accelerometerSensor;

    int[] imagesID=new int[69];
    int currentImage=68;
    Handler handlerNear=new Handler();
    Handler handlerFar=new Handler();

    Runnable runNear=new Runnable() {
        @Override
        public void run() {
            if(currentImage>0){
                imageView.setImageResource(imagesID[--currentImage]);
                handlerNear.postDelayed(runNear,30);
            }
        }
    };

    Runnable runFar=new Runnable() {
        @Override
        public void run() {
            if(currentImage<68) {
                imageView.setImageResource(imagesID[++currentImage]);
                handlerFar.postDelayed(runFar, 30);
            }

        }
    };

    int mov_count=2;
    int mov_threshold=4;
    float alpha=0.8f;
    int shake_interval=500;
    float gravity[]=new float[3];
    int counter=0;
    long firstMoveTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView=findViewById(R.id.imageView);
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);

        List<Sensor> sensors=sensorManager.getSensorList(Sensor.TYPE_ALL);//type of sensors which we are using in our app
        for(int i=0;i<sensors.size();i++){
            String name=sensors.get(i).getName();
            String company=sensors.get(i).getVendor();
            Toast.makeText(this, name+" : "+company, Toast.LENGTH_SHORT).show();
        }

        proximitySensor=sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lightSensor =sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);

        initialise();


    }

    void initialise(){
        for (int i=1;i<=69;i++){
            //The below will get the id of all the rose present in drawable folder inside res
            int id=getResources().getIdentifier("rose"+i,"drawable",getPackageName());
            imagesID[i-1]=id;
        }
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(sensorEvent.sensor==proximitySensor){
            float value=sensorEvent.values[0];

            if(value<proximitySensor.getMaximumRange()){
                if(handlerFar!=null){
                    handlerFar.removeCallbacks(runFar);
                    Log.i("value","-----------------------------------------------"+String.valueOf(value));
                }
                handlerNear=new Handler();
                handlerNear.post(runNear);
            }else{
                if(handlerNear!=null){
                    handlerNear.removeCallbacks(runNear);
                }
                handlerFar=new Handler();
                handlerFar.post(runFar);
            }
        }
        else if(sensorEvent.sensor==accelerometerSensor){
            float x=sensorEvent.values[0];
            float y=sensorEvent.values[1];
            float z=sensorEvent.values[2];

            float max=maxAcceleration(x,y,z);
            if(max>mov_threshold){
                if(counter==0){
                    counter++;
                    firstMoveTime=System.currentTimeMillis();
                }
                else{
                    long now=System.currentTimeMillis();
                    long diff=now-firstMoveTime;
                    if(diff<shake_interval){
                        counter++;
                    }
                    else{
                        counter=0;
                        firstMoveTime=System.currentTimeMillis();
                        return;
                    }

                    if(counter>=mov_count){
                        ConstraintLayout constraintLayout=findViewById(R.id.constraintLayout);
                        Random random=new Random();
                        constraintLayout.setBackgroundColor(Color.rgb(random.nextInt(255),random.nextInt(255),
                                random.nextInt(255)));
                    }
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    float maxAcceleration(float x,float y,float z){
        gravity[0]=calGravity(x,0);
        gravity[1]=calGravity(y,1);
        gravity[2]=calGravity(z,2);

        float fx=x-gravity[0];
        float fy=y-gravity[1];
        float fz=z-gravity[2];

        float max1=Math.max(fx,fy);
        return Math.max(max1,fz);

    }

    float calGravity(float value ,int index){
        return alpha*gravity[index]+(1-alpha)*value;
    }
}
