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
/*    */ public class Pt502Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Pt502Protocol() {
/* 27 */     setSupportedDataCommands(new String[] { "custom", "setTimezone", "alarmSpeed", "outputControl", "requestPhoto" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 33 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 36 */             pipeline.addLast((ChannelHandler)new Pt502FrameDecoder());
/* 37 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 38 */             pipeline.addLast((ChannelHandler)new Pt502ProtocolEncoder());
/* 39 */             pipeline.addLast((ChannelHandler)new Pt502ProtocolDecoder((Protocol)Pt502Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Pt502Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */