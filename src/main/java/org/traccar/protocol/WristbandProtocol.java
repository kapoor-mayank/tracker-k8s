/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
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
/*    */ public class WristbandProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public WristbandProtocol() {
/* 26 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 29 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 3, 2, 3, 0));
/* 30 */             pipeline.addLast((ChannelHandler)new WristbandProtocolDecoder((Protocol)WristbandProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WristbandProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */