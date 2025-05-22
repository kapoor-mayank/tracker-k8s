/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LineBasedFrameDecoder;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ public class Xrb28Protocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public Xrb28Protocol() {
/* 31 */     setSupportedDataCommands(new String[] { "custom", "positionSingle", "positionPeriodic", "alarmArm", "alarmDisarm" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 37 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 40 */             pipeline.addLast((ChannelHandler)new LineBasedFrameDecoder(1024));
/* 41 */             pipeline.addLast((ChannelHandler)new StringEncoder(StandardCharsets.ISO_8859_1));
/* 42 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 43 */             pipeline.addLast((ChannelHandler)new Xrb28ProtocolEncoder());
/* 44 */             pipeline.addLast((ChannelHandler)new Xrb28ProtocolDecoder((Protocol)Xrb28Protocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xrb28Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */