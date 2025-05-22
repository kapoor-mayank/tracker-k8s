package org.traccar.model;


public class DeviceState {
    private Boolean motionState;
    private Position motionPosition;
    private Boolean overspeedState;
    private Position overspeedPosition;
    private long overspeedGeofenceId;

    public void setMotionState(boolean motionState) {
        this.motionState = Boolean.valueOf(motionState);
    }

    public Boolean getMotionState() {
        return this.motionState;
    }


    public void setMotionPosition(Position motionPosition) {
        this.motionPosition = motionPosition;
    }

    public Position getMotionPosition() {
        return this.motionPosition;
    }


    public void setOverspeedState(boolean overspeedState) {
        this.overspeedState = Boolean.valueOf(overspeedState);
    }

    public Boolean getOverspeedState() {
        return this.overspeedState;
    }


    public void setOverspeedPosition(Position overspeedPosition) {
        this.overspeedPosition = overspeedPosition;
    }

    public Position getOverspeedPosition() {
        return this.overspeedPosition;
    }


    public void setOverspeedGeofenceId(long overspeedGeofenceId) {
        this.overspeedGeofenceId = overspeedGeofenceId;
    }

    public long getOverspeedGeofenceId() {
        return this.overspeedGeofenceId;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\DeviceState.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */