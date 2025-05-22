package org.traccar.reports.model;

import java.util.Date;


public class TripReport
        extends BaseReport {
    private long startPositionId;
    private long endPositionId;
    private double startLat;
    private double startLon;
    private double endLat;
    private double endLon;
    private Date startTime;
    private String startAddress;
    private Date endTime;
    private String endAddress;
    private long duration;
    private String driverUniqueId;
    private String driverName;

    public long getStartPositionId() {
        return this.startPositionId;
    }

    public void setStartPositionId(long startPositionId) {
        this.startPositionId = startPositionId;
    }


    public long getEndPositionId() {
        return this.endPositionId;
    }

    public void setEndPositionId(long endPositionId) {
        this.endPositionId = endPositionId;
    }


    public double getStartLat() {
        return this.startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }


    public double getStartLon() {
        return this.startLon;
    }

    public void setStartLon(double startLon) {
        this.startLon = startLon;
    }


    public double getEndLat() {
        return this.endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }


    public double getEndLon() {
        return this.endLon;
    }

    public void setEndLon(double endLon) {
        this.endLon = endLon;
    }


    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    public String getStartAddress() {
        return this.startAddress;
    }

    public void setStartAddress(String address) {
        this.startAddress = address;
    }


    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }


    public String getEndAddress() {
        return this.endAddress;
    }

    public void setEndAddress(String address) {
        this.endAddress = address;
    }


    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public String getDriverUniqueId() {
        return this.driverUniqueId;
    }

    public void setDriverUniqueId(String driverUniqueId) {
        this.driverUniqueId = driverUniqueId;
    }


    public String getDriverName() {
        return this.driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\model\TripReport.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */