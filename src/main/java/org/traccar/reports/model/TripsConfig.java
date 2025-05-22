package org.traccar.reports.model;


public class TripsConfig {
    private double minimalTripDistance;
    private long minimalTripDuration;
    private long minimalParkingDuration;
    private long minimalNoDataDuration;
    private boolean useIgnition;
    private boolean processInvalidPositions;
    private double speedThreshold;

    public TripsConfig() {
    }

    public TripsConfig(double minimalTripDistance, long minimalTripDuration, long minimalParkingDuration, long minimalNoDataDuration, boolean useIgnition, boolean processInvalidPositions, double speedThreshold) {
        this.minimalTripDistance = minimalTripDistance;
        this.minimalTripDuration = minimalTripDuration;
        this.minimalParkingDuration = minimalParkingDuration;
        this.minimalNoDataDuration = minimalNoDataDuration;
        this.useIgnition = useIgnition;
        this.processInvalidPositions = processInvalidPositions;
        this.speedThreshold = speedThreshold;
    }


    public double getMinimalTripDistance() {
        return this.minimalTripDistance;
    }

    public void setMinimalTripDistance(double minimalTripDistance) {
        this.minimalTripDistance = minimalTripDistance;
    }


    public long getMinimalTripDuration() {
        return this.minimalTripDuration;
    }

    public void setMinimalTripDuration(long minimalTripDuration) {
        this.minimalTripDuration = minimalTripDuration;
    }


    public long getMinimalParkingDuration() {
        return this.minimalParkingDuration;
    }

    public void setMinimalParkingDuration(long minimalParkingDuration) {
        this.minimalParkingDuration = minimalParkingDuration;
    }


    public long getMinimalNoDataDuration() {
        return this.minimalNoDataDuration;
    }

    public void setMinimalNoDataDuration(long minimalNoDataDuration) {
        this.minimalNoDataDuration = minimalNoDataDuration;
    }


    public boolean getUseIgnition() {
        return this.useIgnition;
    }

    public void setUseIgnition(boolean useIgnition) {
        this.useIgnition = useIgnition;
    }


    public boolean getProcessInvalidPositions() {
        return this.processInvalidPositions;
    }

    public void setProcessInvalidPositions(boolean processInvalidPositions) {
        this.processInvalidPositions = processInvalidPositions;
    }


    public double getSpeedThreshold() {
        return this.speedThreshold;
    }

    public void setSpeedThreshold(double speedThreshold) {
        this.speedThreshold = speedThreshold;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\model\TripsConfig.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */