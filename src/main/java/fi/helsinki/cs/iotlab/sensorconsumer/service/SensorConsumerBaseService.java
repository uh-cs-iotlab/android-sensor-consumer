package fi.helsinki.cs.iotlab.sensorconsumer.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class SensorConsumerBaseService extends Service
        implements SensorEventListener, LocationListener {

    /**
     * Indicates how to behave if the service is killed
     * In our case we want the starting intent to be redelivered
     */
    private static final int mStartMode = START_REDELIVER_INTENT;

    /**
     * Used to get the parameters from the intent
     */
    static final String PARAMETERS = "PARAMETERS";

    /**
     * The TAG used for logging
     */
    private static final String TAG = "SensorConsumer";

    /**
     * A filter for data collection status broadcast
     */
    public static final String SENSOR_CONSUMER_SERVICE_INTENT_FILTER = "SensorConsumerService";

    /**
     * Key for the status of the service
     */
    public static final String SENSOR_CONSUMER_SERVICE_STATUS = "status";

    /**
     * Key for the stats of the service
     */
    public static final String SENSOR_CONSUMER_SERVICE_STATS = "stats";

    /**
     * Status for the broadcast used for receiver to know if the service started
     */
    public static final int SENSOR_CONSUMER_SERVICE_STARTED = 1;

    /**
     * Status for the broadcast used for receiver to know if the service stopped
     */
    public static final int SENSOR_CONSUMER_SERVICE_STOPPED = 0;

    /**
     * The sensor manager
     */
    private SensorManager mSensorManager;

    /**
     * The list of registered sensors
     */
    private List<Sensor> registeredSensors;
    private boolean isGpsRegistered;
    private boolean isPartialWaveLockEnabled;
    private boolean isInputOutputEnabled;
    private boolean isHeavyComputationEnabled;
    private SensorConsumerServiceStatistics sensorConsumerServiceStatistics;

    /**
     * The handler for the delayed operations (registering the sensors and killing the process)
     */
    private Handler handler;

    /**
     * The location manager for the location information
     */
    private LocationManager locationManager;

    /**
     * To ensure the phone keeps running
     */
    private PowerManager.WakeLock wakeLock;


    /**
     * This function is called when the Service is started via an startCommand.
     * Then the service must be killed manually via a stopService intent.
     * @param intent the start intent (will be resent if service is stopped)
     * @param flags the flags
     * @param startId the start id
     * @return the start mode for the service (resend intent)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Initialisation
        this.isGpsRegistered = false;
        this.isPartialWaveLockEnabled = false;
        this.isInputOutputEnabled = false;
        this.isHeavyComputationEnabled = false;
        if (this.registeredSensors == null) {
            this.registeredSensors = new ArrayList<>();
        }
        else {
            this.registeredSensors.clear();
        }

        final SensorConsumerServiceParameters params = intent.getParcelableExtra(PARAMETERS);
        this.isPartialWaveLockEnabled = params.isPartialWaveLockEnabled();
        this.isInputOutputEnabled = params.isInputOutputEnabled();
        this.isHeavyComputationEnabled = params.isHeavyComputationEnabled();
        this.sensorConsumerServiceStatistics = new SensorConsumerServiceStatistics(new Date(), params);

        this.handler = new Handler();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Register the sensors listed in the parameters
                registerSensors(params.getSensorParameters());
                // Check that we registered at least one sensor
                if (registeredSensors.size() == 0) {
                    Log.e(TAG, "Can't monitor an empty list of sensors");
                    notifyServiceStopped();
                }
                else {
                    // Just
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyServiceStopped();
                        }
                    }, params.getSensorCollectionDuration());
                }
            }
        }, params.getSensorRegisteringDelay());

        // Notify our listeners that the collection started
        notifyServiceStarted();

        return mStartMode;
    }

    /**
     * Notify our listeners that the service started
     */
    private void notifyServiceStarted() {
        if (isPartialWaveLockEnabled) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            wakeLock.acquire();
        }
        Intent intent = new Intent(SENSOR_CONSUMER_SERVICE_INTENT_FILTER);
        intent.putExtra(SENSOR_CONSUMER_SERVICE_STATUS, SENSOR_CONSUMER_SERVICE_STARTED);
        intent.putExtra(SENSOR_CONSUMER_SERVICE_STATS, sensorConsumerServiceStatistics);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Notify our listeners that the service stopped
     */
    private void notifyServiceStopped() {
        if (this.isPartialWaveLockEnabled) {
            wakeLock.release();
        }
        Intent intent = new Intent(SENSOR_CONSUMER_SERVICE_INTENT_FILTER);
        intent.putExtra(SENSOR_CONSUMER_SERVICE_STATUS, SENSOR_CONSUMER_SERVICE_STOPPED);
        intent.putExtra(SENSOR_CONSUMER_SERVICE_STATS, sensorConsumerServiceStatistics);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        unregisterSensors();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Register a sensor using the sensor manager (not for GPS)
     *
     * @param type           the Android type of sensor
     * @param sampleFrequency The frequency in (milliseconds)
     */
    private void registerInnerSensorListener(int type, int sampleFrequency) {
        if (type == Sensor.TYPE_GRAVITY && Build.VERSION.SDK_INT < 9) {
            Log.w(TAG, "Gravity sensor only available from API 9");
            return;
        }
        else if ((type == Sensor.TYPE_GYROSCOPE_UNCALIBRATED ||
                type == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.w(TAG, "Uncalibrated sensors only available from JELLY BEAN MR2");
            return;
        }
        // Get the default sensor for a particular type
        Sensor sensor = mSensorManager.getDefaultSensor(type);
        if (sensor != null) {
            // If the sensor is available then add it to the list of registered sensors
            registeredSensors.add(sensor);
            mSensorManager.registerListener(this, sensor, sampleFrequency * 1000); // freq is in ns
        } else {
            Log.d(TAG, "No sensor of type " + type + " available");
        }
    }

    /**
     * Register the GPS listener
     */
    private void registerGpsListener(int sampleFrequency) {
        Log.d(TAG, "Registering GPS");
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Check for permission,
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the best provider
            String locationProvider = locationManager.getBestProvider(new Criteria(), true);
            // Request location updates for 500ms and 1 meter changes
            locationManager.requestLocationUpdates(locationProvider, sampleFrequency, 1, this);
            this.isGpsRegistered = true;
        } else {
            Log.d(TAG, "Registering GPS failed");
        }
    }

    /**
     * Register a sensor based on its type
     *
     * @param sensorParameter the sensor parameter
     */
    private void registerSensorListener(SensorParameter sensorParameter) {
        // Different handling for GPS sensor
        if (sensorParameter.isGps()) {
            registerGpsListener(sensorParameter.getSampleFrequency());
        } else {
            // Initiate the sensor manager
            if (mSensorManager == null) {
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            }
            registerInnerSensorListener(sensorParameter.getSensorType(),
                    sensorParameter.getSampleFrequency());
        }
    }

    /**
     * Register the sensors listed in the parameters
     *
     * @param sensorParameters the sensor parameters
     */
    private void registerSensors(SensorParameter[] sensorParameters) {
        for (SensorParameter sensorParameter : sensorParameters) {
            registerSensorListener(sensorParameter);
        }
    }

    /**
     * Unregister all sensors
     */
    private void unregisterSensors() {
        if (isGpsRegistered && locationManager != null && !(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            locationManager.removeUpdates(this);
        }

        for(Sensor sensor : registeredSensors) {
            mSensorManager.unregisterListener(this, sensor);
        }
        registeredSensors.clear();
    }

    /**
     * Called when we have received an update from the sensor
     * @param event the received event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        long time = System.currentTimeMillis();
        sensorConsumerServiceStatistics.updateSensorUpdateCounter(time, sensor.getType());

        if (isInputOutputEnabled) {
            Log.w(TAG, "Input and Output operations on the data not yet implemented");
        }

        if (isHeavyComputationEnabled) {
            Log.w(TAG, "heavy computation operations on the data not yet implemented");
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        long time = System.currentTimeMillis();
        sensorConsumerServiceStatistics.updateSensorUpdateCounter(time, -1);
        /*float[] values = new float[6];
        values[0] = (float)location.getLatitude();
        values[1] = (float)location.getLongitude();
        values[2] = (float)location.getAltitude();
        values[3] = location.getSpeed();
        values[4] = location.getAccuracy();
        values[5] = location.getBearing();*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Nothing is done when the status of the provider change
     * @param provider the provider
     * @param status the status
     * @param extras extra bundle
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    /**
     * We do nothing when the provider is enabled
     * @param provider the provider
     */
    @Override
    public void onProviderEnabled(String provider) {}

    /**
     * We do nothing when the provider is disabled
     * @param provider the provider
     */
    @Override
    public void onProviderDisabled(String provider) {}

}
