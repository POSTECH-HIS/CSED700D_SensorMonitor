package org.hislab.sensormonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorMonitor mSensorMonitor;
    private PowerManager.WakeLock mWakeLock;

    private boolean mIsMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorMonitor = new SensorMonitor(new WeakReference<MainActivity>(this));

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Button buttonToggle = (Button)findViewById(R.id.buttonToggle);
        buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsMonitoring = !mIsMonitoring;
                if (mIsMonitoring) {
                    startSensing();
                } else {
                    stopSensing();
                }
            }
        });

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
//        mWakeLock.acquire();  // place this where you want to acquire a wakelock
//        mWakeLock.release();  // place this where you want to release a wakelock
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d(TAG(), "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mSensorManager.unregisterListener(mSensorMonitor, mAccelerometer);
        Log.d(TAG(), "onStop");
    }

    public void startSensing(){
        mSensorManager.registerListener(mSensorMonitor, mAccelerometer, 10000, mSensorMonitor.getWorkerHandler());
        Log.d(TAG(), "Starting sensor monitoring");
    }

    public void stopSensing(){
        mSensorManager.unregisterListener(mSensorMonitor, mAccelerometer);
        Log.d(TAG(), "Stopping sensor monitoring");
    }

    public void updateAccelUI(long timestamp /* millisecond */, float[] values, float freq){
        ((TextView)findViewById(R.id.textAccelX)).setText(String.format("%.3f", values[0]));
        ((TextView)findViewById(R.id.textAccelY)).setText(String.format("%.3f", values[1]));
        ((TextView)findViewById(R.id.textAccelZ)).setText(String.format("%.3f", values[2]));
        ((TextView)findViewById(R.id.textTimstampAccel)).setText(String.format("%d", timestamp));
        ((TextView)findViewById(R.id.textSampleRateAccel)).setText(String.format("%.2f", freq));
    }

    static public String TAG(){
        return MainActivity.class.getName() + " " + Thread.currentThread().getName();
    }
}