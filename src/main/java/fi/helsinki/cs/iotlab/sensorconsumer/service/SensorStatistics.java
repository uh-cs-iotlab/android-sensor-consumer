package fi.helsinki.cs.iotlab.sensorconsumer.service;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by mineraud on 22/12/16.
 */

public class SensorStatistics implements Parcelable {

    /**
     * The creator for the parcel as the parameters can be send via the intent
     */
    public static final Parcelable.Creator<SensorStatistics> CREATOR
            = new Parcelable.Creator<SensorStatistics>() {
        public SensorStatistics createFromParcel(Parcel in) {
            return new SensorStatistics(in);
        }

        public SensorStatistics[] newArray(int size) {
            return new SensorStatistics[size];
        }
    };

    private long start;  // Current time ms
    private long end;
    private int selectedSampleRate;
    private double realSampleRate;
    private int counter;

    SensorStatistics(int selectedSampleRate) {
        this.start = -1L;
        this.end = -1L;
        this.selectedSampleRate = selectedSampleRate;
        this.realSampleRate = -1;
        this.counter = 0;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getSelectedSampleRate() {
        return selectedSampleRate;
    }

    public double getRealSampleRate() {
        return realSampleRate;
    }

    public int getCounter() {
        return counter;
    }

    public double getDuration() {
        return (this.end - this.start) / 1000.;
    }

    public void update(long time) {
        this.counter++;
        if (this.start < 0) {
            this.start = time;
        }
        this.end = time;
        if (getDuration() > 0) {
            this.realSampleRate = this.counter / getDuration();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(start);
        out.writeLong(end);
        out.writeInt(selectedSampleRate);
        out.writeDouble(realSampleRate);
        out.writeInt(counter);
    }

    /**
     * Constructor from parcel
     * @param in the parcel
     */
    private SensorStatistics(Parcel in) {
        this.start = in.readLong();
        this.end = in.readLong();
        this.selectedSampleRate = in.readInt();
        this.realSampleRate = in.readDouble();
        this.counter = in.readInt();
    }


}
