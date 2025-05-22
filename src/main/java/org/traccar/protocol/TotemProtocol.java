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
/*    */ public class TotemProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public TotemProtocol() {
/* 28 */     setSupportedDataCommands(new String[] { "engineResume", "engineStop" });
/*    */ 
/*    */ 
/*    */     
/* 32 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 35 */             pipeline.addLast((ChannelHandler)new TotemFrameDecoder());
/* 36 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 37 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 38 */             pipeline.addLast((ChannelHandler)new TotemProtocolEncoder());
/* 39 */             pipeline.addLast((ChannelHandler)new TotemProtocolDecoder((Protocol)TotemProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TotemProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */