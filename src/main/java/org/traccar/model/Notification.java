package org.traccar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

import org.traccar.database.QueryIgnore;


public class Notification
        extends ScheduledModel {
    private boolean always;
    private String type;
    private String notificators;

    public boolean getAlways() {
        return this.always;
    }

    public void setAlways(boolean always) {
        this.always = always;
    }


    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getNotificators() {
        return this.notificators;
    }

    public void setNotificators(String transports) {
        this.notificators = transports;
    }


    @JsonIgnore
    @QueryIgnore
    public Set<String> getNotificatorsTypes() {
        Set<String> result = new HashSet<>();
        if (this.notificators != null) {
            String[] transportsList = this.notificators.split(",");
            for (String transport : transportsList) {
                result.add(transport.trim());
            }
        }
        return result;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Notification.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */