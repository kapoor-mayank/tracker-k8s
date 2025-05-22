/*    */ package org.traccar.protocol;
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.http.HttpObjectAggregator;
/*    */ import io.netty.handler.codec.http.HttpRequestDecoder;
/*    */ import io.netty.handler.codec.http.HttpResponseEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.PipelineBuilder;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.TrackerServer;
/*    */ 
/*    */ public class InsertProtocol extends BaseProtocol {
/*    */   public InsertProtocol() {
/* 13 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 16 */             pipeline.addLast((ChannelHandler)new HttpResponseEncoder());
/* 17 */             pipeline.addLast((ChannelHandler)new HttpRequestDecoder());
/* 18 */             pipeline.addLast((ChannelHandler)new HttpObjectAggregator(16384));
/* 19 */             pipeline.addLast((ChannelHandler)new OsmAndProtocolDecoder((Protocol)InsertProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\InsertProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */