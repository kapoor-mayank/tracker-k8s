/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.PipelineBuilder;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.TrackerServer;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class RuptelaProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public RuptelaProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "custom", "requestPhoto", "configuration", "getVersion", "firmwareUpdate", "outputControl", "setConnection", "setOdometer" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 36 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 39 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 0, 2, 2, 0));
/* 40 */             pipeline.addLast((ChannelHandler)new RuptelaProtocolEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new RuptelaProtocolDecoder((Protocol)RuptelaProtocol.this, false));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RuptelaProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */