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
/*    */ public class Sprint3XProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Sprint3XProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume", "custom" });
/*    */ 
/*    */ 
/*    */     
/* 30 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 33 */             pipeline.addLast((ChannelHandler)new Gt06FrameDecoder());
/* 34 */             pipeline.addLast((ChannelHandler)new Gt06ProtocolEncoder());
/* 35 */             pipeline.addLast((ChannelHandler)new Gt06ProtocolDecoder((Protocol)Sprint3XProtocol.this, false, true));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Sprint3XProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */