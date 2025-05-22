/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ 
/*    */ public class MegastekFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 32 */     if (buf.readableBytes() < 10) {
/* 33 */       return null;
/*    */     }
/*    */     
/* 36 */     if (Character.isDigit(buf.getByte(buf.readerIndex()))) {
/* 37 */       int length = 4 + Integer.parseInt(buf.toString(buf.readerIndex(), 4, StandardCharsets.US_ASCII));
/* 38 */       if (buf.readableBytes() >= length) {
/* 39 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     } else {
/* 42 */       while (buf.getByte(buf.readerIndex()) == 13 || buf.getByte(buf.readerIndex()) == 10) {
/* 43 */         buf.skipBytes(1);
/*    */       }
/* 45 */       int delimiter = BufferUtil.indexOf("\r\n", buf);
/* 46 */       if (delimiter == -1) {
/* 47 */         delimiter = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)33);
/*    */       }
/* 49 */       if (delimiter != -1) {
/* 50 */         ByteBuf result = buf.readRetainedSlice(delimiter - buf.readerIndex());
/* 51 */         buf.skipBytes(1);
/* 52 */         return result;
/*    */       } 
/*    */     } 
/*    */     
/* 56 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MegastekFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */