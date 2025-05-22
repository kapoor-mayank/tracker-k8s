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
/*    */ public class VisiontekProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public VisiontekProtocol() {
/* 28 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 31 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, '#'));
/* 32 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 33 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 34 */             pipeline.addLast((ChannelHandler)new VisiontekProtocolDecoder((Protocol)VisiontekProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\VisiontekProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */