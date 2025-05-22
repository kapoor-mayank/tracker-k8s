/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
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
/*    */ public class Jt600Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Jt600Protocol() {
/* 27 */     setSupportedDataCommands(new String[] { "engineResume", "engineStop", "setTimezone", "rebootDevice" });
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 32 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 35 */             pipeline.addLast((ChannelHandler)new Jt600FrameDecoder());
/* 36 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 37 */             pipeline.addLast((ChannelHandler)new Jt600ProtocolEncoder());
/* 38 */             pipeline.addLast((ChannelHandler)new Jt600ProtocolDecoder((Protocol)Jt600Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Jt600Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */