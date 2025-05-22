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
/*    */ public class PstProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public PstProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume" });
/*    */ 
/*    */     
/* 29 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new PstProtocolEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new PstProtocolDecoder((Protocol)PstProtocol.this));
/*    */           }
/*    */         });
/* 36 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 39 */             pipeline.addLast((ChannelHandler)new PstFrameEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new PstFrameDecoder());
/* 41 */             pipeline.addLast((ChannelHandler)new PstProtocolEncoder());
/* 42 */             pipeline.addLast((ChannelHandler)new PstProtocolDecoder((Protocol)PstProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PstProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */