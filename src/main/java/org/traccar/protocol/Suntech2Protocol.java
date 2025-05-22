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
/*    */ public class Suntech2Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Suntech2Protocol() {
/* 27 */     setSupportedDataCommands(new String[] { "outputControl", "rebootDevice", "positionSingle", "engineStop", "engineResume", "alarmArm", "alarmDisarm" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 35 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 38 */             pipeline.addLast((ChannelHandler)new SuntechFrameDecoder());
/* 39 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 40 */             pipeline.addLast((ChannelHandler)new SuntechProtocolEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new SuntechProtocolDecoder((Protocol)Suntech2Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Suntech2Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */