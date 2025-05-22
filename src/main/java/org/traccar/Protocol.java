package org.traccar;

import java.util.Collection;
import org.traccar.database.ActiveDevice;
import org.traccar.model.Command;

public interface Protocol {
  String getName();
  
  Collection<TrackerConnector> getConnectorList();
  
  Collection<String> getSupportedDataCommands();
  
  void sendDataCommand(ActiveDevice paramActiveDevice, Command paramCommand);
  
  Collection<String> getSupportedTextCommands();
  
  void sendTextCommand(String paramString, Command paramCommand) throws Exception;
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */