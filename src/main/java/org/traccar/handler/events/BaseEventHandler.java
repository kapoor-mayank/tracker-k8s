package org.traccar.handler.events;

import java.util.Map;

import org.traccar.BaseDataHandler;
import org.traccar.Context;
import org.traccar.model.Event;
import org.traccar.model.Position;


public abstract class BaseEventHandler
        extends BaseDataHandler {
    protected Position handlePosition(Position position) {
        Map<Event, Position> events = analyzePosition(position);
        if (events != null && Context.getNotificationManager() != null) {
            Context.getNotificationManager().updateEvents(events);
        }
        return position;
    }

    protected abstract Map<Event, Position> analyzePosition(Position paramPosition);
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\BaseEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */