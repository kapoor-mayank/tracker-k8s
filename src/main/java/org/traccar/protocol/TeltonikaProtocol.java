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
/*    */ public class TeltonikaProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public TeltonikaProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "serial", "custom" });
/*    */ 
/*    */     
/* 29 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new TeltonikaFrameDecoder());
/* 33 */             pipeline.addLast((ChannelHandler)new TeltonikaProtocolEncoder());
/* 34 */             pipeline.addLast((ChannelHandler)new TeltonikaProtocolDecoder((Protocol)TeltonikaProtocol.this, false));
/*    */           }
/*    */         });
/* 37 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 40 */             pipeline.addLast((ChannelHandler)new TeltonikaProtocolEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new TeltonikaProtocolDecoder((Protocol)TeltonikaProtocol.this, true));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TeltonikaProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */