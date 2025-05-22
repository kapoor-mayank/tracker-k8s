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
/*    */ public class RoboTrackFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private int messageLength(ByteBuf buf) {
/* 26 */     switch (buf.getUnsignedByte(buf.readerIndex())) {
/*    */       case 0:
/* 28 */         return 69;
/*    */       case 128:
/* 30 */         return 3;
/*    */       case 3:
/*    */       case 4:
/*    */       case 6:
/* 34 */         return 24;
/*    */       case 7:
/* 36 */         return 8 + buf.getUnsignedShortLE(buf.readerIndex() + 1);
/*    */       case 8:
/* 38 */         return 6;
/*    */     } 
/* 40 */     return Integer.MAX_VALUE;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 48 */     int length = messageLength(buf);
/*    */     
/* 50 */     if (buf.readableBytes() >= length) {
/* 51 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 54 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RoboTrackFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */