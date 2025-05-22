package org.traccar.database;

import java.util.Set;

public interface ManagableObjects {
  Set<Long> getUserItems(long paramLong);
  
  Set<Long> getManagedItems(long paramLong);
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\ManagableObjects.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */