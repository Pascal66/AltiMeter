package pl.gregoryiwanek.altimeter.app.data.location.managers;

public class GpsManager {

    private boolean mGpsEnabled;
    private long mMeasureTime = 0;

    public void setGpsEnabled(boolean gpsEnabled) {
        mGpsEnabled = gpsEnabled;
    }

    public boolean isGpsEnabled() {
        return mGpsEnabled;
    }

    public long getMeasureTime() {
        return mMeasureTime;
    }

    public void setMeasureTime(long measureTime) {
        mMeasureTime = measureTime;
    }

    public void resetData() {
        mGpsEnabled = false;
        mMeasureTime = 0;
    }
}


