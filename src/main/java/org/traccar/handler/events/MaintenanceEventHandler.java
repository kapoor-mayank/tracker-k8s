package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.traccar.database.IdentityManager;
import org.traccar.database.MaintenancesManager;
import org.traccar.model.Event;
import org.traccar.model.Maintenance;
import org.traccar.model.Position;


@Sharable
public class MaintenanceEventHandler
        extends BaseEventHandler {
    private final IdentityManager identityManager;
    private final MaintenancesManager maintenancesManager;

    public MaintenanceEventHandler(IdentityManager identityManager, MaintenancesManager maintenancesManager) {
        this.identityManager = identityManager;
        this.maintenancesManager = maintenancesManager;
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        if (this.identityManager.getById(position.getDeviceId()) == null ||
                !this.identityManager.isLatestPosition(position)) {
            return null;
        }

        Position lastPosition = this.identityManager.getLastPosition(position.getDeviceId());
        if (lastPosition == null) {
            return null;
        }

        Map<Event, Position> events = new HashMap<>();
        for (Iterator<Long> iterator = this.maintenancesManager.getAllDeviceItems(position.getDeviceId()).iterator(); iterator.hasNext(); ) {
            long maintenanceId = ((Long) iterator.next()).longValue();
            Maintenance maintenance = (Maintenance) this.maintenancesManager.getById(maintenanceId);
            if (maintenance.getPeriod() != 0.0D) {
                double oldValue = lastPosition.getDouble(maintenance.getType());
                double newValue = position.getDouble(maintenance.getType());
                if (oldValue != 0.0D && newValue != 0.0D &&
                        (long) ((oldValue - maintenance.getStart()) / maintenance.getPeriod()) <
                                (long) ((newValue - maintenance.getStart()) / maintenance.getPeriod())) {
                    Event event = new Event("maintenance", position.getDeviceId(), position.getId());
                    event.setMaintenanceId(maintenanceId);
                    event.set(maintenance.getType(), Double.valueOf(newValue));
                    events.put(event, position);
                }
            }
        }


        return events;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\MaintenanceEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */