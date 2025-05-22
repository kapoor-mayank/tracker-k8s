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

import org.apache.poi.ss.util.WorkbookUtil;
//import org.jxls.common.Context;
import org.traccar.Context;
import org.traccar.Main;
import org.traccar.database.DeviceManager;
import org.traccar.database.IdentityManager;
import org.traccar.model.Device;
import org.traccar.model.Group;
import org.traccar.reports.model.DeviceReport;
import org.traccar.reports.model.StopReport;


public final class Stops {
    private static Collection<StopReport> detectStops(long deviceId, Date from, Date to) throws SQLException {
        boolean ignoreOdometer = Context.getDeviceManager().lookupAttributeBoolean(deviceId, "report.ignoreOdometer", false, true);

        IdentityManager identityManager = (IdentityManager) Main.getInjector().getInstance(IdentityManager.class);
        DeviceManager deviceManager = (DeviceManager) Main.getInjector().getInstance(DeviceManager.class);

        return ReportUtils.detectTripsAndStops(identityManager, deviceManager,
                Context.getDataManager().getPositions(deviceId, from, to),
                Context.getTripsConfig(), ignoreOdometer, StopReport.class);
    }


    public static Collection<StopReport> getObjects(long userId, Collection<Long> deviceIds, Collection<Long> groupIds, Date from, Date to) throws SQLException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<StopReport> result = new ArrayList<>();
        for (Iterator<Long> iterator = ReportUtils.getDeviceList(deviceIds, groupIds).iterator(); iterator.hasNext(); ) {
            long deviceId = ((Long) iterator.next()).longValue();
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            result.addAll(detectStops(deviceId, from, to));
        }

        return result;
    }


    public static void getExcel(OutputStream outputStream, long userId, Collection<Long> deviceIds, Collection<Long> groupIds, Date from, Date to) throws SQLException, IOException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<DeviceReport> devicesStops = new ArrayList<>();
        ArrayList<String> sheetNames = new ArrayList<>();
        for (Iterator<Long> iterator = ReportUtils.getDeviceList(deviceIds, groupIds).iterator(); iterator.hasNext(); ) {
            long deviceId = ((Long) iterator.next()).longValue();
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            Collection<StopReport> stops = detectStops(deviceId, from, to);
            DeviceReport deviceStops = new DeviceReport();
            Device device = Context.getIdentityManager().getById(deviceId);
            deviceStops.setDeviceName(device.getName());
            sheetNames.add(WorkbookUtil.createSafeSheetName(deviceStops.getDeviceName()));
            if (device.getGroupId() != 0L) {
                Group group = (Group) Context.getGroupsManager().getById(device.getGroupId());
                if (group != null) {
                    deviceStops.setGroupName(group.getName());
                }
            }
            deviceStops.setObjects(stops);
            devicesStops.add(deviceStops);
        }

        String templatePath = Context.getConfig().getString("report.templatesPath", "templates/export/");

        try (InputStream inputStream = new FileInputStream(templatePath + "/stops.xlsx")) {
            org.jxls.common.Context jxlsContext = ReportUtils.initializeContext(userId);
            jxlsContext.putVar("devices", devicesStops);
            jxlsContext.putVar("sheetNames", sheetNames);
            jxlsContext.putVar("from", from);
            jxlsContext.putVar("to", to);
            ReportUtils.processTemplateWithSheets(inputStream, outputStream, jxlsContext);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\Stops.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */