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
/*    */ public class EelinkProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public EelinkProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "custom", "positionSingle", "engineStop", "engineResume", "rebootDevice" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 33 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 36 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 3, 2));
/* 37 */             pipeline.addLast((ChannelHandler)new EelinkProtocolEncoder(false));
/* 38 */             pipeline.addLast((ChannelHandler)new EelinkProtocolDecoder((Protocol)EelinkProtocol.this));
/*    */           }
/*    */         });
/* 41 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 44 */             pipeline.addLast((ChannelHandler)new EelinkProtocolEncoder(true));
/* 45 */             pipeline.addLast((ChannelHandler)new EelinkProtocolDecoder((Protocol)EelinkProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EelinkProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */