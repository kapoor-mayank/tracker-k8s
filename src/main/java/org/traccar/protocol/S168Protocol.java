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
/*    */ public class S168Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public S168Protocol() {
/* 28 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 31 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, '$'));
/* 32 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 34 */             pipeline.addLast((ChannelHandler)new S168ProtocolDecoder((Protocol)S168Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\S168Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */