package fi.helsinki.cs.iotlab.sensorconsumer.service;

import android.hardware.Sensor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.Date;
import java.util.List;

/**
 * Created by mineraud on 22/12/16.
 */

public class SensorConsumerServiceStatistics implements Parcelable {

    /**
     * The creator for the parcel as the parameters can be send via the intent
     */
    public static final Parcelable.Creator<SensorConsumerServiceStatistics> CREATOR
            = new Parcelable.Creator<SensorConsumerServiceStatistics>() {
        public SensorConsumerServiceStatistics createFromParcel(Parcel in) {
            return new SensorConsumerServiceStatistics(in);
        }

        public SensorConsumerServiceStatistics[] newArray(int size) {
            return new SensorConsumerServiceStatistics[size];
        }
    };

    private int id;
    private Date start;
    private Date end;
    private boolean partialWaveLockEnabled;
    private boolean inputOutputEnabled;
    private boolean heavyComputationEnabled;
    private SparseArray<SensorStatistics> sensorsStatistics;

    private static int getSelectedSampleRate(int sensorType,
                                             @NonNull SensorConsumerServiceParameters parameters) {
        for (SensorParameter parameter : parameters.getSensorParameters()) {
            if (parameter.getSensorType() == sensorType) {
                return parameter.getSampleFrequency();
            }
        }
        // Error we have not found it
        return -1;
    }

    public SensorConsumerServiceStatistics(Date start, @NonNull SensorConsumerServiceParameters parameters) {
        this.id = parameters.getId();
        this.start = start;
        this.partialWaveLockEnabled = parameters.isPartialWaveLockEnabled();
        this.inputOutputEnabled = parameters.isInputOutputEnabled();
        this.heavyComputationEnabled = parameters.isHeavyComputationEnabled();
        this.sensorsStatistics = new SparseArray<>();
    }

    public void initRegisteredSensorsStatistics(@NonNull SensorConsumerServiceParameters parameters,
                                                boolean isGpsEnabled, List<Sensor> registeredSensors) {
        if (isGpsEnabled) {
            this.sensorsStatistics.put(-1, new SensorStatistics(
                    getSelectedSampleRate(-1, parameters)));
        }
        // Do the rest of the sensors
        for (Sensor sensor : registeredSensors) {
            this.sensorsStatistics.put(sensor.getType(),
                    new SensorStatistics(getSelectedSampleRate(sensor.getType(), parameters)));
        }
    }

    public void updateSensorUpdateCounter(long time, int type) {
        SensorStatistics sensorStatistics = this.sensorsStatistics.get(type);
        if (sensorStatistics != null) {
            sensorStatistics.update(time);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeLong(start.getTime());
        out.writeLong(end.getTime());
        out.writeInt(partialWaveLockEnabled ? 1 : 0);
        out.writeInt(inputOutputEnabled ? 1 : 0);
        out.writeInt(heavyComputationEnabled ? 1 : 0);
        // Then dump the sparse array in the parcel
        out.writeInt(sensorsStatistics.size());
        for(int i = 0; i < sensorsStatistics.size(); i++) {
            int key = sensorsStatistics.keyAt(i);
            out.writeInt(key);
            out.writeParcelable(sensorsStatistics.get(key), 0);
        }
    }

    /**
     * Constructor from parcel
     * @param in the parcel
     */
    private SensorConsumerServiceStatistics(Parcel in) {
        this.id = in.readInt();
        this.start = new Date(in.readLong());
        this.end = new Date(in.readLong());
        this.partialWaveLockEnabled = in.readInt() > 0;
        this.inputOutputEnabled = in.readInt() > 0;
        this.heavyComputationEnabled = in.readInt() > 0;
        this.sensorsStatistics = new SparseArray<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            int key = in.readInt();
            SensorStatistics sensorStatistics = in.readParcelable(
                    SensorStatistics.class.getClassLoader());
            this.sensorsStatistics.put(key, sensorStatistics);
        }
    }

}
