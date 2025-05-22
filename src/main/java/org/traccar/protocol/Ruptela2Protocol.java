/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.PipelineBuilder;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.TrackerServer;
/*    */ 
/*    */ public class Ruptela2Protocol extends BaseProtocol {
/*    */   public Ruptela2Protocol() {
/* 12 */     setSupportedDataCommands(new String[] { "custom", "requestPhoto", "configuration", "getVersion", "firmwareUpdate", "outputControl", "setConnection", "setOdometer" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 21 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 24 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 0, 2, 2, 0));
/* 25 */             pipeline.addLast((ChannelHandler)new RuptelaProtocolEncoder());
/* 26 */             pipeline.addLast((ChannelHandler)new RuptelaProtocolDecoder((Protocol)Ruptela2Protocol.this, true));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Ruptela2Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */