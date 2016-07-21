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

    private ShakeDetector instance;

    private Activity activity;
    private SensorListener listener;

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

    protected void register(Activity activity, SensorListener listener) {
        if(activity != null && this.activity != activity) {
            unregister();
        }
        SensorManager sensorManager = getSensorManager(activity);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
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

    interface SensorListener {
        void shakeDetected();
    }

}
