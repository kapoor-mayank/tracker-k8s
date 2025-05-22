/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
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
/*    */ public class GatorProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public GatorProtocol() {
/* 26 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 29 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 3, 2));
/* 30 */             pipeline.addLast((ChannelHandler)new GatorProtocolDecoder((Protocol)GatorProtocol.this));
/*    */           }
/*    */         });
/* 33 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 36 */             pipeline.addLast((ChannelHandler)new GatorProtocolDecoder((Protocol)GatorProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GatorProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */