package org.traccar.helper;

import java.beans.Introspector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.model.BaseModel;


public final class LogAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAction.class);

    private static final String ACTION_CREATE = "create";

    private static final String ACTION_EDIT = "edit";

    private static final String ACTION_REMOVE = "remove";

    private static final String ACTION_LINK = "link";

    private static final String ACTION_UNLINK = "unlink";

    private static final String ACTION_LOGIN = "login";

    private static final String ACTION_LOGOUT = "logout";

    private static final String ACTION_DEVICE_ACCUMULATORS = "resetDeviceAccumulators";
    private static final String PATTERN_OBJECT = "user: %d, action: %s, object: %s, id: %d";
    private static final String PATTERN_LINK = "user: %d, action: %s, owner: %s, id: %d, property: %s, id: %d";
    private static final String PATTERN_LOGIN = "user: %d, action: %s";
    private static final String PATTERN_DEVICE_ACCUMULATORS = "user: %d, action: %s, deviceId: %d";

    public static void create(long userId, BaseModel object) {
        logObjectAction("create", userId, object.getClass(), object.getId());
    }

    public static void edit(long userId, BaseModel object) {
        logObjectAction("edit", userId, object.getClass(), object.getId());
    }

    public static void remove(long userId, Class<?> clazz, long objectId) {
        logObjectAction("remove", userId, clazz, objectId);
    }

    public static void link(long userId, Class<?> owner, long ownerId, Class<?> property, long propertyId) {
        logLinkAction("link", userId, owner, ownerId, property, propertyId);
    }

    public static void unlink(long userId, Class<?> owner, long ownerId, Class<?> property, long propertyId) {
        logLinkAction("unlink", userId, owner, ownerId, property, propertyId);
    }

    public static void login(long userId) {
        logLoginAction("login", userId);
    }

    public static void logout(long userId) {
        logLoginAction("logout", userId);
    }

    public static void resetDeviceAccumulators(long userId, long deviceId) {
        LOGGER.info(String.format("user: %d, action: %s, deviceId: %d", new Object[]{
                Long.valueOf(userId), "resetDeviceAccumulators", Long.valueOf(deviceId)}));
    }

    private static void logObjectAction(String action, long userId, Class<?> clazz, long objectId) {
        LOGGER.info(String.format("user: %d, action: %s, object: %s, id: %d", new Object[]{
                Long.valueOf(userId), action, Introspector.decapitalize(clazz.getSimpleName()), Long.valueOf(objectId)
        }));
    }

    private static void logLinkAction(String action, long userId, Class<?> owner, long ownerId, Class<?> property, long propertyId) {
        LOGGER.info(String.format("user: %d, action: %s, owner: %s, id: %d, property: %s, id: %d", new Object[]{
                Long.valueOf(userId), action,
                Introspector.decapitalize(owner.getSimpleName()), Long.valueOf(ownerId),
                Introspector.decapitalize(property.getSimpleName()), Long.valueOf(propertyId)}));
    }

    private static void logLoginAction(String action, long userId) {
        LOGGER.info(String.format("user: %d, action: %s", new Object[]{Long.valueOf(userId), action}));
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\LogAction.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */