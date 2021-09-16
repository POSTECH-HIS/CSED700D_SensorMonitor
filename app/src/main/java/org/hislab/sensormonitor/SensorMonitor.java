package org.hislab.sensormonitor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;

public class SensorMonitor implements SensorEventListener {
    private int mCount = 0;
    private long mOffset = 0L;
    private float mFreq = 0.0f;
    private final HandlerThread mWorker;

    private final Handler mMainHandler, mWorkerHandler;
    private final WeakReference<MainActivity> mMainActivity;

    public SensorMonitor(WeakReference<MainActivity> activity) {
        mWorker = new HandlerThread("WorkerThread");
        mWorker.start();
        mWorkerHandler = new Handler(mWorker.getLooper());

        mMainActivity = activity;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void finalize() throws Throwable {
        mWorker.quitSafely();
        super.finalize();
    }

    public Handler getWorkerHandler(){
        return mWorkerHandler;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long ts = event.timestamp; // nanosecond
            float[] values = event.values.clone();
            if (mCount++ == 0) {
                mOffset = ts;
            } else if (mCount == 100) {
                mCount = 0;
                mFreq = (1.0f / ((ts - mOffset) / 1e9f / 100.0f));
                Log.v(MainActivity.TAG(), "Processed 100 samples"+ts);
            }

            // instead of creating a new Runnable everytime and posting it, you may do "sendMessage()" with a Message object reused from a global message pull
            // when the sending/posting target is the main thread, there is an even more convenient way: runOnUiThread(), which is also applicable here
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity activity = mMainActivity.get();
                    if (activity != null) {
                        mMainActivity.get().updateAccelUI(ts / 1000000L, values, mFreq);
                        Log.v(MainActivity.TAG(), "Sensor info has been displayed");
                    } else {
                        Log.w(MainActivity.TAG(), "WeakReference to MainActivity has been lost");
                    }
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

