/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LineBasedFrameDecoder;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.BaseProtocol;
/*    */ import org.traccar.Context;
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
/*    */ public class WialonProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public WialonProtocol() {
/* 32 */     setSupportedDataCommands(new String[] { "rebootDevice", "sendUssd", "deviceIdentification", "outputControl" });
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 37 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 40 */             pipeline.addLast((ChannelHandler)new LineBasedFrameDecoder(4096));
/* 41 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 42 */             boolean utf8 = Context.getConfig().getBoolean(WialonProtocol.this.getName() + ".utf8");
/* 43 */             if (utf8) {
/* 44 */               pipeline.addLast((ChannelHandler)new StringDecoder(StandardCharsets.UTF_8));
/*    */             } else {
/* 46 */               pipeline.addLast((ChannelHandler)new StringDecoder());
/*    */             } 
/* 48 */             pipeline.addLast((ChannelHandler)new WialonProtocolEncoder());
/* 49 */             pipeline.addLast((ChannelHandler)new WialonProtocolDecoder((Protocol)WialonProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WialonProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */