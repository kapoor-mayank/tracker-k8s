/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.http.HttpObjectAggregator;
/*    */ import io.netty.handler.codec.http.HttpRequestDecoder;
/*    */ import io.netty.handler.codec.http.HttpResponseEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.Context;
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
/*    */ public class Mta6Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Mta6Protocol() {
/* 29 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new HttpResponseEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new HttpRequestDecoder());
/* 34 */             pipeline.addLast((ChannelHandler)new HttpObjectAggregator(65535));
/* 35 */             pipeline.addLast((ChannelHandler)new Mta6ProtocolDecoder((Protocol)Mta6Protocol.this, 
/* 36 */                   !Context.getConfig().getBoolean(Mta6Protocol.this.getName() + ".can")));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Mta6Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */