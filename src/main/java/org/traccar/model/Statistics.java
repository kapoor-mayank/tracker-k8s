package org.traccar.model;

import java.util.Date;


public class Statistics
        extends ExtendedModel {
    private Date captureTime;
    private int activeUsers;
    private int activeDevices;
    private int requests;
    private int messagesReceived;
    private int messagesStored;
    private int mailSent;
    private int smsSent;
    private int geocoderRequests;
    private int geolocationRequests;

    public Date getCaptureTime() {
        return this.captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }


    public int getActiveUsers() {
        return this.activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }


    public int getActiveDevices() {
        return this.activeDevices;
    }

    public void setActiveDevices(int activeDevices) {
        this.activeDevices = activeDevices;
    }


    public int getRequests() {
        return this.requests;
    }

    public void setRequests(int requests) {
        this.requests = requests;
    }


    public int getMessagesReceived() {
        return this.messagesReceived;
    }

    public void setMessagesReceived(int messagesReceived) {
        this.messagesReceived = messagesReceived;
    }


    public int getMessagesStored() {
        return this.messagesStored;
    }

    public void setMessagesStored(int messagesStored) {
        this.messagesStored = messagesStored;
    }


    public int getMailSent() {
        return this.mailSent;
    }

    public void setMailSent(int mailSent) {
        this.mailSent = mailSent;
    }


    public int getSmsSent() {
        return this.smsSent;
    }

    public void setSmsSent(int smsSent) {
        this.smsSent = smsSent;
    }


    public int getGeocoderRequests() {
        return this.geocoderRequests;
    }

    public void setGeocoderRequests(int geocoderRequests) {
        this.geocoderRequests = geocoderRequests;
    }


    public int getGeolocationRequests() {
        return this.geolocationRequests;
    }

    public void setGeolocationRequests(int geolocationRequests) {
        this.geolocationRequests = geolocationRequests;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Statistics.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */