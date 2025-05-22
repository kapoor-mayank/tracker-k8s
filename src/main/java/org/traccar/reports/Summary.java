package org.traccar.reports;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
//import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.traccar.Context;
import org.traccar.model.Position;
import org.traccar.reports.model.SummaryReport;


public final class Summary {
    private static SummaryReport calculateSummaryResult(long deviceId, Date from, Date to) throws SQLException {
        SummaryReport result = new SummaryReport();
        result.setDeviceId(deviceId);
        result.setDeviceName(Context.getIdentityManager().getById(deviceId).getName());
        Collection<Position> positions = Context.getDataManager().getPositions(deviceId, from, to);
        if (positions != null && !positions.isEmpty()) {
            Position firstPosition = null;
            Position previousPosition = null;
            double speedSum = 0.0D;
            boolean engineHoursEnabled = Context.getConfig().getBoolean("processing.engineHours.enable");
            for (Position position : positions) {
                if (firstPosition == null) {
                    firstPosition = position;
                }
                if (engineHoursEnabled && previousPosition != null && position
                        .getBoolean("ignition") && previousPosition
                        .getBoolean("ignition")) {
                    result.addEngineHours(position.getFixTime().getTime() - previousPosition
                            .getFixTime().getTime());
                }
                previousPosition = position;
                speedSum += position.getSpeed();
                result.setMaxSpeed(position.getSpeed());
            }

            boolean ignoreOdometer = Context.getDeviceManager().lookupAttributeBoolean(deviceId, "report.ignoreOdometer", false, true);
            result.setDistance(ReportUtils.calculateDistance(firstPosition, previousPosition, !ignoreOdometer));
            result.setAverageSpeed(Double.valueOf(speedSum / positions.size()));
            result.setSpentFuel(ReportUtils.calculateFuel(firstPosition, previousPosition));

            if (engineHoursEnabled && firstPosition
                    .getAttributes().containsKey("hours") && previousPosition
                    .getAttributes().containsKey("hours")) {
                result.setEngineHours(previousPosition
                        .getLong("hours") - firstPosition.getLong("hours"));
            }

            if (!ignoreOdometer && firstPosition
                    .getDouble("odometer") != 0.0D && previousPosition
                    .getDouble("odometer") != 0.0D) {
                result.setStartOdometer(firstPosition.getDouble("odometer"));
                result.setEndOdometer(previousPosition.getDouble("odometer"));
            } else {
                result.setStartOdometer(firstPosition.getDouble("totalDistance"));
                result.setEndOdometer(previousPosition.getDouble("totalDistance"));
            }
        }

        return result;
    }


    public static Collection<SummaryReport> getObjects(long userId, Collection<Long> deviceIds, Collection<Long> groupIds, Date from, Date to) throws SQLException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<SummaryReport> result = new ArrayList<>();
        for (Iterator<Long> iterator = ReportUtils.getDeviceList(deviceIds, groupIds).iterator(); iterator.hasNext(); ) {
            long deviceId = ((Long) iterator.next()).longValue();
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            result.add(calculateSummaryResult(deviceId, from, to));
        }

        return result;
    }


    public static void getExcel(OutputStream outputStream, long userId, Collection<Long> deviceIds, Collection<Long> groupIds, Date from, Date to) throws SQLException, IOException {
        ReportUtils.checkPeriodLimit(from, to);
        Collection<SummaryReport> summaries = getObjects(userId, deviceIds, groupIds, from, to);
        String templatePath = Context.getConfig().getString("report.templatesPath", "templates/export/");

        try (InputStream inputStream = new FileInputStream(templatePath + "/summary.xlsx")) {
            org.jxls.common.Context jxlsContext = ReportUtils.initializeContext(userId);
            jxlsContext.putVar("summaries", summaries);
            jxlsContext.putVar("from", from);
            jxlsContext.putVar("to", to);
            JxlsHelper.getInstance().setUseFastFormulaProcessor(false)
                    .processTemplate(inputStream, outputStream, jxlsContext);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\Summary.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */