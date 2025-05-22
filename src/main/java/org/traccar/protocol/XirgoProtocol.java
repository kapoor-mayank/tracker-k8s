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
/*    */ 
/*    */ public class XirgoProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public XirgoProtocol() {
/* 30 */     setSupportedDataCommands(new String[] { "outputControl" });
/*    */     
/* 32 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 35 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, "##"));
/* 36 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 37 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 38 */             pipeline.addLast((ChannelHandler)new XirgoProtocolEncoder());
/* 39 */             pipeline.addLast((ChannelHandler)new XirgoProtocolDecoder((Protocol)XirgoProtocol.this));
/*    */           }
/*    */         });
/* 42 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 45 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 46 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 47 */             pipeline.addLast((ChannelHandler)new XirgoProtocolEncoder());
/* 48 */             pipeline.addLast((ChannelHandler)new XirgoProtocolDecoder((Protocol)XirgoProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\XirgoProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */