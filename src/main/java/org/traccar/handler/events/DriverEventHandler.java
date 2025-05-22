package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.Collections;
import java.util.Map;

import org.traccar.database.IdentityManager;
import org.traccar.model.Event;
import org.traccar.model.Position;


@Sharable
public class DriverEventHandler
        extends BaseEventHandler {
    private final IdentityManager identityManager;

    public DriverEventHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        if (!this.identityManager.isLatestPosition(position)) {
            return null;
        }
        String driverUniqueId = position.getString("driverUniqueId");
        if (driverUniqueId != null) {
            String oldDriverUniqueId = null;
            Position lastPosition = this.identityManager.getLastPosition(position.getDeviceId());
            if (lastPosition != null) {
                oldDriverUniqueId = lastPosition.getString("driverUniqueId");
            }
            if (!driverUniqueId.equals(oldDriverUniqueId)) {
                Event event = new Event("driverChanged", position.getDeviceId(), position.getId());
                event.set("driverUniqueId", driverUniqueId);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\DriverEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */