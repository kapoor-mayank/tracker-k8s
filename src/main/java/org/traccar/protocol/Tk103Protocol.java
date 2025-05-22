/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
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
/*    */ public class Tk103Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Tk103Protocol() {
/* 29 */     setSupportedDataCommands(new String[] { "custom", "getDeviceStatus", "deviceIdentification", "modeDeepSleep", "modePowerSaving", "alarmSos", "setConnection", "sosNumber", "positionSingle", "positionPeriodic", "positionStop", "getVersion", "powerOff", "rebootDevice", "setOdometer", "engineStop", "engineResume", "outputControl" });
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
/*    */ 
/*    */     
/* 48 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 51 */             pipeline.addLast((ChannelHandler)new Tk103FrameDecoder());
/* 52 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 53 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 54 */             pipeline.addLast((ChannelHandler)new Tk103ProtocolEncoder());
/* 55 */             pipeline.addLast((ChannelHandler)new Tk103ProtocolDecoder((Protocol)Tk103Protocol.this));
/*    */           }
/*    */         });
/* 58 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 61 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 62 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 63 */             pipeline.addLast((ChannelHandler)new Tk103ProtocolEncoder());
/* 64 */             pipeline.addLast((ChannelHandler)new Tk103ProtocolDecoder((Protocol)Tk103Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tk103Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */