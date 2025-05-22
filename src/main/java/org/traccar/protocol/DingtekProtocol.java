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
/*    */ public class DingtekProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public DingtekProtocol() {
/* 27 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 30 */             pipeline.addLast((ChannelHandler)new DingtekFrameDecoder());
/* 31 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 32 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new DingtekProtocolDecoder((Protocol)DingtekProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DingtekProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */