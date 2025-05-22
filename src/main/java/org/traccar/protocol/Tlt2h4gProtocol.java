/*    */ package org.traccar.protocol;
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.CharacterDelimiterFrameDecoder;
/*    */ import org.traccar.PipelineBuilder;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.TrackerServer;
/*    */ 
/*    */ public class Tlt2h4gProtocol extends BaseProtocol {
/*    */   public Tlt2h4gProtocol() {
/* 13 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 16 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(32768, "##\r\n"));
/* 17 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 18 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 19 */             pipeline.addLast((ChannelHandler)new Tlt2hProtocolDecoder((Protocol)Tlt2h4gProtocol.this, true));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tlt2h4gProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */