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
/*    */ 
/*    */ public class Gl200ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   private final Gl200TextProtocolDecoder textProtocolDecoder;
/*    */   private final Gl200BinaryProtocolDecoder binaryProtocolDecoder;
/*    */   
/*    */   public Gl200ProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/* 33 */     this.textProtocolDecoder = new Gl200TextProtocolDecoder(protocol);
/* 34 */     this.binaryProtocolDecoder = new Gl200BinaryProtocolDecoder(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 41 */     ByteBuf buf = (ByteBuf)msg;
/*    */     
/* 43 */     if (Gl200FrameDecoder.isBinary(buf)) {
/* 44 */       return this.binaryProtocolDecoder.decode(channel, remoteAddress, msg);
/*    */     }
/* 46 */     return this.textProtocolDecoder.decode(channel, remoteAddress, msg);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gl200ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */