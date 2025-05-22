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
/*    */ public class WondexProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public WondexProtocol() {
/* 28 */     setTextCommandEncoder(new WondexProtocolEncoder());
/* 29 */     setSupportedCommands(new String[] { "getDeviceStatus", "getModemStatus", "rebootDevice", "positionSingle", "getVersion", "deviceIdentification" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 36 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 39 */             pipeline.addLast((ChannelHandler)new WondexFrameDecoder());
/* 40 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 41 */             pipeline.addLast((ChannelHandler)new WondexProtocolEncoder());
/* 42 */             pipeline.addLast((ChannelHandler)new WondexProtocolDecoder((Protocol)WondexProtocol.this));
/*    */           }
/*    */         });
/* 45 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 48 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 49 */             pipeline.addLast((ChannelHandler)new WondexProtocolEncoder());
/* 50 */             pipeline.addLast((ChannelHandler)new WondexProtocolDecoder((Protocol)WondexProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WondexProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */