/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LineBasedFrameDecoder;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ public class FlexApiProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public FlexApiProtocol() {
/* 29 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new LineBasedFrameDecoder(5120));
/* 33 */             pipeline.addLast((ChannelHandler)new StringDecoder(StandardCharsets.US_ASCII));
/* 34 */             pipeline.addLast((ChannelHandler)new FlexApiProtocolDecoder((Protocol)FlexApiProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FlexApiProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */