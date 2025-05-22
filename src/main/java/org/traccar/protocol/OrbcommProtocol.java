/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.http.HttpObjectAggregator;
/*    */ import io.netty.handler.codec.http.HttpRequestEncoder;
/*    */ import io.netty.handler.codec.http.HttpResponseDecoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.PipelineBuilder;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.TrackerClient;
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
/*    */ public class OrbcommProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public OrbcommProtocol() {
/* 28 */     addClient(new TrackerClient(getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 31 */             pipeline.addLast((ChannelHandler)new HttpRequestEncoder());
/* 32 */             pipeline.addLast((ChannelHandler)new HttpResponseDecoder());
/* 33 */             pipeline.addLast((ChannelHandler)new HttpObjectAggregator(65535));
/* 34 */             pipeline.addLast((ChannelHandler)new OrbcommProtocolDecoder((Protocol)OrbcommProtocol.this));
/* 35 */             pipeline.addLast((ChannelHandler)new OrbcommProtocolPoller((Protocol)OrbcommProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OrbcommProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */