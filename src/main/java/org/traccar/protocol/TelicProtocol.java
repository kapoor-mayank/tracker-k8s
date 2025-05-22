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
/*    */ public class TelicProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public TelicProtocol() {
/* 27 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 30 */             pipeline.addLast((ChannelHandler)new TelicFrameDecoder());
/* 31 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 32 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new TelicProtocolDecoder((Protocol)TelicProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TelicProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */