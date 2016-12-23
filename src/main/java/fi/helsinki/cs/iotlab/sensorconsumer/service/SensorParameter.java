package fi.helsinki.cs.iotlab.sensorconsumer.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mineraud on 22/12/16.
 */

public class SensorParameter implements Parcelable {

    /**
     * The creator for the parcel as the parameters can be send via the intent
     */
    public static final Parcelable.Creator<SensorParameter> CREATOR
            = new Parcelable.Creator<SensorParameter>() {
        public SensorParameter createFromParcel(Parcel in) {
            return new SensorParameter(in);
        }

        public SensorParameter[] newArray(int size) {
            return new SensorParameter[size];
        }
    };

    private final int sensorType;
    private final int sampleFrequency;
    private final boolean isGps;

    public SensorParameter(int sensorType, int sampleFrequency) {
        this.sensorType = sensorType;
        this.sampleFrequency = sampleFrequency;
        this.isGps = false;
    }

    /**
     * Constructor to add the GPS
     * @param sampleFrequency
     */
    public SensorParameter(int sampleFrequency) {
        this.sensorType = -1;
        this.sampleFrequency = sampleFrequency;
        this.isGps = true;
    }

    public int getSensorType() {
        return sensorType;
    }

    public int getSampleFrequency() {
        return sampleFrequency;
    }

    public boolean isGps() { return isGps; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(sensorType);
        out.writeInt(sampleFrequency);
        out.writeInt(isGps ? 1 : 0);
    }

    /**
     * Constructor from parcel
     * @param in the parcel
     */
    private SensorParameter(Parcel in) {
        this.sensorType = in.readInt();
        this.sampleFrequency = in.readInt();
        this.isGps = in.readInt() > 0;
    }

}
