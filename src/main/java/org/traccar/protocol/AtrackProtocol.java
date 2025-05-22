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
/*    */ public class AtrackProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public AtrackProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "custom" });
/*    */     
/* 28 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 31 */             pipeline.addLast((ChannelHandler)new AtrackFrameDecoder());
/* 32 */             pipeline.addLast((ChannelHandler)new AtrackProtocolEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new AtrackProtocolDecoder((Protocol)AtrackProtocol.this));
/*    */           }
/*    */         });
/* 36 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 39 */             pipeline.addLast((ChannelHandler)new AtrackProtocolEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new AtrackProtocolDecoder((Protocol)AtrackProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AtrackProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */