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
/*    */ 
/*    */ public class T800xProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public T800xProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "custom" });
/*    */     
/* 29 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 3, 2, -5, 0));
/* 33 */             pipeline.addLast((ChannelHandler)new T800xProtocolEncoder());
/* 34 */             pipeline.addLast((ChannelHandler)new T800xProtocolDecoder((Protocol)T800xProtocol.this));
/*    */           }
/*    */         });
/* 37 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 40 */             pipeline.addLast((ChannelHandler)new T800xProtocolEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new T800xProtocolDecoder((Protocol)T800xProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\T800xProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */