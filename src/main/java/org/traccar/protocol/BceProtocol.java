//  package org.traccar.protocol;
// 
//  import io.netty.channel.ChannelHandler;
//  import org.traccar.BaseProtocol;
//  import org.traccar.PipelineBuilder;
//  import org.traccar.Protocol;
//  import org.traccar.TrackerServer;
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
//  public class BceProtocol
//    extends BaseProtocol
//  {
//    public BceProtocol() {
//      setSupportedDataCommands(new String[] { "outputControl" });
// 
//      addServer(new TrackerServer(false, getName())
//          {
//            protected void addProtocolHandlers(PipelineBuilder pipeline) {
//              pipeline.addLast((ChannelHandler)new BceFrameDecoder());
//              pipeline.addLast((ChannelHandler)new BceProtocolEncoder());
//              pipeline.addLast((ChannelHandler)new BceProtocolDecoder((Protocol)BceProtocol.this));
//            }
//          });
//    }
//  }
//
//
/// * Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BceProtocol.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */