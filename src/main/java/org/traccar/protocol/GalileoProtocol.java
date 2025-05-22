/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
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
/*    */ public class GalileoProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public GalileoProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "custom", "outputControl" });
/*    */ 
/*    */     
/* 29 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new GalileoFrameDecoder());
/* 33 */             pipeline.addLast((ChannelHandler)new GalileoProtocolEncoder());
/* 34 */             pipeline.addLast((ChannelHandler)new GalileoProtocolDecoder((Protocol)GalileoProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GalileoProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */