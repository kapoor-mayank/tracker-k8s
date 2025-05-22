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
/*    */ public class CityeasyProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public CityeasyProtocol() {
/* 27 */     setSupportedDataCommands(new String[] { "positionSingle", "positionPeriodic", "positionStop", "setTimezone" });
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 32 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 35 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1024, 2, 2, -4, 0));
/* 36 */             pipeline.addLast((ChannelHandler)new CityeasyProtocolEncoder());
/* 37 */             pipeline.addLast((ChannelHandler)new CityeasyProtocolDecoder((Protocol)CityeasyProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CityeasyProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */