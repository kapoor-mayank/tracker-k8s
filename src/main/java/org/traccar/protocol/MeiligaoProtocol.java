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
/*    */ public class MeiligaoProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public MeiligaoProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "positionSingle", "positionPeriodic", "engineStop", "engineResume", "movementAlarm", "setTimezone", "requestPhoto", "rebootDevice" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 35 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 38 */             pipeline.addLast((ChannelHandler)new MeiligaoFrameDecoder());
/* 39 */             pipeline.addLast((ChannelHandler)new MeiligaoProtocolEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new MeiligaoProtocolDecoder((Protocol)MeiligaoProtocol.this));
/*    */           }
/*    */         });
/* 43 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 46 */             pipeline.addLast((ChannelHandler)new MeiligaoProtocolEncoder());
/* 47 */             pipeline.addLast((ChannelHandler)new MeiligaoProtocolDecoder((Protocol)MeiligaoProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeiligaoProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */