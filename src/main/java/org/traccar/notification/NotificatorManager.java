package org.traccar.notification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.model.Typed;
import org.traccar.notificators.Notificator;
import org.traccar.notificators.NotificatorFirebase;
import org.traccar.notificators.NotificatorMail;
import org.traccar.notificators.NotificatorNull;
import org.traccar.notificators.NotificatorSms;
import org.traccar.notificators.NotificatorWeb;


public final class NotificatorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorManager.class);

    private static final Notificator NULL_NOTIFICATOR = (Notificator) new NotificatorNull();

    private final Map<String, Notificator> notificators = new HashMap<>();

    public NotificatorManager() {
        String[] types = Context.getConfig().getString("notificator.types", "").split(",");
        for (String type : types) {
            String defaultNotificator = "";
            switch (type) {
                case "web":
                    defaultNotificator = NotificatorWeb.class.getCanonicalName();
                    break;
                case "mail":
                    defaultNotificator = NotificatorMail.class.getCanonicalName();
                    break;
                case "sms":
                    defaultNotificator = NotificatorSms.class.getCanonicalName();
                    break;
                case "firebase":
                    defaultNotificator = NotificatorFirebase.class.getCanonicalName();
                    break;
            }


            String className = Context.getConfig().getString("notificator." + type + ".class", defaultNotificator);
            try {


                this.notificators.put(type, (Notificator) Class.forName(className).newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.warn("Unable to load notificator class for " + type + " " + className + " " + e.getMessage());
            }
        }
    }

    public Notificator getNotificator(String type) {
        Notificator notificator = this.notificators.get(type);
        if (notificator == null) {
            LOGGER.warn("No notificator configured for type : " + type);
            return NULL_NOTIFICATOR;
        }
        return notificator;
    }

    public Set<Typed> getAllNotificatorTypes() {
        Set<Typed> result = new HashSet<>();
        for (String notificator : this.notificators.keySet()) {
            result.add(new Typed(notificator));
        }
        return result;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notification\NotificatorManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */