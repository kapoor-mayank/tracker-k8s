package org.traccar.api;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.database.ConnectionManager;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;


public class AsyncSocket
        extends WebSocketAdapter
        implements ConnectionManager.UpdateListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncSocket.class);

    private static final String KEY_DEVICES = "devices";

    private static final String KEY_POSITIONS = "positions";
    private static final String KEY_EVENTS = "events";
    private long userId;

    public AsyncSocket(long userId) {
        this.userId = userId;
    }


    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        Map<String, Collection<?>> data = new HashMap<>();
        data.put("positions", Context.getDeviceManager().getInitialState(this.userId));
        sendData(data);

        Context.getConnectionManager().addListener(this.userId, this);
    }


    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        Context.getConnectionManager().removeListener(this.userId, this);
    }


    public void onUpdateDevice(Device device) {
        Map<String, Collection<?>> data = new HashMap<>();
        data.put("devices", Collections.singletonList(device));
        sendData(data);
    }


    public void onUpdatePosition(Position position) {
        Map<String, Collection<?>> data = new HashMap<>();
        data.put("positions", Collections.singletonList(position));
        sendData(data);
    }


    public void onUpdateEvent(Event event) {
        Map<String, Collection<?>> data = new HashMap<>();
        data.put("events", Collections.singletonList(event));
        sendData(data);
    }

    private void sendData(Map<String, Collection<?>> data) {
        if (!data.isEmpty() && isConnected())
            try {
                getRemote().sendString(Context.getObjectMapper().writeValueAsString(data), null);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Socket JSON formatting error", (Throwable) e);
            }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\AsyncSocket.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */