/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
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
/*    */ public class SabertekFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 29 */     int beginIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)2);
/* 30 */     if (beginIndex >= 0) {
/* 31 */       int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)3);
/* 32 */       if (endIndex >= 0) {
/* 33 */         buf.readerIndex(beginIndex + 1);
/* 34 */         ByteBuf frame = buf.readRetainedSlice(endIndex - beginIndex - 1);
/* 35 */         buf.readerIndex(endIndex + 1);
/* 36 */         buf.skipBytes(2);
/* 37 */         return frame;
/*    */       } 
/*    */     } 
/*    */     
/* 41 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SabertekFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */