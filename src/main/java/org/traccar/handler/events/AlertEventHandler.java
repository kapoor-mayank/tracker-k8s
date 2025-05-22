package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.Collections;
import java.util.Map;

import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.IdentityManager;
import org.traccar.model.Event;
import org.traccar.model.Position;


@Sharable
public class AlertEventHandler
        extends BaseEventHandler {
    private final IdentityManager identityManager;
    private final boolean ignoreDuplicateAlerts;

    public AlertEventHandler(Config config, IdentityManager identityManager) {
        this.identityManager = identityManager;
        this.ignoreDuplicateAlerts = config.getBoolean(Keys.EVENT_IGNORE_DUPLICATE_ALERTS);
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        Object alarm = position.getAttributes().get("alarm");
        if (alarm != null) {
            boolean ignoreAlert = false;
            if (this.ignoreDuplicateAlerts) {
                Position lastPosition = this.identityManager.getLastPosition(position.getDeviceId());
                if (lastPosition != null && alarm.equals(lastPosition.getAttributes().get("alarm"))) {
                    ignoreAlert = true;
                }
            }
            if (!ignoreAlert) {
                Event event = new Event("alarm", position.getDeviceId(), position.getId());
                event.set("alarm", (String) alarm);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\AlertEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */