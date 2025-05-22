/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.FixedLengthFrameDecoder;
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
/*    */ public class PricolProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public PricolProtocol() {
/* 26 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 29 */             pipeline.addLast((ChannelHandler)new FixedLengthFrameDecoder(64));
/* 30 */             pipeline.addLast((ChannelHandler)new PricolProtocolDecoder((Protocol)PricolProtocol.this));
/*    */           }
/*    */         });
/* 33 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 36 */             pipeline.addLast((ChannelHandler)new PricolProtocolDecoder((Protocol)PricolProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PricolProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */