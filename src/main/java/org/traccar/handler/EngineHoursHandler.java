package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import org.traccar.BaseDataHandler;
import org.traccar.database.IdentityManager;
import org.traccar.model.Position;


@Sharable
public class EngineHoursHandler
        extends BaseDataHandler {
    private final IdentityManager identityManager;

    public EngineHoursHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }


    protected Position handlePosition(Position position) {
        if (!position.getAttributes().containsKey("hours")) {
            Position last = this.identityManager.getLastPosition(position.getDeviceId());
            if (last != null) {
                long hours = last.getLong("hours");
                if (last.getBoolean("ignition") && position.getBoolean("ignition")) {
                    hours += position.getFixTime().getTime() - last.getFixTime().getTime();
                }
                if (hours != 0L) {
                    position.set("hours", Long.valueOf(hours));
                }
            }
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\EngineHoursHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */