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
/*    */ public class HomtecsProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public HomtecsProtocol() {
/* 27 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 30 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 31 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 32 */             pipeline.addLast((ChannelHandler)new HomtecsProtocolDecoder((Protocol)HomtecsProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HomtecsProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */