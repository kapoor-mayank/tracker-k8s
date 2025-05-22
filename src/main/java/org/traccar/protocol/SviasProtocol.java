/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.CharacterDelimiterFrameDecoder;
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
/*    */ public class SviasProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public SviasProtocol() {
/* 30 */     setSupportedDataCommands(new String[] { "custom", "positionSingle", "setOdometer", "engineStop", "engineResume", "alarmArm", "alarmDisarm", "alarmRemove" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 39 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 42 */             pipeline.addLast((ChannelHandler)new CharacterDelimiterFrameDecoder(1024, "]"));
/* 43 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 44 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 45 */             pipeline.addLast((ChannelHandler)new SviasProtocolEncoder());
/* 46 */             pipeline.addLast((ChannelHandler)new SviasProtocolDecoder((Protocol)SviasProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SviasProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */