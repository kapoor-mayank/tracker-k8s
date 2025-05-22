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
/*    */ public class KhdProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public KhdProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume", "getVersion", "factoryReset", "setSpeedLimit", "setOdometer", "positionSingle" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 36 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 39 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(512, 3, 2));
/* 40 */             pipeline.addLast((ChannelHandler)new KhdProtocolEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new KhdProtocolDecoder((Protocol)KhdProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\KhdProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */