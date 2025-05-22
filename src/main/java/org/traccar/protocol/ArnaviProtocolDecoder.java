/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.Protocol;
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
/*    */ 
/*    */ public class ArnaviProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   private final ArnaviTextProtocolDecoder textProtocolDecoder;
/*    */   private final ArnaviBinaryProtocolDecoder binaryProtocolDecoder;
/*    */   
/*    */   public ArnaviProtocolDecoder(Protocol protocol) {
/* 31 */     super(protocol);
/* 32 */     this.textProtocolDecoder = new ArnaviTextProtocolDecoder(protocol);
/* 33 */     this.binaryProtocolDecoder = new ArnaviBinaryProtocolDecoder(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 40 */     ByteBuf buf = (ByteBuf)msg;
/*    */     
/* 42 */     if (buf.getByte(buf.readerIndex()) == 36) {
/* 43 */       return this.textProtocolDecoder.decode(channel, remoteAddress, msg);
/*    */     }
/* 45 */     return this.binaryProtocolDecoder.decode(channel, remoteAddress, msg);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ArnaviProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */