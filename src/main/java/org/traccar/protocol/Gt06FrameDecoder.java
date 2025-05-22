/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ 
/*    */ public class Gt06FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 5) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     String prefix = buf.getCharSequence(buf.readerIndex(), 3, StandardCharsets.US_ASCII).toString();
/* 36 */     if (prefix.equals("QZE")) {
/* 37 */       buf.skipBytes(3);
/*    */     }
/*    */     
/* 40 */     int length = 4;
/*    */     
/* 42 */     if (buf.getByte(buf.readerIndex()) == 120) {
/* 43 */       length += 1 + buf.getUnsignedByte(buf.readerIndex() + 2);
/*    */     } else {
/* 45 */       length += 2 + buf.getUnsignedShort(buf.readerIndex() + 2);
/*    */     } 
/*    */     
/* 48 */     if (buf.readableBytes() >= length && buf.getUnsignedShort(buf.readerIndex() + length - 2) == 3338) {
/* 49 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 52 */     int endIndex = buf.readerIndex() - 1;
/*    */     do {
/* 54 */       endIndex = buf.indexOf(endIndex + 1, buf.writerIndex(), (byte)13);
/* 55 */       if (endIndex > 0 && buf.writerIndex() > endIndex + 1 && buf.getByte(endIndex + 1) == 10) {
/* 56 */         return buf.readRetainedSlice(endIndex + 2 - buf.readerIndex());
/*    */       }
/* 58 */     } while (endIndex > 0);
/*    */     
/* 60 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gt06FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */