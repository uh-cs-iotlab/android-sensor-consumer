package fi.helsinki.cs.iotlab.sensorconsumer.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mineraud on 22/12/16.
 */

public class SensorConsumerServiceParameters implements Parcelable {

    /**
     * The creator for the parcel as the parameters can be send via the intent
     */
    public static final Parcelable.Creator<SensorConsumerServiceParameters> CREATOR
            = new Parcelable.Creator<SensorConsumerServiceParameters>() {
        public SensorConsumerServiceParameters createFromParcel(Parcel in) {
            return new SensorConsumerServiceParameters(in);
        }

        public SensorConsumerServiceParameters[] newArray(int size) {
            return new SensorConsumerServiceParameters[size];
        }
    };

    private final int id;  // The id of the service
    private final long sensorRegisteringDelay;  // Delay after which we are registering the sensors
    private final long sensorCollectionDuration;  // Duration of the data collection
    private final SensorParameter[] sensorParameters;  // The parameters for each sensors
    private final boolean partialWaveLockEnabled;  // If the partial lock should be used
    private final boolean inputOutputEnabled;  // If the sensor data is stored into a file
    private final boolean heavyComputationEnabled;  // If some heavy computation should be done on the collected data

    public SensorConsumerServiceParameters(int id,
                                           long sensorRegisteringDelay,
                                           long sensorCollectionDuration,
                                           SensorParameter[] sensorParameters,
                                           boolean partialWaveLockEnabled,
                                           boolean inputOutputEnabled,
                                           boolean heavyComputationEnabled) {
        this.id = id;
        this.sensorRegisteringDelay = sensorRegisteringDelay;
        this.sensorCollectionDuration = sensorCollectionDuration;
        this.sensorParameters = sensorParameters;
        this.partialWaveLockEnabled = partialWaveLockEnabled;
        this.inputOutputEnabled = inputOutputEnabled;
        this.heavyComputationEnabled = heavyComputationEnabled;
    }

    final int getId() {
        return id;
    }

    final SensorParameter[] getSensorParameters() {
        return sensorParameters;
    }

    final boolean isPartialWaveLockEnabled() { return partialWaveLockEnabled; }

    final boolean isInputOutputEnabled() { return inputOutputEnabled; }

    final boolean isHeavyComputationEnabled() { return heavyComputationEnabled; }

    final long getSensorRegisteringDelay() { return sensorRegisteringDelay; }

    final long getSensorCollectionDuration() { return sensorCollectionDuration; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeLong(sensorRegisteringDelay);
        out.writeLong(sensorCollectionDuration);
        out.writeParcelableArray(sensorParameters, 0);
        out.writeInt(partialWaveLockEnabled ? 1 : 0);
        out.writeInt(inputOutputEnabled ? 1 : 0);
        out.writeInt(heavyComputationEnabled ? 1 : 0);
    }

    /**
     * Constructor from parcel
     * @param in the parcel
     */
    private SensorConsumerServiceParameters(Parcel in) {
        this.id = in.readInt();
        this.sensorRegisteringDelay = in.readLong();
        this.sensorCollectionDuration = in.readLong();
        this.sensorParameters = in.createTypedArray(SensorParameter.CREATOR);
        this.partialWaveLockEnabled = in.readInt() > 0;
        this.inputOutputEnabled = in.readInt() > 0;
        this.heavyComputationEnabled = in.readInt() > 0;
    }

}
