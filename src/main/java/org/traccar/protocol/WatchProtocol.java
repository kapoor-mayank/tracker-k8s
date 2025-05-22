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
/*    */ public class WatchProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public WatchProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "custom", "positionSingle", "positionPeriodic", "sosNumber", "alarmSos", "alarmBattery", "rebootDevice", "powerOff", "alarmRemove", "silenceTime", "alarmClock", "setPhonebook", "message", "voiceMessage", "setTimezone", "setIndicator" });
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
/*    */     
/* 43 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 46 */             pipeline.addLast((ChannelHandler)new WatchFrameDecoder());
/* 47 */             pipeline.addLast((ChannelHandler)new WatchProtocolEncoder());
/* 48 */             pipeline.addLast((ChannelHandler)new WatchProtocolDecoder((Protocol)WatchProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WatchProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */