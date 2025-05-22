package org.traccar.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.traccar.model.BaseModel;
import org.traccar.model.Driver;


public class DriversManager
        extends ExtendedObjectManager<Driver> {
    private Map<String, Driver> driversByUniqueId;

    public DriversManager(DataManager dataManager) {
        super(dataManager, Driver.class);
        if (this.driversByUniqueId == null) {
            this.driversByUniqueId = new ConcurrentHashMap<>();
        }
    }

    private void putUniqueDriverId(Driver driver) {
        if (this.driversByUniqueId == null) {
            this.driversByUniqueId = new ConcurrentHashMap<>(getAllItems().size());
        }
        this.driversByUniqueId.put(driver.getUniqueId(), driver);
    }


    protected void addNewItem(Driver driver) {
        super.addNewItem(driver);
        putUniqueDriverId(driver);
    }


    protected void updateCachedItem(Driver driver) {
        Driver cachedDriver = getById(driver.getId());
        cachedDriver.setName(driver.getName());
        if (!driver.getUniqueId().equals(cachedDriver.getUniqueId())) {
            this.driversByUniqueId.remove(cachedDriver.getUniqueId());
            cachedDriver.setUniqueId(driver.getUniqueId());
            putUniqueDriverId(cachedDriver);
        }
        cachedDriver.setAttributes(driver.getAttributes());
    }


    protected void removeCachedItem(long driverId) {
        Driver cachedDriver = getById(driverId);
        if (cachedDriver != null) {
            String driverUniqueId = cachedDriver.getUniqueId();
            super.removeCachedItem(driverId);
            this.driversByUniqueId.remove(driverUniqueId);
        }
    }

    public Driver getDriverByUniqueId(String uniqueId) {
        return this.driversByUniqueId.get(uniqueId);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\DriversManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */