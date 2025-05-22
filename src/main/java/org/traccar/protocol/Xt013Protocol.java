/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LineBasedFrameDecoder;
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
/*    */ public class Xt013Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Xt013Protocol() {
/* 29 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new LineBasedFrameDecoder(1024));
/* 33 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 34 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 35 */             pipeline.addLast((ChannelHandler)new Xt013ProtocolDecoder((Protocol)Xt013Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xt013Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */