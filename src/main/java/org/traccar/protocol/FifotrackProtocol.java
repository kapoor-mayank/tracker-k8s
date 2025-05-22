/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
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
/*    */ public class FifotrackProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public FifotrackProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "custom", "requestPhoto" });
/*    */ 
/*    */     
/* 30 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 33 */             pipeline.addLast((ChannelHandler)new FifotrackFrameDecoder());
/* 34 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 35 */             pipeline.addLast((ChannelHandler)new FifotrackProtocolEncoder());
/* 36 */             pipeline.addLast((ChannelHandler)new FifotrackProtocolDecoder((Protocol)FifotrackProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FifotrackProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */