package org.traccar.notification;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
//import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.model.User;
import org.traccar.reports.ReportUtils;

public final class NotificationFormatter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationFormatter.class);

    public static VelocityContext prepareContext(long userId, Event event, Position position) {
        User user = Context.getPermissionsManager().getUser(userId);
        Device device = Context.getIdentityManager().getById(event.getDeviceId());
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("user", user);
        velocityContext.put("device", device);
        velocityContext.put("event", event);
        if (position != null) {
            velocityContext.put("position", position);
            velocityContext.put("speedUnit", ReportUtils.getSpeedUnit(userId));
            velocityContext.put("distanceUnit", ReportUtils.getDistanceUnit(userId));
            velocityContext.put("volumeUnit", ReportUtils.getVolumeUnit(userId));
        }
        if (event.getGeofenceId() != 0L)
            velocityContext.put("geofence", Context.getGeofenceManager().getById(event.getGeofenceId()));
        if (event.getMaintenanceId() != 0L)
            velocityContext.put("maintenance", Context.getMaintenancesManager().getById(event.getMaintenanceId()));
        String driverUniqueId = event.getString("driverUniqueId");
        if (driverUniqueId != null)
            velocityContext.put("driver", Context.getDriversManager().getDriverByUniqueId(driverUniqueId));
        velocityContext.put("webUrl", Context.getVelocityEngine().getProperty("web.url"));
        velocityContext.put("dateTool", new DateTool());
        velocityContext.put("numberTool", new NumberTool());
        velocityContext.put("timezone", ReportUtils.getTimezone(userId));
        velocityContext.put("locale", Locale.getDefault());
        return velocityContext;
    }

    public static Template getTemplate(Event event, String path) {
        Template template;
        try {
            String templateFilePath = Paths.get(path, new String[] { event.getType() + ".vm" }).toString();
            template = Context.getVelocityEngine().getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
        } catch (ResourceNotFoundException error) {
            LOGGER.warn("Notification template error", (Throwable)error);
            String templateFilePath = Paths.get(path, new String[] { "unknown.vm" }).toString();
            template = Context.getVelocityEngine().getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
        }
        return template;
    }

    public static FullMessage formatFullMessage(long userId, Event event, Position position) {
        VelocityContext velocityContext = prepareContext(userId, event, position);
        String formattedMessage = formatMessage(velocityContext, Long.valueOf(userId), event, position, "full");
        return new FullMessage((String)velocityContext.get("subject"), formattedMessage);
    }

    public static String formatShortMessage(long userId, Event event, Position position) {
        return formatMessage(null, Long.valueOf(userId), event, position, "short");
    }

    private static String formatMessage(VelocityContext vc, Long userId, Event event, Position position, String templatePath) {
        VelocityContext velocityContext = vc;
        if (velocityContext == null)
            velocityContext = prepareContext(userId.longValue(), event, position);
        StringWriter writer = new StringWriter();
        getTemplate(event, templatePath).merge((org.apache.velocity.context.Context)velocityContext, writer);
        return writer.toString();
    }
}
