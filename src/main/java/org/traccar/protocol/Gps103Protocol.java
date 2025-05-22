/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.CharacterDelimiterFrameDecoder;
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
/*    */ public class Gps103Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Gps103Protocol() {
/* 29 */     setSupportedDataCommands(new String[] { "custom", "positionSingle", "positionPeriodic", "positionStop", "engineStop", "engineResume", "customEngineStop", "customEngineResume", "alarmArm", "alarmDisarm", "requestPhoto" });
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
/* 41 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 44 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(2048, false, new String[] { "\r\n", "\n", ";", "*" }));
/* 45 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 46 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 47 */             pipeline.addLast((ChannelHandler)new Gps103ProtocolEncoder());
/* 48 */             pipeline.addLast((ChannelHandler)new Gps103ProtocolDecoder((Protocol)Gps103Protocol.this));
/*    */           }
/*    */         });
/* 51 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 54 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 55 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 56 */             pipeline.addLast((ChannelHandler)new Gps103ProtocolEncoder());
/* 57 */             pipeline.addLast((ChannelHandler)new Gps103ProtocolDecoder((Protocol)Gps103Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gps103Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */