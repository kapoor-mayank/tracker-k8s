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
/*    */ public class Pt215Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Pt215Protocol() {
/* 26 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 29 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 2, 1, -1, 0));
/* 30 */             pipeline.addLast((ChannelHandler)new Pt215ProtocolDecoder((Protocol)Pt215Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Pt215Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */