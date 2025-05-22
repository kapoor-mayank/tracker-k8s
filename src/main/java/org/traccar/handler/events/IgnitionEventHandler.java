package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.Collections;
import java.util.Map;

import org.traccar.database.IdentityManager;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;


@Sharable
public class IgnitionEventHandler
        extends BaseEventHandler {
    private final IdentityManager identityManager;

    public IgnitionEventHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        Device device = this.identityManager.getById(position.getDeviceId());
        if (device == null || !this.identityManager.isLatestPosition(position)) {
            return null;
        }

        Map<Event, Position> result = null;

        if (position.getAttributes().containsKey("ignition")) {
            boolean ignition = position.getBoolean("ignition");

            Position lastPosition = this.identityManager.getLastPosition(position.getDeviceId());
            if (lastPosition != null && lastPosition.getAttributes().containsKey("ignition")) {
                boolean oldIgnition = lastPosition.getBoolean("ignition");

                if (ignition && !oldIgnition) {
                    result = Collections.singletonMap(new Event("ignitionOn", position
                            .getDeviceId(), position.getId()), position);
                } else if (!ignition && oldIgnition) {
                    result = Collections.singletonMap(new Event("ignitionOff", position
                            .getDeviceId(), position.getId()), position);
                }
            }
        }
        return result;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\IgnitionEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */