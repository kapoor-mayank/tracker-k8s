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
/*    */ public class EasyTrackPlusProtocol extends BaseProtocol {
/*    */   public EasyTrackPlusProtocol() {
/* 14 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume", "custom" });
/*    */ 
/*    */ 
/*    */     
/* 18 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 21 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, new String[] { "#", "\r\n" }));
/* 22 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 23 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 24 */             pipeline.addLast((ChannelHandler)new EasyTrackPlusProtocolDecoder((Protocol)EasyTrackPlusProtocol.this));
/* 25 */             pipeline.addLast((ChannelHandler)new EasyTrackPlusProtocolEncoder());
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EasyTrackPlusProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */