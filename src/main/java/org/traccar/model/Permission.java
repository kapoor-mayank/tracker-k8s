package org.traccar.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.traccar.database.DataManager;


public class Permission {
    private Class<?> ownerClass;
    private long ownerId;
    private Class<?> propertyClass;
    private long propertyId;

    public Permission(LinkedHashMap<String, Long> permissionMap) throws ClassNotFoundException {
        Iterator<Map.Entry<String, Long>> iterator = permissionMap.entrySet().iterator();
        String owner = (String) ((Map.Entry) iterator.next()).getKey();
        this.ownerClass = DataManager.getClassByName(owner);
        String property = (String) ((Map.Entry) iterator.next()).getKey();
        this.propertyClass = DataManager.getClassByName(property);
        this.ownerId = ((Long) permissionMap.get(owner)).longValue();
        this.propertyId = ((Long) permissionMap.get(property)).longValue();
    }

    public Class<?> getOwnerClass() {
        return this.ownerClass;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public Class<?> getPropertyClass() {
        return this.propertyClass;
    }

    public long getPropertyId() {
        return this.propertyId;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Permission.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */