/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.PipelineBuilder;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.TrackerServer;
/*    */ 
/*    */ public class IcarProtocol
/*    */   extends BaseProtocol {
/*    */   public IcarProtocol() {
/* 13 */     setSupportedDataCommands(new String[] { "alarmArm", "alarmDisarm", "engineStop", "engineResume", "positionPeriodic" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 20 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 23 */             pipeline.addLast((ChannelHandler)new H02FrameDecoder(47));
/* 24 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 25 */             pipeline.addLast((ChannelHandler)new H02ProtocolEncoder());
/* 26 */             pipeline.addLast((ChannelHandler)new H02ProtocolDecoder((Protocol)IcarProtocol.this));
/*    */           }
/*    */         });
/* 29 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 32 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 33 */             pipeline.addLast((ChannelHandler)new H02ProtocolEncoder());
/* 34 */             pipeline.addLast((ChannelHandler)new H02ProtocolDecoder((Protocol)IcarProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\IcarProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */