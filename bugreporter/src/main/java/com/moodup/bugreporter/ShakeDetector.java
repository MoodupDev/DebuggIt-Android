package com.moodup.bugreporter;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    //region Consts

    private static final int SHAKE_THRESHOLD = 2000;

    //endregion

    //region Fields

    private static ShakeDetector instance;

    private Activity activity;
    private ShakeListener listener;

    private long lastUpdate;
    private float lastX;
    private float lastY;
    private float lastZ;

    //endregion

    //region Override Methods

    //endregion

    //region Events

    //endregion

    //region Methods


    protected static ShakeDetector getInstance() {
        if(instance == null) {
            instance = new ShakeDetector();
        }
        return instance;
    }

    protected void register(Activity activity, ShakeListener listener) {
        if(this.activity != null && this.activity != activity) {
            unregister();
        } else if(activity == null) {
            return;
        }
        SensorManager sensorManager = getSensorManager(activity);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        this.listener = listener;
        this.activity = activity;
    }

    private void unregister() {
        getSensorManager(activity).unregisterListener(this);
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

            if(speed > SHAKE_THRESHOLD) {
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

    //endregion

    interface ShakeListener {
        void shakeDetected();
    }

}
