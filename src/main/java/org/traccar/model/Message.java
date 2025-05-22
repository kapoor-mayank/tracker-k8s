package org.traccar.model;


public class Message
        extends ExtendedModel {
    private long deviceId;
    private String type;

    public long getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }


    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Message.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */