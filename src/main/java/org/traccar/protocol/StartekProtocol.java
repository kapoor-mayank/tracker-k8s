/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringDecoder;
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
/*    */ public class StartekProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public StartekProtocol() {
/* 28 */     setSupportedDataCommands(new String[] { "custom", "outputControl", "engineStop", "engineResume" });
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 33 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 36 */             pipeline.addLast((ChannelHandler)new StartekFrameDecoder());
/* 37 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 38 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 39 */             pipeline.addLast((ChannelHandler)new StartekProtocolEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new StartekProtocolDecoder((Protocol)StartekProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\StartekProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */