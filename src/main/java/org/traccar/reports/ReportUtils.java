package org.traccar.reports;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.jxls.area.Area;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
//import org.jxls.common.Context;
import org.jxls.formula.FormulaProcessor;
import org.jxls.formula.StandardFormulaProcessor;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.TransformerFactory;
import org.traccar.Context;
import org.traccar.database.DeviceManager;
import org.traccar.database.IdentityManager;
import org.traccar.handler.events.MotionEventHandler;
import org.traccar.model.DeviceState;
import org.traccar.model.Driver;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.reports.model.StopReport;
import org.traccar.reports.model.TripReport;
import org.traccar.reports.model.TripsConfig;

public final class ReportUtils {
    public static void checkPeriodLimit(Date from, Date to) {
        long limit = Context.getConfig().getLong("report.periodLimit") * 1000L;
        if (limit > 0L && to.getTime() - from.getTime() > limit)
            throw new IllegalArgumentException("Time period exceeds the limit");
    }

    public static String getDistanceUnit(long userId) {
        return (String)Context.getPermissionsManager().lookupAttribute(userId, "distanceUnit", "km");
    }

    public static String getSpeedUnit(long userId) {
        return (String)Context.getPermissionsManager().lookupAttribute(userId, "speedUnit", "kn");
    }

    public static String getVolumeUnit(long userId) {
        return (String)Context.getPermissionsManager().lookupAttribute(userId, "volumeUnit", "ltr");
    }

    public static TimeZone getTimezone(long userId) {
        String timezone = (String)Context.getPermissionsManager().lookupAttribute(userId, "timezone", null);
        return (timezone != null) ? TimeZone.getTimeZone(timezone) : TimeZone.getDefault();
    }

    public static Collection<Long> getDeviceList(Collection<Long> deviceIds, Collection<Long> groupIds) {
        Collection<Long> result = new ArrayList<>();
        result.addAll(deviceIds);
        for (Iterator<Long> iterator = groupIds.iterator(); iterator.hasNext(); ) {
            long groupId = ((Long)iterator.next()).longValue();
            result.addAll(Context.getPermissionsManager().getGroupDevices(groupId));
        }
        return result;
    }

    public static double calculateDistance(Position firstPosition, Position lastPosition) {
        return calculateDistance(firstPosition, lastPosition, true);
    }

    public static double calculateDistance(Position firstPosition, Position lastPosition, boolean useOdometer) {
        double distance = 0.0D;
        double firstOdometer = firstPosition.getDouble("odometer");
        double lastOdometer = lastPosition.getDouble("odometer");
        if (useOdometer && (firstOdometer != 0.0D || lastOdometer != 0.0D)) {
            distance = lastOdometer - firstOdometer;
        } else if (firstPosition.getAttributes().containsKey("totalDistance") && lastPosition
                .getAttributes().containsKey("totalDistance")) {
            distance = lastPosition.getDouble("totalDistance") - firstPosition.getDouble("totalDistance");
        }
        return distance;
    }

    public static double calculateFuel(Position firstPosition, Position lastPosition) {
        if (firstPosition.getAttributes().get("fuel") != null && lastPosition
                .getAttributes().get("fuel") != null) {
            BigDecimal value = new BigDecimal(firstPosition.getDouble("fuel") - lastPosition.getDouble("fuel"));
            return value.setScale(1, RoundingMode.HALF_EVEN).doubleValue();
        }
        return 0.0D;
    }

    public static String findDriver(Position firstPosition, Position lastPosition) {
        if (firstPosition.getAttributes().containsKey("driverUniqueId"))
            return firstPosition.getString("driverUniqueId");
        if (lastPosition.getAttributes().containsKey("driverUniqueId"))
            return lastPosition.getString("driverUniqueId");
        return null;
    }

    public static String findDriverName(String driverUniqueId) {
        if (driverUniqueId != null && Context.getDriversManager() != null) {
            Driver driver = Context.getDriversManager().getDriverByUniqueId(driverUniqueId);
            if (driver != null)
                return driver.getName();
        }
        return null;
    }

    public static org.jxls.common.Context initializeContext(long userId) {
        org.jxls.common.Context jxlsContext = PoiTransformer.createInitialContext();
        jxlsContext.putVar("distanceUnit", getDistanceUnit(userId));
        jxlsContext.putVar("speedUnit", getSpeedUnit(userId));
        jxlsContext.putVar("volumeUnit", getVolumeUnit(userId));
        jxlsContext.putVar("webUrl", Context.getVelocityEngine().getProperty("web.url"));
        jxlsContext.putVar("dateTool", new DateTool());
        jxlsContext.putVar("numberTool", new NumberTool());
        jxlsContext.putVar("timezone", getTimezone(userId));
        jxlsContext.putVar("locale", Locale.getDefault());
        jxlsContext.putVar("bracketsRegex", "[\\{\\}\"]");
        return jxlsContext;
    }

    public static void processTemplateWithSheets(InputStream templateStream, OutputStream targetStream, org.jxls.common.Context jxlsContext) throws IOException {
        Transformer transformer = TransformerFactory.createTransformer(templateStream, targetStream);
        List<Area> xlsAreas = (new XlsCommentAreaBuilder(transformer)).build();
        for (Area xlsArea : xlsAreas) {
            xlsArea.applyAt(new CellRef(xlsArea.getStartCellRef().getCellName()), jxlsContext);
            xlsArea.setFormulaProcessor((FormulaProcessor)new StandardFormulaProcessor());
            xlsArea.processFormulas();
        }
        transformer.deleteSheet(((Area)xlsAreas.get(0)).getStartCellRef().getSheetName());
        transformer.write();
    }

    private static TripReport calculateTrip(ArrayList<Position> positions, int startIndex, int endIndex, boolean ignoreOdometer) {
        Position startTrip = positions.get(startIndex);
        Position endTrip = positions.get(endIndex);
        double speedMax = 0.0D;
        double speedSum = 0.0D;
        for (int i = startIndex; i <= endIndex; i++) {
            double speed = ((Position)positions.get(i)).getSpeed();
            speedSum += speed;
            if (speed > speedMax)
                speedMax = speed;
        }
        TripReport trip = new TripReport();
        long tripDuration = endTrip.getFixTime().getTime() - startTrip.getFixTime().getTime();
        long deviceId = startTrip.getDeviceId();
        trip.setDeviceId(deviceId);
        trip.setDeviceName(Context.getIdentityManager().getById(deviceId).getName());
        trip.setStartPositionId(startTrip.getId());
        trip.setStartLat(startTrip.getLatitude());
        trip.setStartLon(startTrip.getLongitude());
        trip.setStartTime(startTrip.getFixTime());
        String startAddress = startTrip.getAddress();
        if (startAddress == null && Context.getGeocoder() != null &&
                Context.getConfig().getBoolean("geocoder.onRequest"))
            startAddress = Context.getGeocoder().getAddress(startTrip.getLatitude(), startTrip.getLongitude(), null);
        trip.setStartAddress(startAddress);
        trip.setEndPositionId(endTrip.getId());
        trip.setEndLat(endTrip.getLatitude());
        trip.setEndLon(endTrip.getLongitude());
        trip.setEndTime(endTrip.getFixTime());
        String endAddress = endTrip.getAddress();
        if (endAddress == null && Context.getGeocoder() != null &&
                Context.getConfig().getBoolean("geocoder.onRequest"))
            endAddress = Context.getGeocoder().getAddress(endTrip.getLatitude(), endTrip.getLongitude(), null);
        trip.setEndAddress(endAddress);
        trip.setDistance(calculateDistance(startTrip, endTrip, !ignoreOdometer));
        trip.setDuration(tripDuration);
        trip.setAverageSpeed(Double.valueOf(speedSum / (endIndex - startIndex)));
        trip.setMaxSpeed(speedMax);
        trip.setSpentFuel(calculateFuel(startTrip, endTrip));
        trip.setDriverUniqueId(findDriver(startTrip, endTrip));
        trip.setDriverName(findDriverName(trip.getDriverUniqueId()));
        if (!ignoreOdometer && startTrip
                .getDouble("odometer") != 0.0D && endTrip
                .getDouble("odometer") != 0.0D) {
            trip.setStartOdometer(startTrip.getDouble("odometer"));
            trip.setEndOdometer(endTrip.getDouble("odometer"));
        } else {
            trip.setStartOdometer(startTrip.getDouble("totalDistance"));
            trip.setEndOdometer(endTrip.getDouble("totalDistance"));
        }
        return trip;
    }

    private static StopReport calculateStop(ArrayList<Position> positions, int startIndex, int endIndex, boolean ignoreOdometer) {
        Position startStop = positions.get(startIndex);
        Position endStop = positions.get(endIndex);
        StopReport stop = new StopReport();
        long deviceId = startStop.getDeviceId();
        stop.setDeviceId(deviceId);
        stop.setDeviceName(Context.getIdentityManager().getById(deviceId).getName());
        stop.setPositionId(startStop.getId());
        stop.setLatitude(startStop.getLatitude());
        stop.setLongitude(startStop.getLongitude());
        stop.setStartTime(startStop.getFixTime());
        String address = startStop.getAddress();
        if (address == null && Context.getGeocoder() != null &&
                Context.getConfig().getBoolean("geocoder.onRequest"))
            address = Context.getGeocoder().getAddress(stop.getLatitude(), stop.getLongitude(), null);
        stop.setAddress(address);
        stop.setEndTime(endStop.getFixTime());
        long stopDuration = endStop.getFixTime().getTime() - startStop.getFixTime().getTime();
        stop.setDuration(stopDuration);
        stop.setSpentFuel(calculateFuel(startStop, endStop));
        long engineHours = 0L;
        if (startStop.getAttributes().containsKey("hours") && endStop
                .getAttributes().containsKey("hours")) {
            engineHours = endStop.getLong("hours") - startStop.getLong("hours");
        } else if (Context.getConfig().getBoolean("processing.engineHours.enable")) {
            for (int i = startIndex + 1; i <= endIndex; i++) {
                if (((Position)positions.get(i)).getBoolean("ignition") && ((Position)positions
                        .get(i - 1)).getBoolean("ignition"))
                    engineHours += ((Position)positions.get(i)).getFixTime().getTime() - ((Position)positions
                            .get(i - 1)).getFixTime().getTime();
            }
        }
        stop.setEngineHours(engineHours);
        if (!ignoreOdometer && startStop
                .getDouble("odometer") != 0.0D && endStop
                .getDouble("odometer") != 0.0D) {
            stop.setStartOdometer(startStop.getDouble("odometer"));
            stop.setEndOdometer(endStop.getDouble("odometer"));
        } else {
            stop.setStartOdometer(startStop.getDouble("totalDistance"));
            stop.setEndOdometer(endStop.getDouble("totalDistance"));
        }
        return stop;
    }

    private static <T extends org.traccar.reports.model.BaseReport> T calculateTripOrStop(ArrayList<Position> positions, int startIndex, int endIndex, boolean ignoreOdometer, Class<T> reportClass) {
        if (reportClass.equals(TripReport.class))
            return (T)calculateTrip(positions, startIndex, endIndex, ignoreOdometer);
        return (T)calculateStop(positions, startIndex, endIndex, ignoreOdometer);
    }

    private static boolean isMoving(ArrayList<Position> positions, int index, TripsConfig tripsConfig) {
        if (tripsConfig.getMinimalNoDataDuration() > 0L) {
            boolean beforeGap = (index < positions.size() - 1 && ((Position)positions.get(index + 1)).getFixTime().getTime() - ((Position)positions.get(index)).getFixTime().getTime() >= tripsConfig.getMinimalNoDataDuration());
            boolean afterGap = (index > 0 && ((Position)positions.get(index)).getFixTime().getTime() - ((Position)positions.get(index - 1)).getFixTime().getTime() >= tripsConfig.getMinimalNoDataDuration());
            if (beforeGap || afterGap)
                return false;
        }
        if (((Position)positions.get(index)).getAttributes().containsKey("motion") && ((Position)positions
                .get(index)).getAttributes().get("motion") instanceof Boolean)
            return ((Position)positions.get(index)).getBoolean("motion");
        return (((Position)positions.get(index)).getSpeed() > tripsConfig.getSpeedThreshold());
    }

    public static <T extends org.traccar.reports.model.BaseReport> Collection<T> detectTripsAndStops(IdentityManager identityManager, DeviceManager deviceManager, Collection<Position> positionCollection, TripsConfig tripsConfig, boolean ignoreOdometer, Class<T> reportClass) {
        Collection<T> result = new ArrayList<>();
        ArrayList<Position> positions = new ArrayList<>(positionCollection);
        if (!positions.isEmpty()) {
            boolean trips = reportClass.equals(TripReport.class);
            MotionEventHandler motionHandler = new MotionEventHandler(identityManager, deviceManager, tripsConfig);
            DeviceState deviceState = new DeviceState();
            deviceState.setMotionState(isMoving(positions, 0, tripsConfig));
            int startEventIndex = (trips == deviceState.getMotionState().booleanValue()) ? 0 : -1;
            int startNoEventIndex = -1;
            for (int i = 0; i < positions.size(); i++) {
                Map<Event, Position> event = motionHandler.updateMotionState(deviceState, positions.get(i),
                        isMoving(positions, i, tripsConfig));
                if (startEventIndex == -1 && ((trips != deviceState
                        .getMotionState().booleanValue() && deviceState.getMotionPosition() != null) || (trips == deviceState
                        .getMotionState().booleanValue() && event != null))) {
                    startEventIndex = i;
                    startNoEventIndex = -1;
                } else if (trips != deviceState.getMotionState().booleanValue() && startEventIndex != -1 && deviceState
                        .getMotionPosition() == null && event == null) {
                    startEventIndex = -1;
                }
                if (startNoEventIndex == -1 && ((trips == deviceState
                        .getMotionState().booleanValue() && deviceState.getMotionPosition() != null) || (trips != deviceState
                        .getMotionState().booleanValue() && event != null))) {
                    startNoEventIndex = i;
                } else if (startNoEventIndex != -1 && deviceState.getMotionPosition() == null && event == null) {
                    startNoEventIndex = -1;
                }
                if (startEventIndex != -1 && startNoEventIndex != -1 && event != null && trips != deviceState
                        .getMotionState().booleanValue()) {
                    result.add(calculateTripOrStop(positions, startEventIndex, startNoEventIndex, ignoreOdometer, reportClass));
                    startEventIndex = -1;
                }
            }
            if (startEventIndex != -1 && (startNoEventIndex != -1 || !trips))
                result.add(calculateTripOrStop(positions, startEventIndex, (startNoEventIndex != -1) ? startNoEventIndex : (positions
                        .size() - 1), ignoreOdometer, reportClass));
        }
        return result;
    }
}
