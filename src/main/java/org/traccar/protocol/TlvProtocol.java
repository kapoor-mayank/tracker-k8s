/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
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
/*    */ public class TlvProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public TlvProtocol() {
/* 26 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 29 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, false));
/* 30 */             pipeline.addLast((ChannelHandler)new TlvProtocolDecoder((Protocol)TlvProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TlvProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */