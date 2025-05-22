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
/*    */ 
/*    */ public class NoranProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public NoranProtocol() {
/* 26 */     setSupportedDataCommands(new String[] { "positionSingle", "positionPeriodic", "positionStop", "engineStop", "engineResume" });
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 32 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 35 */             pipeline.addLast((ChannelHandler)new NoranProtocolEncoder());
/* 36 */             pipeline.addLast((ChannelHandler)new NoranProtocolDecoder((Protocol)NoranProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NoranProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */