package org.traccar.sms;

import org.traccar.notification.MessageException;

public interface SmsManager {
  void sendMessageSync(String paramString1, String paramString2, boolean paramBoolean) throws InterruptedException, MessageException;
  
  void sendMessageAsync(String paramString1, String paramString2, boolean paramBoolean);
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\sms\SmsManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */