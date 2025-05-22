/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/*    */ import java.nio.ByteOrder;
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
/*    */ public class CastelProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public CastelProtocol() {
/* 28 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume" });
/*    */ 
/*    */     
/* 31 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 34 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 2, 2, -4, 0, true));
/* 35 */             pipeline.addLast((ChannelHandler)new CastelProtocolEncoder());
/* 36 */             pipeline.addLast((ChannelHandler)new CastelProtocolDecoder((Protocol)CastelProtocol.this));
/*    */           }
/*    */         });
/* 39 */     addServer(new TrackerServer(true, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 42 */             pipeline.addLast((ChannelHandler)new CastelProtocolEncoder());
/* 43 */             pipeline.addLast((ChannelHandler)new CastelProtocolDecoder((Protocol)CastelProtocol.this));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CastelProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */