package ch.ethz.matsim.av.electric;

import org.matsim.core.config.ReflectiveConfigGroup;

public class RechargingConfig extends ReflectiveConfigGroup {
    final static public String RECHARGING = "recharging";
    final static public String TRACK_CONSUMPTION = "trackConsumption";
    final static public String TRACKING_START_TIME = "trackingStartTime";
    final static public String TRACKING_END_TIME = "trackingEndTime";
    final static public String TRACKING_BIN_DURATION = "trackingBinDuration";

    private boolean trackConsumption = false;

    private int trackingStartTime = 0;
    private int trackingEndTime = 1;
    private int trackingBinDuration = 0;

    public RechargingConfig() {
        super(RECHARGING);
    }

    @StringGetter(TRACK_CONSUMPTION)
    public boolean getTrackConsumption() {
        return trackConsumption;
    }

    @StringSetter(TRACK_CONSUMPTION)
    public void setTrackConsumption(boolean trackConsumption) {
        this.trackConsumption = trackConsumption;
    }

    @StringGetter(TRACKING_START_TIME)
    public int getTrackingStartTime() {
        return trackingStartTime;
    }

    @StringSetter(TRACKING_START_TIME)
    public void setTrackingStartTime(int trackingStartTime) {
        this.trackingStartTime = trackingStartTime;
    }

    @StringGetter(TRACKING_END_TIME)
    public int getTrackingEndTime() {
        return trackingEndTime;
    }

    @StringSetter(TRACKING_END_TIME)
    public void setTrackingEndTime(int trackingEndTime) {
        this.trackingEndTime = trackingEndTime;
    }

    @StringGetter(TRACKING_BIN_DURATION)
    public int getTrackingBinDuration() {
        return trackingBinDuration;
    }

    @StringSetter(TRACKING_BIN_DURATION)
    public void setTrackingBinDuration(int trackingBinDuration) {
        this.trackingBinDuration = trackingBinDuration;
    }
}
