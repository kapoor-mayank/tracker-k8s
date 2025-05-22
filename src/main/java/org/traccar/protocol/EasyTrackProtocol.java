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
/*    */ public class EasyTrackProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public EasyTrackProtocol() {
/* 29 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume", "alarmArm", "alarmDisarm" });
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 34 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 37 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, new String[] { "#\r\n", "#", "\r\n" }));
/* 38 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 39 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new EasyTrackProtocolEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new EasyTrackProtocolDecoder((Protocol)EasyTrackProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EasyTrackProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */