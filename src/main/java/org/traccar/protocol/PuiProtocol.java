/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.mqtt.MqttDecoder;
/*    */ import io.netty.handler.codec.mqtt.MqttEncoder;
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
/*    */ public class PuiProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public PuiProtocol() {
/* 27 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 30 */             pipeline.addLast((ChannelHandler)MqttEncoder.INSTANCE);
/* 31 */             pipeline.addLast((ChannelHandler)new MqttDecoder());
/* 32 */             pipeline.addLast((ChannelHandler)new PuiProtocolDecoder((Protocol)PuiProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PuiProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */