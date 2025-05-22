package org.traccar.reports.model;

import java.util.Date;


public class StopReport
        extends BaseReport {
    private long positionId;
    private double latitude;
    private double longitude;
    private Date startTime;
    private Date endTime;
    private String address;
    private long duration;
    private long engineHours;

    public long getPositionId() {
        return this.positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }


    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }


    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public long getEngineHours() {
        return this.engineHours;
    }

    public void setEngineHours(long engineHours) {
        this.engineHours = engineHours;
    }

    public void addEngineHours(long engineHours) {
        this.engineHours += engineHours;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\model\StopReport.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */