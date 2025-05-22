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
/*    */ public class TaipProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public TaipProtocol() {
/* 28 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 31 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, '<'));
/* 32 */             pipeline.addLast((ChannelHandler)new TaipPrefixEncoder((Protocol)TaipProtocol.this));
/* 33 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 34 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 35 */             pipeline.addLast((ChannelHandler)new TaipProtocolDecoder((Protocol)TaipProtocol.this));
/*    */           }
/*    */         });
/* 38 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 41 */             pipeline.addLast((ChannelHandler)new TaipPrefixEncoder((Protocol)TaipProtocol.this));
/* 42 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 43 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 44 */             pipeline.addLast((ChannelHandler)new TaipProtocolDecoder((Protocol)TaipProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TaipProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */