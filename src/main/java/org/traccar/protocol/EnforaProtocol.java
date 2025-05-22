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
/*    */ 
/*    */ public class EnforaProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public EnforaProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "custom", "engineStop", "engineResume" });
/*    */ 
/*    */ 
/*    */     
/* 31 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 34 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 0, 2, -2, 2));
/* 35 */             pipeline.addLast((ChannelHandler)new EnforaProtocolEncoder());
/* 36 */             pipeline.addLast((ChannelHandler)new EnforaProtocolDecoder((Protocol)EnforaProtocol.this));
/*    */           }
/*    */         });
/* 39 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 42 */             pipeline.addLast((ChannelHandler)new EnforaProtocolEncoder());
/* 43 */             pipeline.addLast((ChannelHandler)new EnforaProtocolDecoder((Protocol)EnforaProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EnforaProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */