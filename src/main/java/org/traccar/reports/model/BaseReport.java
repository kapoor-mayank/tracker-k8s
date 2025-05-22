package org.traccar.reports.model;


public class BaseReport {
    private long deviceId;
    private String deviceName;
    private double distance;
    private double averageSpeed;
    private double maxSpeed;
    private double spentFuel;
    private double startOdometer;
    private double endOdometer;

    public long getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }


    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void addDistance(double distance) {
        this.distance += distance;
    }


    public double getAverageSpeed() {
        return this.averageSpeed;
    }

    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed.doubleValue();
    }


    public double getMaxSpeed() {
        return this.maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        if (maxSpeed > this.maxSpeed) {
            this.maxSpeed = maxSpeed;
        }
    }


    public double getSpentFuel() {
        return this.spentFuel;
    }

    public void setSpentFuel(double spentFuel) {
        this.spentFuel = spentFuel;
    }


    public double getStartOdometer() {
        return this.startOdometer;
    }

    public void setStartOdometer(double startOdometer) {
        this.startOdometer = startOdometer;
    }


    public double getEndOdometer() {
        return this.endOdometer;
    }

    public void setEndOdometer(double endOdometer) {
        this.endOdometer = endOdometer;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\model\BaseReport.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */