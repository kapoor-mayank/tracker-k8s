package org.traccar;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.ContextResolver;

import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.config.Config;
import org.traccar.database.AttributesManager;
import org.traccar.database.BaseObjectManager;
import org.traccar.database.CalendarManager;
import org.traccar.database.CommandsManager;
import org.traccar.database.ConnectionManager;
import org.traccar.database.DataManager;
import org.traccar.database.DeviceManager;
import org.traccar.database.DriversManager;
import org.traccar.database.GeofenceManager;
import org.traccar.database.GroupsManager;
import org.traccar.database.IdentityManager;
import org.traccar.database.LdapProvider;
import org.traccar.database.MailManager;
import org.traccar.database.MaintenancesManager;
import org.traccar.database.MediaManager;
import org.traccar.database.NotificationManager;
import org.traccar.database.PermissionsManager;
import org.traccar.database.RedisManager;
import org.traccar.database.UsersManager;
import org.traccar.geocoder.Geocoder;
import org.traccar.helper.Log;
import org.traccar.helper.SanitizerModule;
import org.traccar.model.Attribute;
import org.traccar.model.Calendar;
import org.traccar.model.Command;
import org.traccar.model.Device;
import org.traccar.model.Driver;
import org.traccar.model.Geofence;
import org.traccar.model.Group;
import org.traccar.model.Maintenance;
import org.traccar.model.Notification;
import org.traccar.model.User;
import org.traccar.notification.EventForwarder;
import org.traccar.notification.JsonTypeEventForwarder;
import org.traccar.notification.NotificatorManager;
import org.traccar.reports.model.TripsConfig;
import org.traccar.sms.SmsManager;
import org.traccar.web.WebServer;


public final class Context {
    private static Config config;
    private static DataForwarder dataForwarder;
    private static ObjectMapper objectMapper;
    private static IdentityManager identityManager;
    private static DataManager dataManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);
    private static LdapProvider ldapProvider;
    private static MailManager mailManager;
    private static MediaManager mediaManager;
    private static RedisManager redisManager;
    private static UsersManager usersManager;
    private static GroupsManager groupsManager;
    private static DeviceManager deviceManager;
    private static ConnectionManager connectionManager;

    public static Config getConfig() {
        return config;
    }

    private static PermissionsManager permissionsManager;
    private static WebServer webServer;

    public static DataForwarder getDataForwarder() {
        return dataForwarder;
    }

    private static ServerManager serverManager;
    private static GeofenceManager geofenceManager;
    private static CalendarManager calendarManager;

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static NotificationManager notificationManager;
    private static NotificatorManager notificatorManager;
    private static VelocityEngine velocityEngine;

    public static IdentityManager getIdentityManager() {
        return identityManager;
    }


    public static DataManager getDataManager() {
        return dataManager;
    }


    public static LdapProvider getLdapProvider() {
        return ldapProvider;
    }


    public static MailManager getMailManager() {
        return mailManager;
    }


    public static MediaManager getMediaManager() {
        return mediaManager;
    }


    public static RedisManager getRedisManager() {
        return redisManager;
    }


    public static UsersManager getUsersManager() {
        return usersManager;
    }


    public static GroupsManager getGroupsManager() {
        return groupsManager;
    }


    public static DeviceManager getDeviceManager() {
        return deviceManager;
    }


    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }


    public static PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public static Geocoder getGeocoder() {
        return (Main.getInjector() != null) ? (Geocoder) Main.getInjector().getInstance(Geocoder.class) : null;
    }


    public static WebServer getWebServer() {
        return webServer;
    }


    public static ServerManager getServerManager() {
        return serverManager;
    }


    public static GeofenceManager getGeofenceManager() {
        return geofenceManager;
    }


    public static CalendarManager getCalendarManager() {
        return calendarManager;
    }


    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }


    public static NotificatorManager getNotificatorManager() {
        return notificatorManager;
    }


    public static VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    private static Client client = ClientBuilder.newClient();
    private static EventForwarder eventForwarder;
    private static AttributesManager attributesManager;
    private static DriversManager driversManager;

    public static Client getClient() {
        return client;
    }

    private static CommandsManager commandsManager;
    private static MaintenancesManager maintenancesManager;
    private static SmsManager smsManager;
    private static TripsConfig tripsConfig;

    public static EventForwarder getEventForwarder() {
        return eventForwarder;
    }


    public static AttributesManager getAttributesManager() {
        return attributesManager;
    }


    public static DriversManager getDriversManager() {
        return driversManager;
    }


    public static CommandsManager getCommandsManager() {
        return commandsManager;
    }


    public static MaintenancesManager getMaintenancesManager() {
        return maintenancesManager;
    }


    public static SmsManager getSmsManager() {
        return smsManager;
    }


    public static TripsConfig getTripsConfig() {
        return tripsConfig;
    }

    public static TripsConfig initTripsConfig() {
        return new TripsConfig(config
                .getLong("report.trip.minimalTripDistance", 500L), config
                .getLong("report.trip.minimalTripDuration", 300L) * 1000L, config
                .getLong("report.trip.minimalParkingDuration", 300L) * 1000L, config
                .getLong("report.trip.minimalNoDataDuration", 3600L) * 1000L, config
                .getBoolean("report.trip.useIgnition"), config
                .getBoolean("event.motion.processInvalidPositions"), config
                .getDouble("event.motion.speedThreshold", 0.01D));
    }

    private static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
        private ObjectMapperContextResolver() {
        }

        public ObjectMapper getContext(Class<?> clazz) {
            return Context.objectMapper;
        }
    }


    public static void init(String configFile) throws Exception {
        try {
            config = new Config(configFile);
        } catch (Exception e) {
            config = new Config();
            Log.setupDefaultLogger();
            throw e;
        }

        if (config.getBoolean("logger.enable")) {
            Log.setupLogger(config);
        }

        objectMapper = new ObjectMapper();
        objectMapper.registerModule((Module) new SanitizerModule());
        objectMapper.registerModule((Module) new JSR353Module());
        objectMapper.setConfig(objectMapper
                .getSerializationConfig().without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        if (getConfig().getBoolean("mapper.prettyPrintedJson")) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        client = (Client) ClientBuilder.newClient().register(new ObjectMapperContextResolver());

        if (config.hasKey("database.url")) {
            dataManager = new DataManager(config);
        }

        if (config.getBoolean("ldap.enable")) {
            ldapProvider = new LdapProvider(config);
        }

        mailManager = new MailManager();

        mediaManager = new MediaManager(config.getString("media.path"));

        if (config.getBoolean("redis.enable")) {
            redisManager = new RedisManager();
        }

        if (dataManager != null) {
            usersManager = new UsersManager(dataManager);
            groupsManager = new GroupsManager(dataManager);
            deviceManager = new DeviceManager(dataManager);
        }

        identityManager = (IdentityManager) deviceManager;

        dataForwarder = new DataForwarder();

        if (config.getBoolean("web.enable")) {
            webServer = new WebServer(config);
        }

        permissionsManager = new PermissionsManager(dataManager, usersManager);

        connectionManager = new ConnectionManager();

        tripsConfig = initTripsConfig();

        initEventsModule();

        serverManager = new ServerManager();

        if (config.getBoolean("event.forward.enable")) {
            eventForwarder = (EventForwarder) new JsonTypeEventForwarder();
        }

        attributesManager = new AttributesManager(dataManager);

        driversManager = new DriversManager(dataManager);

        commandsManager = new CommandsManager(dataManager, config.getBoolean("commands.queueing"));
    }


    private static void initEventsModule() {
        String address;
        if (config.getBoolean("event.enable")) {
            geofenceManager = new GeofenceManager(dataManager);
            calendarManager = new CalendarManager(dataManager);
        }

        maintenancesManager = new MaintenancesManager(dataManager);
        notificationManager = new NotificationManager(dataManager);
        notificatorManager = new NotificatorManager();
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("file.resource.loader.path",
                getConfig().getString("templates.rootPath", "templates") + "/");
        velocityProperties.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");


        try {
            address = config.getString("web.address", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            address = "localhost";
        }

        String webUrl = URIUtil.newURI("http", address, config.getInteger("web.port", 8082), "", "");
        webUrl = getConfig().getString("web.url", webUrl);
        velocityProperties.setProperty("web.url", webUrl);

        velocityEngine = new VelocityEngine();
        velocityEngine.init(velocityProperties);
    }

    public static void init(IdentityManager testIdentityManager, MediaManager testMediaManager) {
        config = new Config();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule((Module) new JSR353Module());
        client = (Client) ClientBuilder.newClient().register(new ObjectMapperContextResolver());
        identityManager = testIdentityManager;
        mediaManager = testMediaManager;
    }

    public static <T extends org.traccar.model.BaseModel> BaseObjectManager<T> getManager(Class<T> clazz) {
        if (clazz.equals(Device.class))
            return (BaseObjectManager<T>) deviceManager;
        if (clazz.equals(Group.class))
            return (BaseObjectManager<T>) groupsManager;
        if (clazz.equals(User.class))
            return (BaseObjectManager<T>) usersManager;
        if (clazz.equals(Calendar.class))
            return (BaseObjectManager<T>) calendarManager;
        if (clazz.equals(Attribute.class))
            return (BaseObjectManager<T>) attributesManager;
        if (clazz.equals(Geofence.class))
            return (BaseObjectManager<T>) geofenceManager;
        if (clazz.equals(Driver.class))
            return (BaseObjectManager<T>) driversManager;
        if (clazz.equals(Command.class))
            return (BaseObjectManager<T>) commandsManager;
        if (clazz.equals(Maintenance.class))
            return (BaseObjectManager<T>) maintenancesManager;
        if (clazz.equals(Notification.class)) {
            return (BaseObjectManager<T>) notificationManager;
        }
        return null;
    }
}