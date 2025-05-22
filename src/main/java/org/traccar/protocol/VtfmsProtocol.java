/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringDecoder;
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
/*    */ public class VtfmsProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public VtfmsProtocol() {
/* 27 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 30 */             pipeline.addLast((ChannelHandler)new VtfmsFrameDecoder());
/* 31 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 32 */             pipeline.addLast((ChannelHandler)new VtfmsProtocolDecoder((Protocol)VtfmsProtocol.this));
/*    */           }
/*    */         });
/* 35 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 38 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 39 */             pipeline.addLast((ChannelHandler)new VtfmsProtocolDecoder((Protocol)VtfmsProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\VtfmsProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */