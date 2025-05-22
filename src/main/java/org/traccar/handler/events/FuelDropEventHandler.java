package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.Collections;
import java.util.Map;

import org.traccar.database.IdentityManager;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;


@Sharable
public class FuelDropEventHandler
        extends BaseEventHandler {
    public static final String ATTRIBUTE_FUEL_DROP_THRESHOLD = "fuelDropThreshold";
    private final IdentityManager identityManager;

    public FuelDropEventHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        Device device = this.identityManager.getById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!this.identityManager.isLatestPosition(position)) {
            return null;
        }


        double fuelDropThreshold = this.identityManager.lookupAttributeDouble(device.getId(), "fuelDropThreshold", 0.0D, false);

        if (fuelDropThreshold > 0.0D) {
            Position lastPosition = this.identityManager.getLastPosition(position.getDeviceId());
            if (position.getAttributes().containsKey("fuel") && lastPosition != null && lastPosition
                    .getAttributes().containsKey("fuel")) {


                double drop = lastPosition.getDouble("fuel") - position.getDouble("fuel");
                if (drop >= fuelDropThreshold) {
                    Event event = new Event("deviceFuelDrop", position.getDeviceId(), position.getId());
                    event.set("fuelDropThreshold", Double.valueOf(fuelDropThreshold));
                    return Collections.singletonMap(event, position);
                }
            }
        }

        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\FuelDropEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */