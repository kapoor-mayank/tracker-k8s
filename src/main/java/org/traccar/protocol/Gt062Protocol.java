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
/*    */ public class Gt062Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Gt062Protocol() {
/* 26 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume", "custom" });
/*    */ 
/*    */ 
/*    */     
/* 30 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 33 */             pipeline.addLast((ChannelHandler)new Gt06FrameDecoder());
/* 34 */             pipeline.addLast((ChannelHandler)new Gt06ProtocolEncoder());
/* 35 */             pipeline.addLast((ChannelHandler)new Gt06ProtocolDecoder((Protocol)Gt062Protocol.this, true, false));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gt062Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */