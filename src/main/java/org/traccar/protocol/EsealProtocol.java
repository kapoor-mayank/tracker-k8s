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
/*    */ public class EsealProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public EsealProtocol() {
/* 29 */     setSupportedDataCommands(new String[] { "custom", "alarmArm", "alarmDisarm" });
/*    */ 
/*    */ 
/*    */     
/* 33 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 36 */             pipeline.addLast((ChannelHandler)new LineBasedFrameDecoder(1024));
/* 37 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 38 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 39 */             pipeline.addLast((ChannelHandler)new EsealProtocolEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new EsealProtocolDecoder((Protocol)EsealProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EsealProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */