/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.http.HttpObjectAggregator;
/*    */ import io.netty.handler.codec.http.HttpRequestDecoder;
/*    */ import io.netty.handler.codec.http.HttpResponseEncoder;
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
/*    */ public class PiligrimProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public PiligrimProtocol() {
/* 28 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 31 */             pipeline.addLast((ChannelHandler)new HttpResponseEncoder());
/* 32 */             pipeline.addLast((ChannelHandler)new HttpRequestDecoder());
/* 33 */             pipeline.addLast((ChannelHandler)new HttpObjectAggregator(16384));
/* 34 */             pipeline.addLast((ChannelHandler)new PiligrimProtocolDecoder((Protocol)PiligrimProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PiligrimProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */