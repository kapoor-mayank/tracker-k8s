/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.ChannelHandler;
/*    */ import io.netty.handler.codec.LineBasedFrameDecoder;
/*    */ import io.netty.handler.codec.string.StringDecoder;
/*    */ import io.netty.handler.codec.string.StringEncoder;
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
/*    */ public class XexunProtocol
/*    */   extends BaseProtocol
/*    */ {
/*    */   public XexunProtocol() {
/* 31 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume" });
/*    */ 
/*    */     
/* 34 */     addServer(new TrackerServer(false, getName())
/*    */         {
/*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
/* 37 */             boolean full = Context.getConfig().getBoolean(XexunProtocol.this.getName() + ".extended");
/* 38 */             if (full) {
/* 39 */               pipeline.addLast((ChannelHandler)new LineBasedFrameDecoder(1024));
/*    */             } else {
/* 41 */               pipeline.addLast((ChannelHandler)new XexunFrameDecoder());
/*    */             } 
/* 43 */             pipeline.addLast((ChannelHandler)new StringEncoder());
/* 44 */             pipeline.addLast((ChannelHandler)new StringDecoder());
/* 45 */             pipeline.addLast((ChannelHandler)new XexunProtocolEncoder());
/* 46 */             pipeline.addLast((ChannelHandler)new XexunProtocolDecoder((Protocol)XexunProtocol.this, full));
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\XexunProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */