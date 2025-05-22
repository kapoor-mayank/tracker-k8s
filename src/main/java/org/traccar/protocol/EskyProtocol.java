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
/*    */ public class EskyProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public EskyProtocol() {
/* 27 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 30 */             pipeline.addLast((ChannelHandler)new EskyFrameDecoder());
/* 31 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 32 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 33 */             pipeline.addLast((ChannelHandler)new EskyProtocolDecoder((Protocol)EskyProtocol.this));
/*    */           }
/*    */         });
/* 36 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 39 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 41 */             pipeline.addLast((ChannelHandler)new EskyProtocolDecoder((Protocol)EskyProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EskyProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */