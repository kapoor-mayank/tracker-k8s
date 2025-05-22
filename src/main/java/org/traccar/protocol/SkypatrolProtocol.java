/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
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
/*    */ public class SkypatrolProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public SkypatrolProtocol() {
/* 25 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 28 */             pipeline.addLast((ChannelHandler)new SkypatrolProtocolDecoder((Protocol)SkypatrolProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SkypatrolProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */