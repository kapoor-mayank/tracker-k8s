package org.traccar.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.model.Geofence;
import org.traccar.model.Position;


public class GeofenceManager
        extends ExtendedObjectManager<Geofence> {
    public GeofenceManager(DataManager dataManager) {
        super(dataManager, Geofence.class);
    }


    public final void refreshExtendedPermissions() {
        super.refreshExtendedPermissions();
        recalculateDevicesGeofences();
    }

    public List<Long> getCurrentDeviceGeofences(Position position) {
        List<Long> result = new ArrayList<>();
        for (Iterator<Long> iterator = getAllDeviceItems(position.getDeviceId()).iterator(); iterator.hasNext(); ) {
            long geofenceId = ((Long) iterator.next()).longValue();
            Geofence geofence = getById(geofenceId);
            if (geofence != null && geofence.getGeometry()
                    .containsPoint(position.getLatitude(), position.getLongitude())) {
                result.add(Long.valueOf(geofenceId));
            }
        }

        return result;
    }

    public void recalculateDevicesGeofences() {
        for (Device device : Context.getDeviceManager().getAllDevices()) {
            List<Long> deviceGeofenceIds = device.getGeofenceIds();
            if (deviceGeofenceIds == null) {
                deviceGeofenceIds = new ArrayList<>();
            } else {
                deviceGeofenceIds.clear();
            }
            Position lastPosition = Context.getIdentityManager().getLastPosition(device.getId());
            if (lastPosition != null && getAllDeviceItems(device.getId()) != null) {
                deviceGeofenceIds.addAll(getCurrentDeviceGeofences(lastPosition));
            }
            device.setGeofenceIds(deviceGeofenceIds);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\GeofenceManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */