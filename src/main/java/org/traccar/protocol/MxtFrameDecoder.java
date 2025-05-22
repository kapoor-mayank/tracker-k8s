/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import org.traccar.BaseFrameDecoder;
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
/*    */ 
/*    */ public class MxtFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     if (buf.readableBytes() < 2) {
/* 31 */       return null;
/*    */     }
/*    */     
/* 34 */     int index = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)4);
/* 35 */     if (index != -1) {
/* 36 */       ByteBuf result = Unpooled.buffer(index + 1 - buf.readerIndex());
/*    */       
/* 38 */       while (buf.readerIndex() <= index) {
/* 39 */         int b = buf.readUnsignedByte();
/* 40 */         if (b == 16) {
/* 41 */           result.writeByte(buf.readUnsignedByte() - 32); continue;
/*    */         } 
/* 43 */         result.writeByte(b);
/*    */       } 
/*    */ 
/*    */       
/* 47 */       return result;
/*    */     } 
/*    */     
/* 50 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MxtFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */