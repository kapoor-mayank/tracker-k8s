/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
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
/*    */ public class CellocatorProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public CellocatorProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "outputControl" });
/*    */     
/* 28 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 31 */             pipeline.addLast((ChannelHandler)new CellocatorFrameDecoder());
/* 32 */             pipeline.addLast((ChannelHandler)new CellocatorProtocolEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new CellocatorProtocolDecoder((Protocol)CellocatorProtocol.this));
/*    */           }
/*    */         });
/* 36 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 39 */             pipeline.addLast((ChannelHandler)new CellocatorProtocolEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new CellocatorProtocolDecoder((Protocol)CellocatorProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CellocatorProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */