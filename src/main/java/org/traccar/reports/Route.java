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
import org.traccar.model.Device;
import org.traccar.model.Group;
import org.traccar.model.Position;
import org.traccar.reports.model.DeviceReport;


public final class Route {
    public static Collection<Position> getObjects(long userId, Collection<Long> deviceIds, Collection<Long> groupIds, Date from, Date to) throws SQLException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<Position> result = new ArrayList<>();
        for (Iterator<Long> iterator = ReportUtils.getDeviceList(deviceIds, groupIds).iterator(); iterator.hasNext(); ) {
            long deviceId = ((Long) iterator.next()).longValue();
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            result.addAll(Context.getDataManager().getPositions(deviceId, from, to));
        }

        return result;
    }


    public static void getExcel(OutputStream outputStream, long userId, Collection<Long> deviceIds, Collection<Long> groupIds, Date from, Date to) throws SQLException, IOException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<DeviceReport> devicesRoutes = new ArrayList<>();
        ArrayList<String> sheetNames = new ArrayList<>();
        for (Iterator<Long> iterator = ReportUtils.getDeviceList(deviceIds, groupIds).iterator(); iterator.hasNext(); ) {
            long deviceId = ((Long) iterator.next()).longValue();
            Context.getPermissionsManager().checkDevice(userId, deviceId);

            Collection<Position> positions = Context.getDataManager().getPositions(deviceId, from, to);
            DeviceReport deviceRoutes = new DeviceReport();
            Device device = Context.getIdentityManager().getById(deviceId);
            deviceRoutes.setDeviceName(device.getName());
            sheetNames.add(WorkbookUtil.createSafeSheetName(deviceRoutes.getDeviceName()));
            if (device.getGroupId() != 0L) {
                Group group = (Group) Context.getGroupsManager().getById(device.getGroupId());
                if (group != null) {
                    deviceRoutes.setGroupName(group.getName());
                }
            }
            deviceRoutes.setObjects(positions);
            devicesRoutes.add(deviceRoutes);
        }

        String templatePath = Context.getConfig().getString("report.templatesPath", "templates/export/");

        try (InputStream inputStream = new FileInputStream(templatePath + "/route.xlsx")) {
            org.jxls.common.Context jxlsContext = ReportUtils.initializeContext(userId);
            jxlsContext.putVar("devices", devicesRoutes);
            jxlsContext.putVar("sheetNames", sheetNames);
            jxlsContext.putVar("from", from);
            jxlsContext.putVar("to", to);
            ReportUtils.processTemplateWithSheets(inputStream, outputStream, jxlsContext);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\Route.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */