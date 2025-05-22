/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringEncoder;
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
/*    */ 
/*    */ public class Gl200Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Gl200Protocol() {
/* 28 */     setSupportedDataCommands(new String[] { "positionSingle", "engineStop", "engineResume", "deviceIdentification", "rebootDevice" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 34 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 37 */             pipeline.addLast((ChannelHandler)new Gl200FrameDecoder());
/* 38 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 39 */             pipeline.addLast((ChannelHandler)new Gl200ProtocolEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new Gl200ProtocolDecoder((Protocol)Gl200Protocol.this));
/*    */           }
/*    */         });
/* 43 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 46 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 47 */             pipeline.addLast((ChannelHandler)new Gl200ProtocolEncoder());
/* 48 */             pipeline.addLast((ChannelHandler)new Gl200ProtocolDecoder((Protocol)Gl200Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gl200Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */