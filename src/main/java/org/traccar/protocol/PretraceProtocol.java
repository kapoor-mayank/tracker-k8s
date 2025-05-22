/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.CharacterDelimiterFrameDecoder;
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
/*    */ public class PretraceProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public PretraceProtocol() {
/* 29 */     setSupportedDataCommands(new String[] { "custom", "positionPeriodic" });
/*    */ 
/*    */     
/* 32 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 35 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, ')'));
/* 36 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 37 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 38 */             pipeline.addLast((ChannelHandler)new PretraceProtocolEncoder());
/* 39 */             pipeline.addLast((ChannelHandler)new PretraceProtocolDecoder((Protocol)PretraceProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PretraceProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */