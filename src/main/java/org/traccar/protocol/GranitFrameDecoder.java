/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import org.traccar.BaseFrameDecoder;
/*    */ import org.traccar.helper.BufferUtil;
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
/*    */ public class GranitFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     int indexEnd = BufferUtil.indexOf("\r\n", buf);
/* 31 */     if (indexEnd != -1) {
/* 32 */       int indexTilde = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)126);
/* 33 */       if (indexTilde != -1 && indexTilde < indexEnd) {
/* 34 */         int length = buf.getUnsignedShortLE(indexTilde + 1);
/* 35 */         indexEnd = BufferUtil.indexOf("\r\n", buf, indexTilde + 2 + length, buf.writerIndex());
/* 36 */         if (indexEnd == -1) {
/* 37 */           return null;
/*    */         }
/*    */       } 
/* 40 */       ByteBuf frame = buf.readRetainedSlice(indexEnd - buf.readerIndex());
/* 41 */       buf.skipBytes(2);
/* 42 */       return frame;
/*    */     } 
/* 44 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GranitFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */