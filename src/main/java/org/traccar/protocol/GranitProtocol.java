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
/*    */ 
/*    */ public class GranitProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public GranitProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "deviceIdentification", "rebootDevice", "positionSingle" });
/*    */ 
/*    */ 
/*    */     
/* 31 */     setTextCommandEncoder(new GranitProtocolSmsEncoder());
/* 32 */     setSupportedTextCommands(new String[] { "rebootDevice", "positionPeriodic" });
/*    */ 
/*    */     
/* 35 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 38 */             pipeline.addLast((ChannelHandler)new GranitFrameDecoder());
/* 39 */             pipeline.addLast((ChannelHandler)new GranitProtocolEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new GranitProtocolDecoder((Protocol)GranitProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GranitProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */