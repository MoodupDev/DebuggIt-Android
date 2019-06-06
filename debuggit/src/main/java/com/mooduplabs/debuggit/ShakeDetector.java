package com.mooduplabs.debuggit;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.lang.ref.WeakReference;

public class ShakeDetector implements SensorEventListener {
    private static final int SHAKE_THRESHOLD = 3000;

    private static ShakeDetector instance;

    private WeakReference<Activity> activity;
    private ShakeListener listener;

    private long lastUpdate;
    private float lastX;
    private float lastY;
    private float lastZ;

    protected static ShakeDetector getInstance() {
        if (instance == null) {
            instance = new ShakeDetector();
        }
        return instance;
    }

    protected void register(Activity activity, ShakeListener listener) {
        if (this.activity != null && getActivity() != activity) {
            unregister();
        } else if (activity == null) {
            return;
        }
        SensorManager sensorManager = getSensorManager(activity);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        this.listener = listener;
        this.activity = new WeakReference<>(activity);
    }

    protected void unregister() {
        getSensorManager(getActivity()).unregisterListener(this);
        listener = null;
    }

    private Activity getActivity() {
        return activity.get();
    }

    private SensorManager getSensorManager(Activity activity) {
        return (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        long currentTime = System.currentTimeMillis();
        // only allow one update every 100ms.
        if ((currentTime - lastUpdate) > 100) {
            long diffTime = (currentTime - lastUpdate);
            lastUpdate = currentTime;

            float x = values[0];
            float y = values[1];
            float z = values[2];

            float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                listener.shakeDetected();
            }
            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    interface ShakeListener {
        void shakeDetected();
    }
}
