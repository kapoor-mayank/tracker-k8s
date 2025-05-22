package org.traccar.database;

import org.traccar.model.Device;
import org.traccar.model.Position;

public interface IdentityManager {
  long addUnknownDevice(String paramString);
  
  Device getById(long paramLong);
  
  Device getByUniqueId(String paramString) throws Exception;
  
  Position getLastPosition(long paramLong);
  
  boolean isLatestPosition(Position paramPosition);
  
  boolean lookupAttributeBoolean(long paramLong, String paramString, boolean paramBoolean1, boolean paramBoolean2);
  
  String lookupAttributeString(long paramLong, String paramString1, String paramString2, boolean paramBoolean);
  
  int lookupAttributeInteger(long paramLong, String paramString, int paramInt, boolean paramBoolean);
  
  long lookupAttributeLong(long paramLong1, String paramString, long paramLong2, boolean paramBoolean);
  
  double lookupAttributeDouble(long paramLong, String paramString, double paramDouble, boolean paramBoolean);
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\IdentityManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */