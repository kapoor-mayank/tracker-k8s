package org.traccar;

import io.netty.channel.group.ChannelGroup;

public interface TrackerConnector {
  boolean isDatagram();
  
  boolean isSecure();
  
  ChannelGroup getChannelGroup();
  
  void start() throws Exception;
  
  void stop();
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\TrackerConnector.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */