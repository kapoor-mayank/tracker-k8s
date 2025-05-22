/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import io.netty.handler.codec.MessageToByteEncoder;
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
/*    */ public class PstFrameEncoder
/*    */   extends MessageToByteEncoder<ByteBuf>
/*    */ {
/*    */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
/* 27 */     out.writeByte(40);
/* 28 */     while (msg.isReadable()) {
/* 29 */       int b = msg.readUnsignedByte();
/* 30 */       if (b == 39 || b == 40 || b == 41) {
/* 31 */         out.writeByte(39);
/* 32 */         out.writeByte(b ^ 0x40); continue;
/*    */       } 
/* 34 */       out.writeByte(b);
/*    */     } 
/*    */     
/* 37 */     out.writeByte(41);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PstFrameEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */