package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.traccar.database.CalendarManager;
import org.traccar.database.GeofenceManager;
import org.traccar.database.IdentityManager;
import org.traccar.model.Calendar;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Geofence;
import org.traccar.model.Position;


@Sharable
public class GeofenceEventHandler
        extends BaseEventHandler {
    private final IdentityManager identityManager;
    private final GeofenceManager geofenceManager;
    private final CalendarManager calendarManager;

    public GeofenceEventHandler(IdentityManager identityManager, GeofenceManager geofenceManager, CalendarManager calendarManager) {
        this.identityManager = identityManager;
        this.geofenceManager = geofenceManager;
        this.calendarManager = calendarManager;
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        Device device = this.identityManager.getById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!this.identityManager.isLatestPosition(position) || !position.getValid()) {
            return null;
        }

        List<Long> currentGeofences = this.geofenceManager.getCurrentDeviceGeofences(position);
        List<Long> oldGeofences = new ArrayList<>();
        if (device.getGeofenceIds() != null) {
            oldGeofences.addAll(device.getGeofenceIds());
        }
        List<Long> newGeofences = new ArrayList<>(currentGeofences);
        newGeofences.removeAll(oldGeofences);
        oldGeofences.removeAll(currentGeofences);

        device.setGeofenceIds(currentGeofences);

        Map<Event, Position> events = new HashMap<>();
        Iterator<Long> iterator;
        for (iterator = oldGeofences.iterator(); iterator.hasNext(); ) {
            long geofenceId = ((Long) iterator.next()).longValue();
            long calendarId = ((Geofence) this.geofenceManager.getById(geofenceId)).getCalendarId();
            Calendar calendar = (calendarId != 0L) ? (Calendar) this.calendarManager.getById(calendarId) : null;
            if (calendar == null || calendar.checkMoment(position.getFixTime())) {
                Event event = new Event("geofenceExit", position.getDeviceId(), position.getId());
                event.setGeofenceId(geofenceId);
                events.put(event, position);
            }
        }

        for (iterator = newGeofences.iterator(); iterator.hasNext(); ) {
            long geofenceId = ((Long) iterator.next()).longValue();
            long calendarId = ((Geofence) this.geofenceManager.getById(geofenceId)).getCalendarId();
            Calendar calendar = (calendarId != 0L) ? (Calendar) this.calendarManager.getById(calendarId) : null;
            if (calendar == null || calendar.checkMoment(position.getFixTime())) {
                Event event = new Event("geofenceEnter", position.getDeviceId(), position.getId());
                event.setGeofenceId(geofenceId);
                events.put(event, position);
            }
        }

        return events;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\GeofenceEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */