package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseDataHandler;
import org.traccar.database.DataManager;
import org.traccar.model.BaseModel;
import org.traccar.model.Position;


@Sharable
public class DefaultDataHandler
        extends BaseDataHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataHandler.class);

    private final DataManager dataManager;

    public DefaultDataHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }


    protected Position handlePosition(Position position) {
        try {
            this.dataManager.addObject((BaseModel) position);
        } catch (Exception error) {
            LOGGER.warn("Failed to store position", error);
        }

        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\DefaultDataHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */