package org.traccar.model;

import java.util.Date;
import java.util.List;

import org.traccar.database.QueryExtended;
import org.traccar.database.QueryIgnore;


public class Device
        extends GroupedModel {
    private String name;
    private String uniqueId;
    public static final String STATUS_UNKNOWN = "unknown";
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";
    private String status;
    private Date lastUpdate;
    private long positionId;
    private List<Long> geofenceIds;
    private String phone;
    private String model;
    private String contact;
    private String category;
    private boolean disabled;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }


    @QueryIgnore
    public String getStatus() {
        return (this.status != null) ? this.status : "offline";
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @QueryExtended
    public Date getLastUpdate() {
        if (this.lastUpdate != null) {
            return new Date(this.lastUpdate.getTime());
        }
        return null;
    }


    public void setLastUpdate(Date lastUpdate) {
        if (lastUpdate != null) {
            this.lastUpdate = new Date(lastUpdate.getTime());
        } else {
            this.lastUpdate = null;
        }
    }


    @QueryIgnore
    public long getPositionId() {
        return this.positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }


    @QueryIgnore
    public List<Long> getGeofenceIds() {
        return this.geofenceIds;
    }

    public void setGeofenceIds(List<Long> geofenceIds) {
        this.geofenceIds = geofenceIds;
    }


    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }


    public String getContact() {
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }


    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Device.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */