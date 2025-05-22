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
/*    */ public class MeitrackProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public MeitrackProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "positionLog", "positionSingle", "engineStop", "engineResume", "alarmArm", "alarmDisarm", "requestPhoto", "sendSms" });
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
/* 39 */             pipeline.addLast((ChannelHandler)new MeitrackFrameDecoder());
/* 40 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new MeitrackProtocolEncoder());
/* 42 */             pipeline.addLast((ChannelHandler)new MeitrackProtocolDecoder((Protocol)MeitrackProtocol.this));
/*    */           }
/*    */         });
/* 45 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 48 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 49 */             pipeline.addLast((ChannelHandler)new MeitrackProtocolEncoder());
/* 50 */             pipeline.addLast((ChannelHandler)new MeitrackProtocolDecoder((Protocol)MeitrackProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeitrackProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */