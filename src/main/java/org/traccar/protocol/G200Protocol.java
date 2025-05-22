/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.PipelineBuilder;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.TrackerServer;
/*    */ 
/*    */ public class G200Protocol extends BaseProtocol {
/*    */   public G200Protocol() {
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
/* 23 */             int messageLength = Context.getConfig().getInteger(G200Protocol.this.getName() + ".messageLength");
/* 24 */             pipeline.addLast((ChannelHandler)new H02FrameDecoder(messageLength));
/* 25 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 26 */             pipeline.addLast((ChannelHandler)new H02ProtocolEncoder());
/* 27 */             pipeline.addLast((ChannelHandler)new H02ProtocolDecoder((Protocol)G200Protocol.this));
/*    */           }
/*    */         });
/* 30 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 33 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 34 */             pipeline.addLast((ChannelHandler)new H02ProtocolEncoder());
/* 35 */             pipeline.addLast((ChannelHandler)new H02ProtocolDecoder((Protocol)G200Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\G200Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */