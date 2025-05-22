package org.traccar.sms.smpp;

import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.model.Event;


public final class TextMessageEventHandler {
    public static void handleTextMessage(String phone, String message) {
        Device device = Context.getDeviceManager().getDeviceByPhone(phone);
        if (device != null && Context.getNotificationManager() != null) {
            Event event = new Event("textMessage", device.getId());
            event.set("message", message);
            Context.getNotificationManager().updateEvent(event, null);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\sms\smpp\TextMessageEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */