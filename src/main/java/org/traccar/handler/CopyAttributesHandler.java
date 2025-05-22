package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import org.traccar.BaseDataHandler;
import org.traccar.database.IdentityManager;
import org.traccar.model.Position;


@Sharable
public class CopyAttributesHandler
        extends BaseDataHandler {
    private IdentityManager identityManager;

    public CopyAttributesHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }


    protected Position handlePosition(Position position) {
        String attributesString = this.identityManager.lookupAttributeString(position
                .getDeviceId(), "processing.copyAttributes", "", true);
        if (attributesString.isEmpty()) {
            attributesString = "driverUniqueId";
        } else {
            attributesString = attributesString + ",driverUniqueId";
        }
        Position last = this.identityManager.getLastPosition(position.getDeviceId());
        if (last != null) {
            for (String attribute : attributesString.split("[ ,]")) {
                if (last.getAttributes().containsKey(attribute) && !position.getAttributes().containsKey(attribute)) {
                    position.getAttributes().put(attribute, last.getAttributes().get(attribute));
                }
            }
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\CopyAttributesHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */